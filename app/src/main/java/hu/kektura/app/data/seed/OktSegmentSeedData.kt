package hu.kektura.app.data.seed

import hu.kektura.app.data.model.GpxSegment

object OktSegmentSeedData {

    val segments: List<GpxSegment> = listOf(
        seg(1,  "Írott-kő – Sárvár",                               "Kőszegi-hegység",              72.5f),
        seg(2,  "Sárvár – Sümeg",                                  "Kemeneshát / Zalai-dombság",   72.6f),
        seg(3,  "Sümeg – Keszthely",                               "Keszthely-hegység",             45.3f),
        seg(4,  "Keszthely – Tapolca",                             "Tapolcai-medence",              29.4f),
        seg(5,  "Tapolca – Badacsonytördemic",                     "Balaton-felvidék",              17.2f),
        seg(6,  "Badacsonytördemic – Nagyvázsony",                 "Balaton-felvidék",              49.7f),
        seg(7,  "Nagyvázsony – Városlőd",                          "Bakony",                        24.7f),
        seg(8,  "Városlőd – Zirc",                                 "Bakony",                        38.8f),
        seg(9,  "Zirc – Bodajk",                                   "Bakony",                        58.6f),
        seg(10, "Bodajk – Szárliget",                              "Vértes",                        56.2f),
        seg(11, "Szárliget – Dorog",                               "Gerecse",                       69.0f),
        seg(12, "Dorog – Piliscsaba",                              "Pilis",                         18.6f),
        seg(13, "Piliscsaba – Hűvösvölgy",                         "Pilis / Budai-hegység",         22.3f),
        seg(14, "Hűvösvölgy – Rozália téglagyár",                  "Budai-hegység / Pilis",         14.1f),
        seg(15, "Rozália téglagyár – Dobogókő",                    "Pilis",                         22.8f),
        seg(16, "Dobogókő – Visegrád",                             "Pilis / Visegrádi-hegység",     24.7f),
        seg(17, "Nagymaros – Nógrád",                              "Börzsöny",                      41.0f),
        seg(18, "Nógrád – Becske",                                 "Nógrád",                        59.9f),
        seg(19, "Becske – Mátraverebély",                          "Cserhát / Mátra",               74.4f),
        seg(20, "Mátraverebély – Mátraháza",                       "Mátra",                         26.2f),
        seg(21, "Mátraháza – Sirok",                               "Mátra / Bükk-előhegység",       26.1f),
        seg(22, "Sirok – Szarvaskő",                               "Bükk",                          18.0f),
        seg(23, "Szarvaskő – Putnok",                              "Bükk / Aggtelek-karszt",        63.0f),
        seg(24, "Putnok – Bódvaszilas",                            "Aggtelek-karszt",               63.9f),
        seg(25, "Bódvaszilas – Boldogkőváralja",                   "Cserehát",                      68.4f),
        seg(26, "Boldogkőváralja – Nagy-Nyugodó",                  "Zemplén",                       54.4f),
        seg(27, "Nagy-Nyugodó – Hollóháza",                        "Zemplén",                       52.3f)
    )

    private fun seg(id: Int, name: String, region: String, distanceKm: Float) =
        GpxSegment(id = id, trailType = "OKT", name = name, region = region, distanceKm = distanceKm)
}
