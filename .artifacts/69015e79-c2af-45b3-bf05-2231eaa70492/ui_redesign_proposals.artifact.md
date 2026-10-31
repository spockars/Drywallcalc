# Propuestas de Rediseño UI/UX para DrywallCalc

Como Diseñador Senior de UI/UX, he preparado dos propuestas conceptuales completamente diferentes para renovar la interfaz de usuario, la navegación y la experiencia general de **DrywallCalc**, resolviendo los problemas de fatiga de scroll, densidad cognitiva y flujo de navegación.

---

## Propuesta 1: "Pro Contractor Hub" (Panel de Control Ágil para Profesionales)
*Enfoque: Eficiencia operativa, acceso directo a herramientas, velocidad de cálculo y gestión de proyectos.*

### 🧭 Modelo de Navegación
* **Bottom Navigation Bar (Barra de Navegación Inferior):**
  1. 📂 **Proyectos:** Lista de proyectos guardados con tarjetas métricas (costo total, área total, fecha).
  2. ⚡ **Cálculo Rápido:** Acceso directo a una interfaz unificada de cálculo con pestañas superiores rápidas (Muro / Cielo / PVC / Mueble).
  3. 💰 **Precios & Catálogo:** Gestión centralizada de precios de materiales y mano de obra.
  4. 📊 **Reportes:** Centro de exportación de PDF y resúmenes compartidos.

### 🎨 Estilo Visual y UI
* **Paleta de Colores:** Azul Pizarra Oscuro (profesional/industrial) con acentos en Ámbar Construcción (`#F59E0B`) para llamadas a la acción y estados activos.
* **Sticky Bottom Bar (Barra Inferior Flotante):** En la pantalla de cálculo, una barra fija en la parte inferior muestra en **tiempo real** el área neta ($m^2$) y el costo estimado actualizado a medida que el usuario modifica dimensiones o añade aberturas (sin necesidad de hacer scroll hasta el fondo).
* **Secciones Colapsables (Accordions):** Las especificaciones técnicas, aberturas y pintura se organizan en tarjetas colapsables con insignias de resumen (ej. *"1 puerta añadida • Perfilería 64mm"*), reduciendo el scroll vertical en un 60%.

---

## Propuesta 2: "Guided Studio" (Estudio Guiado y Asistente por Pasos)
*Enfoque: Simplicidad visual, experiencia amigable tipo "wizard", ideal tanto para usuarios novatos como profesionales que buscan una interfaz limpia y sin fricciones.*

### 🧭 Navigation & Flow
* **Card-Driven Home (Panel de Bienvenida Visual):**
  * En lugar de menús complejos, la pantalla principal muestra tarjetas visuales grandes y atractivas con ilustraciones de lo que se desea construir: *Muro Drywall*, *Cielo Raso*, *Cielo PVC*, *Mueble a Medida*.
* **Stepped Wizard Flow (Asistente por Pasos):**
  * Al seleccionar un elemento, la app transiciona a un flujo guiado por pasos con una barra de progreso superior:
    * **Paso 1:** Dimensiones (Largo y Alto).
    * **Paso 2:** Aberturas y Vanos (Puertas/Ventanas interactivas).
    * **Paso 3:** Materiales y Acabados (Placas, Aislamiento, Pintura).
    * **Paso 4:** Resultado & Presupuesto Instantáneo.
  * Botones flotantes de "Anterior" / "Siguiente" claramente definidos.

### 🎨 Estilo Visual y UI
* **Paleta de Colores:** Tonos neutros cálidos con Verde Salvia (`#10B981`) y blanco suave, transmitiendo precisión, modernidad y limpieza.
* **Tarjetas de Selección Visual (Visual Choice Chips):** En lugar de menús desplegables grises, se usan tarjetas seleccionables con iconos grandes para tipos de placa (Standard, RH, RF, ST) y calibres de perfilería.

---

## Comparativa y Recomendación

| Criterio | Propuesta 1: Pro Contractor Hub | Propuesta 2: Guided Studio |
| :--- | :--- | :--- |
| **Velocidad de Entrada** | Muy alta (todo en una pantalla con secciones colapsables) | Media (flujo guiado paso a paso) |
| **Curva de Aprendizaje** | Ideal para usuarios expertos / contratistas frecuentes | Excelente para nuevos usuarios o uso esporádico |
| **Uso de Pantalla** | Optimizado con Sticky Bar y acordeones | Optimizado con wizard de pasos limpios |

*Recomendación del Diseñador:* Si la aplicación está enfocada principalmente en contratistas y calculistas experimentados que necesitan velocidad, la **Propuesta 1 ("Pro Contractor Hub")** es la ganadora. Si se busca una experiencia universal y muy accesible para cualquier cliente final o constructor novato, la **Propuesta 2 ("Guided Studio")** ofrece la máxima limpieza visual.
