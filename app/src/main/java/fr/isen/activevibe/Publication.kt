package fr.isen.activevibe

data class Publication(
    val id: String = "",
    val username: String? = null, // Ajout du champ username
    val imageUrl: String? = null,
    val description: String = "",
    val timestamp: Long? = null,
    val sportType: String? = null,
    val duration: String? = null,
    val distance: String? = null,
    val speed: String? = null
)