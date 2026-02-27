package hu.kektura.app.util

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object GpxDownloader {

    /** Downloads a GPX file and returns its content as a String, or null on failure. */
    fun download(urlString: String): String? {
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout    = 30_000
            conn.requestMethod  = "GET"
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
}
