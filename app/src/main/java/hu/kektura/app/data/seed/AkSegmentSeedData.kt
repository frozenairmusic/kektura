package hu.kektura.app.data.seed

import hu.kektura.app.data.model.GpxSegment

object AkSegmentSeedData {

    val segments: List<GpxSegment> = listOf(
        seg(101, "Szekszárd – Bajai átkelés",        "Tolna / Duna-ártér",         45.2f),
        seg(102, "Bajai átkelés – Hajósi pincék",    "Bácska",                     52.4f),
        seg(103, "Hajósi pincék – Petróczi-iskola",  "Kiskunság",                  64.5f),
        seg(104, "Petróczi-iskola – Mindszent vá.",  "Duna–Tisza köze",            97.5f),
        seg(105, "Mindszent vá. – Szarvas",          "Tisza-ártér / Körös-vidék",  77.4f),
        seg(106, "Szarvas – Dévaványa",              "Körös-vidék",                75.8f),
        seg(107, "Dévaványa – Vésztő vá.",           "Körös-vidék",                47.4f),
        seg(108, "Vésztő vá. – Berettyóújfalu",     "Bihar",                     101.5f),
        seg(109, "Berettyóújfalu – Létavértes",      "Bihar",                      67.9f),
        seg(110, "Létavértes – Halápi csárda",       "Hajdúság / Nyírség",         58.5f),
        seg(111, "Halápi csárda – Nyírbátor",        "Nyírség",                    65.0f),
        seg(112, "Nyírbátor – Kisvárda",             "Nyírség / Rétköz",           50.8f),
        seg(113, "Kisvárda – Sátoraljaújhely",       "Bodrogköz / Zemplén",        66.2f)
    )

    private fun seg(id: Int, name: String, region: String, distanceKm: Float) =
        GpxSegment(id = id, trailType = "ALFOLDI", name = name, region = region, distanceKm = distanceKm)
}
