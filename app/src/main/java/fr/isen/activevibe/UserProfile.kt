package fr.isen.activevibe

data class UserProfile(
    val nom: String = "",
    val email: String = "",
    val age: String = "",
    val gender: String = "",
    val nationality: String = "",
    val height: String = "",
    val weight: String = "",
    val sport: String = "",
    val role: String = "",
    val level: String = "",
    val team: String = "",
    val achievements: String = ""
) {
    constructor() : this("", "", "", "", "", "", "", "", "", "", "", "")
}
