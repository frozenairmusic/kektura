package hu.kektura.app.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.util.GpxParser
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

private const val STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val SOURCE_TRAIL = "trail-source"
private const val SOURCE_STAMP = "stamp-source"
private const val LAYER_TRAIL = "trail-layer"
private const val LAYER_STAMP_COLLECTED = "stamp-collected-layer"
private const val LAYER_STAMP_PENDING = "stamp-pending-layer"

/** Bounding box covering Hungary for offline tile downloads. */
private val HUNGARY_BOUNDS = LatLngBounds.from(48.6, 23.1, 45.7, 16.1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel()
) {
    val segments by viewModel.segments.collectAsStateWithLifecycle()
    val stampPoints by viewModel.stampPoints.collectAsStateWithLifecycle()
    val collectedIds by viewModel.collectedIds.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    var selectedStamp by remember { mutableStateOf<StampPoint?>(null) }
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleRef by remember { mutableStateOf<Style?>(null) }
    var locationEnabled by remember { mutableStateOf(false) }

    // Always-current reference to stamp points for use inside the map click listener.
    val currentStampPoints by rememberUpdatedState(stampPoints)

    val mapView = remember {
        MapView(context, MapLibreMapOptions.createFromAttributes(context))
            .also { it.onCreate(null) }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        locationEnabled = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Forward Android lifecycle events to MapView.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START   -> mapView.onStart()
                Lifecycle.Event.ON_RESUME  -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE   -> mapView.onPause()
                Lifecycle.Event.ON_STOP    -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else                       -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Async map + style initialization. Also registers the stamp-click listener once.
    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            mapRef = map
            map.setStyle(Style.Builder().fromUri(STYLE_URL)) { style ->
                styleRef = style

                // Centre on Hungary on first load.
                map.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(47.4979, 19.0402))
                            .zoom(6.5)
                            .build()
                    )
                )

                // Trail polyline source + layer
                style.addSource(GeoJsonSource(SOURCE_TRAIL, FeatureCollection.fromFeatures(listOf())))
                style.addLayer(
                    LineLayer(LAYER_TRAIL, SOURCE_TRAIL).also { layer ->
                        layer.setProperties(
                            PropertyFactory.lineColor("#1565C0"),
                            PropertyFactory.lineWidth(4f),
                            PropertyFactory.lineOpacity(0.85f)
                        )
                    }
                )

                // Stamp-point source + two circle layers coloured by collected state
                style.addSource(GeoJsonSource(SOURCE_STAMP, FeatureCollection.fromFeatures(listOf())))
                style.addLayer(
                    CircleLayer(LAYER_STAMP_COLLECTED, SOURCE_STAMP).also { layer ->
                        layer.setFilter(
                            Expression.eq(Expression.get("collected"), Expression.literal(true))
                        )
                        layer.setProperties(
                            PropertyFactory.circleRadius(7f),
                            PropertyFactory.circleColor("#4CAF50"),
                            PropertyFactory.circleStrokeWidth(1.5f),
                            PropertyFactory.circleStrokeColor("#FFFFFF")
                        )
                    }
                )
                style.addLayer(
                    CircleLayer(LAYER_STAMP_PENDING, SOURCE_STAMP).also { layer ->
                        layer.setFilter(
                            Expression.eq(Expression.get("collected"), Expression.literal(false))
                        )
                        layer.setProperties(
                            PropertyFactory.circleRadius(7f),
                            PropertyFactory.circleColor("#FF7043"),
                            PropertyFactory.circleStrokeWidth(1.5f),
                            PropertyFactory.circleStrokeColor("#FFFFFF")
                        )
                    }
                )
            }

            // Tap on a stamp circle → show the info bottom sheet.
            map.addOnMapClickListener { latLng ->
                val screen = map.projection.toScreenLocation(latLng)
                val hits = map.queryRenderedFeatures(
                    PointF(screen.x, screen.y),
                    LAYER_STAMP_COLLECTED, LAYER_STAMP_PENDING
                )
                val stampId = hits.firstOrNull()?.getNumberProperty("sp_id")?.toInt()
                if (stampId != null) {
                    selectedStamp = currentStampPoints.find { it.id == stampId }
                    selectedStamp != null
                } else {
                    false
                }
            }
        }
    }

    // Enable GPS blue dot once the user grants location permission.
    LaunchedEffect(locationEnabled, styleRef) {
        val style = styleRef ?: return@LaunchedEffect
        val map = mapRef ?: return@LaunchedEffect
        if (locationEnabled) enableLocationComponent(context, map, style)
    }

    // Rebuild trail polylines when the set of available segments changes.
    LaunchedEffect(styleRef, segments) {
        val style = styleRef ?: return@LaunchedEffect
        updateTrailSource(style, segments)
        // Keep DB stamp positions in sync with the embedded GPX waypoints.
        segments.forEach { seg ->
            if (seg.gpxContent != null) viewModel.syncWaypointsFromGpx(seg)
        }
    }

    // Refresh stamp circles when collection state changes.
    LaunchedEffect(styleRef, stampPoints, collectedIds) {
        val style = styleRef ?: return@LaunchedEffect
        updateStampSource(style, stampPoints, collectedIds)
    }

    // Ask for location on first composition.
    LaunchedEffect(Unit) {
        locationLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        // Offline download FAB
        FloatingActionButton(
            onClick = {
                downloadOfflineRegion(context) { msg ->
                    scope.launch { snackbarHost.showSnackbar(msg) }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 160.dp)
        ) {
            Icon(Icons.Default.Download, contentDescription = "Offline letöltés")
        }

        // My-location FAB
        FloatingActionButton(
            onClick = {
                mapRef?.locationComponent?.lastKnownLocation?.let { loc ->
                    mapRef?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(loc.latitude, loc.longitude), 13.0
                        )
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 88.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Helyzetem")
        }

        SnackbarHost(
            hostState = snackbarHost,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Stamp info bottom sheet
    selectedStamp?.let { stamp ->
        ModalBottomSheet(onDismissRequest = { selectedStamp = null }) {
            StampSheetContent(
                stamp = stamp,
                collected = collectedIds.contains(stamp.id),
                onCollect = {
                    viewModel.markCollected(stamp.id)
                    selectedStamp = null
                },
                onRemove = {
                    viewModel.removeStamp(stamp.id)
                    selectedStamp = null
                }
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@SuppressLint("MissingPermission")
private fun enableLocationComponent(context: Context, map: MapLibreMap, style: Style) {
    val locationComponent = map.locationComponent
    locationComponent.activateLocationComponent(
        LocationComponentActivationOptions.builder(context, style).build()
    )
    locationComponent.isLocationComponentEnabled = true
    locationComponent.cameraMode = CameraMode.NONE
    locationComponent.renderMode = RenderMode.COMPASS
}

private fun updateTrailSource(style: Style, segments: List<GpxSegment>) {
    val features = segments.mapNotNull { seg ->
        val gpxContent = seg.gpxContent ?: return@mapNotNull null
        val pts = try {
            GpxParser.parseTracks(gpxContent.byteInputStream())
        } catch (_: Exception) {
            return@mapNotNull null
        }
        if (pts.isEmpty()) return@mapNotNull null
        Feature.fromGeometry(LineString.fromLngLats(pts.map { Point.fromLngLat(it.lon, it.lat) }))
    }
    (style.getSource(SOURCE_TRAIL) as? GeoJsonSource)
        ?.setGeoJson(FeatureCollection.fromFeatures(features))
}

private fun updateStampSource(
    style: Style,
    stampPoints: List<StampPoint>,
    collectedIds: Set<Int>
) {
    val features = stampPoints.map { sp ->
        Feature.fromGeometry(Point.fromLngLat(sp.longitude, sp.latitude)).also { f ->
            f.addBooleanProperty("collected", collectedIds.contains(sp.id))
            f.addNumberProperty("sp_id", sp.id)
        }
    }
    (style.getSource(SOURCE_STAMP) as? GeoJsonSource)
        ?.setGeoJson(FeatureCollection.fromFeatures(features))
}

private fun downloadOfflineRegion(context: Context, onMessage: (String) -> Unit) {
    val offlineManager = OfflineManager.getInstance(context)
    val definition = OfflineTilePyramidRegionDefinition(
        STYLE_URL,
        HUNGARY_BOUNDS,
        6.0,
        14.0,
        context.resources.displayMetrics.density
    )
    offlineManager.createOfflineRegion(
        definition,
        "OKT".toByteArray(),
        object : OfflineManager.CreateOfflineRegionCallback {
            override fun onCreate(region: OfflineRegion) {
                region.setObserver(object : OfflineRegion.OfflineRegionObserver {
                    override fun onStatusChanged(status: OfflineRegionStatus) {
                        if (status.isComplete) onMessage("Offline térkép letöltve!")
                    }
                    override fun onError(error: OfflineRegionError) {
                        onMessage("Letöltés sikertelen: ${error.message}")
                    }
                    override fun mapboxTileCountLimitExceeded(limit: Long) {
                        onMessage("Csempe korlát elérve")
                    }
                })
                region.setDownloadState(OfflineRegion.STATE_ACTIVE)
            }
            override fun onError(error: String) {
                onMessage("Letöltés sikertelen: $error")
            }
        }
    )
}

@Composable
private fun StampSheetContent(
    stamp: StampPoint,
    collected: Boolean,
    onCollect: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stamp.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (stamp.region.isNotBlank()) {
            Text(
                text = stamp.region,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (stamp.stampCode.isNotBlank()) {
            Text(
                text = stamp.stampCode,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (stamp.notes.isNotBlank()) {
            Text(
                text = stamp.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = if (collected) onRemove else onCollect,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (collected) "Bejegyzés visszavonása" else "Bélyegző begyűjtve!")
        }
    }
}

