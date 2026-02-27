package hu.kektura.app.util

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

/**
 * Parses GPX files produced by turistaterkepek.hu.
 *
 * OKT stamp stations are encoded as <wpt> elements.
 * The stamp code (e.g. OKTPH_07) appears in the <desc> field inside a
 * parenthesised group: "… (EM143INF) (OKTPH_07)"
 */
object GpxParser {

    data class WptPoint(
        val lat: Double,
        val lon: Double,
        val name: String,
        val desc: String = "",
        val ele: Double = 0.0
    )

    /**
     * Extracts stamp waypoints from <wpt> elements.
     * Identical parsing logic to the original kektura-old app.
     */
    fun parseWaypoints(input: InputStream): List<WptPoint> {
        val waypoints = mutableListOf<WptPoint>()
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(input, null)

        var currentLat: Double? = null
        var currentLon: Double? = null
        var currentName = ""
        var currentDesc = ""
        var currentEle  = 0.0
        var inWpt  = false
        var inName = false
        var inDesc = false
        var inEle  = false

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "wpt" -> {
                        inWpt      = true
                        currentLat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull()
                        currentLon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull()
                        currentName = ""
                        currentDesc = ""
                        currentEle  = 0.0
                    }
                    "name" -> if (inWpt) inName = true
                    "desc" -> if (inWpt) inDesc = true
                    "ele"  -> if (inWpt) inEle  = true
                }
                XmlPullParser.TEXT -> when {
                    inName -> currentName += parser.text
                    inDesc -> currentDesc += parser.text
                    inEle  -> currentEle   = parser.text.trim().toDoubleOrNull() ?: 0.0
                }
                XmlPullParser.END_TAG -> when (parser.name) {
                    "wpt" -> {
                        val lat = currentLat
                        val lon = currentLon
                        if (lat != null && lon != null) {
                            waypoints.add(
                                WptPoint(lat, lon, currentName.trim(), currentDesc.trim(), currentEle)
                            )
                        }
                        inWpt = false
                        currentEle = 0.0
                    }
                    "name" -> inName = false
                    "desc" -> inDesc = false
                    "ele"  -> inEle  = false
                }
            }
            eventType = parser.next()
        }
        return waypoints
    }
}
