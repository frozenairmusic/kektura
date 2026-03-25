package hu.kektura.app.data.remote

import hu.kektura.app.data.model.GpxSegment
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * A single segment entry from the remote metadata.json.
 */
data class RemoteSegmentMeta(
    val trailKey: String,       // "okt", "ak", "rpddk"
    val segmentNumber: String,  // "01", "02", …
    val lastUpdated: String,    // "20251016"
    val filename: String,       // "okt_01_20251107.gpx"
    val title: String,          // "Írott-kő - Sárvár"
    val distanceKm: Float       // 72.5
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
        id          = roomId,
        trailType   = trailType,
        name        = title,
        region      = "",
        distanceKm  = distanceKm
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

    /**
     * Parses the metadata JSON which has the structure:
     * ```json
     * {
     *   "okt": { "01": { "last_updated": "20251107", "filename": "okt_01_20251107.gpx" }, … },
     *   "ak":  { "01": { … }, … },
     *   "rpddk": { "01": { … }, … },
     *   "last_updated": "2026-03-03"
     * }
     * ```
     */
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
                val title = entry.optString("title", "")
                val distanceKm = entry.optString("distance", "0")
                    .replace(",", ".")
                    .replace(Regex("[^0-9.]"), "")
                    .toFloatOrNull() ?: 0f
                if (lastUpdated.isNotBlank() && filename.isNotBlank()) {
                    result.add(
                        RemoteSegmentMeta(
                            trailKey      = trailKey,
                            segmentNumber = segNumber,
                            lastUpdated   = lastUpdated,
                            filename      = filename,
                            title         = title,
                            distanceKm    = distanceKm
                        )
                    )
                }
            }
        }
        return result
    }
}
