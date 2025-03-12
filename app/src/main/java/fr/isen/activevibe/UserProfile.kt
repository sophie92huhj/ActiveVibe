package fr.isen.activevibe

//import androidx.room.Entity
//import androidx.room.PrimaryKey


//@Entity(tableName = "user_profile")

data class UserProfile(
    //@PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileImageUri: String = "",
    val nomUtilisateur: String = "",
    val nom: String = "",
    val email: String = "",
    val age: String = "",
    val gender: String = "",
    val nationality: String = "",
    val height: String = "",
    val weight: String = "",
    val sport: String = "",
    //val role: String = "",
    val level: String = "",
    val team: String = "",
    //val achievements: String = ""
) {
    constructor() : this("", "", "", "", "", "", "", "", "", "", "", "")
}
