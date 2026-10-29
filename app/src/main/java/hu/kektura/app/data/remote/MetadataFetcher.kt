package hu.kektura.app.data.remote

import hu.kektura.app.data.model.GpxSegment
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * One waypoint-to-waypoint section of a segment, including the GPX track point
 * indices that bound it (for polyline slicing on the map).
 */
data class RemoteSection(
    val from: String,
    val to: String,
    val trackFrom: Int,       // zero-based trkpt index, first section is always 0
    val trackTo: Int,         // zero-based trkpt index, last section is always N-1
    val distanceKm: Float,
    val elevationGainM: Int,
    val elevationLossM: Int
)

/**
 * A single segment entry from the remote metadata.json.
 */
data class RemoteSegmentMeta(
    val trailKey: String,       // "okt", "ak", "rpddk"
    val segmentNumber: String,  // "01", "02", …
    val lastUpdated: String,    // "20251016"
    val filename: String,       // "okt_01_20251107.gpx"
    val code: String,           // "OKT-01"
    val title: String,          // "Írott-kő - Sárvár"
    val distanceKm: Float,      // 72.5
    val elevationGainM: Int,    // 570
    val elevationLossM: Int,    // 1290
    val durationMin: Int,       // 1130 (minutes)
    val stampCount: Int,        // 9
    val sections: List<RemoteSection>
) {
    /**
     * Maps the remote trail key + segment number to the local Room entity ID.
     *   okt   01-27  → 1..27
     *   ak    01-13  → 101..113
     *   rpddk 01-11  → 201..211
     */
    val roomId: Int
        get() {
            val num = segmentNumber.toIntOrNull() ?: return -1
            return when (trailKey) {
                "okt"   -> num
                "ak"    -> 100 + num
                "rpddk" -> 200 + num
                else    -> -1
            }
        }

    /** Trail type string stored in Room ("OKT", "AK", "RPDDK"). */
    val trailType: String get() = trailKey.uppercase()

    /** Full download URL for the GPX file. */
    val gpxUrl: String
        get() = "$BASE_URL/$trailKey/$filename"

    /** Creates a GpxSegment row suitable for initial insertion (no GPX data yet). */
    fun toGpxSegment() = GpxSegment(
        id             = roomId,
        trailType      = trailType,
        name           = title,
        region         = "",
        distanceKm     = distanceKm,
        elevationGainM = elevationGainM,
        elevationLossM = elevationLossM,
        code           = code,
        durationMin    = durationMin,
        stampCount     = stampCount
    )

    companion object {
        private const val BASE_URL = "https://storage.googleapis.com/kektura-gpx/gpx"
    }
}

/**
 * Fetches and parses the remote metadata.json that describes all available
 * GPX segments and their last-updated dates.
 */
object MetadataFetcher {

    private const val METADATA_URL = "https://storage.googleapis.com/kektura-gpx/metadata.json"

    /**
     * Downloads metadata.json and returns a list of [RemoteSegmentMeta] entries,
     * or null if the download fails.
     */
    fun fetch(): List<RemoteSegmentMeta>? {
        val json = downloadJson() ?: return null
        return parseMetadata(json)
    }

    private fun downloadJson(): String? {
        return try {
            val conn = URL(METADATA_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "KekturApp/1.0")
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (e: IOException) {
            null
        }
    }

    private fun parseMetadata(jsonText: String): List<RemoteSegmentMeta> {
        val root = JSONObject(jsonText)
        val result = mutableListOf<RemoteSegmentMeta>()

        for (trailKey in listOf("okt", "ak", "rpddk")) {
            val trailObj = root.optJSONObject(trailKey) ?: continue
            val keys = trailObj.keys()
            while (keys.hasNext()) {
                val segNumber = keys.next()
                val entry = trailObj.optJSONObject(segNumber) ?: continue
                val lastUpdated = entry.optString("last_updated", "")
                val filename = entry.optString("filename", "")
                val code = entry.optString("code", "")
                val title = entry.optString("title", "")

                // Support both new numeric format (72.5) and old string format ("72,5 km")
                val distanceKm = run {
                    val raw = entry.opt("distance")
                    when (raw) {
                        is Number -> raw.toFloat()
                        is String -> raw.replace(",", ".").replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: 0f
                        else -> 0f
                    }
                }

                // Support both new separate fields and old combined "elevation" string
                val gainM: Int
                val lossM: Int
                if (entry.has("elevation_gain")) {
                    gainM = entry.optInt("elevation_gain", 0)
                    lossM = entry.optInt("elevation_loss", 0)
                } else {
                    val parts = entry.optString("elevation", "").split("/")
                    gainM = parts.getOrNull(0)?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 0
                    lossM = parts.getOrNull(1)?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 0
                }

                val durationMin = entry.optInt("duration", 0)
                val stampCount = entry.optInt("stamp_count", 0)

                val sectionsArr = entry.optJSONArray("sections")
                val sections = mutableListOf<RemoteSection>()
                if (sectionsArr != null) {
                    for (i in 0 until sectionsArr.length()) {
                        val s = sectionsArr.optJSONObject(i) ?: continue
                        sections.add(
                            RemoteSection(
                                from           = s.optString("from", ""),
                                to             = s.optString("to", ""),
                                trackFrom      = s.optInt("track_from", 0),
                                trackTo        = s.optInt("track_to", 0),
                                distanceKm     = s.optDouble("distance", 0.0).toFloat(),
                                elevationGainM = s.optInt("elevation_gain", 0),
                                elevationLossM = s.optInt("elevation_loss", 0)
                            )
                        )
                    }
                }

                if (lastUpdated.isNotBlank() && filename.isNotBlank()) {
                    result.add(
                        RemoteSegmentMeta(
                            trailKey       = trailKey,
                            segmentNumber  = segNumber,
                            lastUpdated    = lastUpdated,
                            filename       = filename,
                            code           = code,
                            title          = title,
                            distanceKm     = distanceKm,
                            elevationGainM = gainM,
                            elevationLossM = lossM,
                            durationMin    = durationMin,
                            stampCount     = stampCount,
                            sections       = sections
                        )
                    )
                }
            }
        }
        return result
    }
}
