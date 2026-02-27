package hu.kektura.app.data.seed

object AkSegmentUrls {

    private const val BASE = "https://turistaterkepek.hu/kekturahu/gpx/nagyszakasz"

    val urls: Map<Int, String> = mapOf(
        101 to "$BASE/ak_01_20231109.gpx",
        102 to "$BASE/ak_02_20240730.gpx",
        103 to "$BASE/ak_03_20251210.gpx",
        104 to "$BASE/ak_04_20251114.gpx",
        105 to "$BASE/ak_05_20251203.gpx",
        106 to "$BASE/ak_06_20250612.gpx",
        107 to "$BASE/ak_07_20201112.gpx",
        108 to "$BASE/ak_08_20250107.gpx",
        109 to "$BASE/ak_09_20251114.gpx",
        110 to "$BASE/ak_10_20251107.gpx",
        111 to "$BASE/ak_11_20250903.gpx",
        112 to "$BASE/ak_12_20251107.gpx",
        113 to "$BASE/ak_13_20251107.gpx"
    )
}
