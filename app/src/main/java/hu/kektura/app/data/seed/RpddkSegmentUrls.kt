package hu.kektura.app.data.seed

object RpddkSegmentUrls {

    private const val BASE = "https://turistaterkepek.hu/kekturahu/gpx/nagyszakasz"

    val urls: Map<Int, String> = mapOf(
        201 to "$BASE/rpddk_01_20251008.gpx",
        202 to "$BASE/rpddk_02_20251008.gpx",
        203 to "$BASE/rpddk_03_20241217.gpx",
        204 to "$BASE/rpddk_04_20260130.gpx",
        205 to "$BASE/rpddk_05_20251120.gpx",
        206 to "$BASE/rpddk_06_20220602.gpx",
        207 to "$BASE/rpddk_07_20251120.gpx",
        208 to "$BASE/rpddk_08_20250410.gpx",
        209 to "$BASE/rpddk_09_20250626.gpx",
        210 to "$BASE/rpddk_10_20241217.gpx",
        211 to "$BASE/rpddk_11_20230824.gpx"
    )
}
