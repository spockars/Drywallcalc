package com.example.drywall.domain.engine

import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.DrywallMaterial

/**
 * Catálogo completo de insumos cotizables, en el orden en que se muestran en la pantalla de
 * precios. Incluye perfiles de 2.44 m, 3.0 m y placas de yeso y fibrocemento.
 */
val ALL_DRYWALL_MATERIALS: List<DrywallMaterial> = listOf(
    // Estructura 2.44 m (Estándar Colombia)
    DrywallCatalog.CANAL_GUIA_2_44M,
    DrywallCatalog.POSTE_MONTANTE_2_44M,
    DrywallCatalog.PERFIL_OMEGA_2_44M,
    DrywallCatalog.PERFIL_ANGULO_PERIMETRAL_2_44M,
    // Estructura 3.0 m (Industrial)
    DrywallCatalog.CANAL_GUIA_3M,
    DrywallCatalog.POSTE_MONTANTE_3M,
    DrywallCatalog.PERFIL_OMEGA_3M,
    DrywallCatalog.PERFIL_ANGULO_PERIMETRAL_3M,
    // Placas
    DrywallCatalog.getBoardMaterial(DrywallBoardType.ESTANDAR_ST),
    DrywallCatalog.getBoardMaterial(DrywallBoardType.RESISTENTE_HUMEDAD_RH),
    DrywallCatalog.getBoardMaterial(DrywallBoardType.RESISTENTE_FUEGO_RF),
    DrywallCatalog.getBoardMaterial(DrywallBoardType.FIBROCEMENTO_6MM),
    DrywallCatalog.getBoardMaterial(DrywallBoardType.FIBROCEMENTO_8MM),
    DrywallCatalog.getBoardMaterial(DrywallBoardType.FIBROCEMENTO_10MM),
    // Fijaciones y accesorios
    DrywallCatalog.ALAMBRE_GALVANIZADO,
    DrywallCatalog.TABLILLA_PVC_5_95M,
    DrywallCatalog.CORNISA_PVC_3M,
    DrywallCatalog.TORNILLO_SOMBRERILLO_PVC,
    DrywallCatalog.ESQUINERO_PERFORADO_3M,
    DrywallCatalog.TORNILLO_T1,
    DrywallCatalog.TORNILLO_T2,
    DrywallCatalog.TORNILLO_FIBROCEMENTO,
    DrywallCatalog.ANCLAJE_CHAZO_TORNILLO,
    DrywallCatalog.CINTA_PAPEL_JUNTAS,
    DrywallCatalog.MASILLA_JUNTAS_BALDE,
    DrywallCatalog.LANA_AISLAMIENTO
)
