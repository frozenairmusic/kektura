package hu.kektura.app.data.seed

import hu.kektura.app.data.model.GpxSegment

object RpddkSegmentSeedData {

    val segments: List<GpxSegment> = listOf(
        seg(201, "Írott-kő – Egyházasrádóc",         "Kőszegi-hegység / Vasi-síkság", 50.0f),
        seg(202, "Egyházasrádóc – Őriszentpéter",    "Őrség",                         66.4f),
        seg(203, "Őriszentpéter – Zalalövő",         "Göcsej",                        54.6f),
        seg(204, "Zalalövő – Rádiháza",              "Zala-völgy",                    41.2f),
        seg(205, "Rádiháza – Palin",                 "Zalai-dombság / Keszthely-hg.", 59.1f),
        seg(206, "Palin – Zalakomár",                "Kis-Balaton",                   31.5f),
        seg(207, "Zalakomár – Kaposmérő",            "Somogy",                        63.4f),
        seg(208, "Kaposmérő – Abaliget vá.",         "Zselic / Mecsek",               67.9f),
        seg(209, "Abaliget vá. – Zobákpuszta",       "Mecsek",                        38.3f),
        seg(210, "Zobákpuszta – Mórágy",             "Mecsek / Tolnai-dombság",       37.7f),
        seg(211, "Mórágy – Szekszárd",               "Tolnai-dombság",                32.0f)
    )

    private fun seg(id: Int, name: String, region: String, distanceKm: Float) =
        GpxSegment(id = id, trailType = "DEL_DUNANTULI", name = name, region = region, distanceKm = distanceKm)
}
