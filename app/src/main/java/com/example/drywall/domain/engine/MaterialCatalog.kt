package com.example.drywall.domain.engine

import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.DrywallMaterial

/**
 * Catálogo completo de insumos cotizables, en el orden en que se muestran en la pantalla de
 * precios. Incluye las tres placas porque cada tipo tiene su propio precio comercial.
 */
val ALL_DRYWALL_MATERIALS: List<DrywallMaterial> = listOf(
    DrywallCatalog.CANAL_GUIA_3M,
    DrywallCatalog.POSTE_MONTANTE_3M,
    DrywallCatalog.PERFIL_OMEGA_3M,
    DrywallCatalog.getBoardMaterial(DrywallBoardType.ESTANDAR_ST),
    DrywallCatalog.getBoardMaterial(DrywallBoardType.RESISTENTE_HUMEDAD_RH),
    DrywallCatalog.getBoardMaterial(DrywallBoardType.RESISTENTE_FUEGO_RF),
    DrywallCatalog.TORNILLO_T1,
    DrywallCatalog.TORNILLO_T2,
    DrywallCatalog.ANCLAJE_CHAZO_TORNILLO,
    DrywallCatalog.CINTA_PAPEL_JUNTAS,
    DrywallCatalog.MASILLA_JUNTAS_BALDE,
    DrywallCatalog.LANA_AISLAMIENTO
)
