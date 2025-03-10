package fr.isen.activevibe

data class Publication(
    val id: String = "",
    val sportType: String = "",
    val description: String = "",
    val imageUrl: String? = null,  // 📷 Image facultative
    val duration: String? = null,  // ⏱ Durée de l'effort (optionnel)
    val distance: String? = null,  // 📏 Distance (optionnel)
    val speed: String? = null,     // ⚡ Vitesse (optionnel)
    val timestamp: Long = System.currentTimeMillis()
)