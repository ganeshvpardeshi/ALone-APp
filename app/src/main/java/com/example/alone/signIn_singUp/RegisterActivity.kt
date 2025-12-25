package com.example.alone.signIn_singUp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.alone.R
import com.example.alone.beConnection.ApiClient
import com.example.alone.model.User
import com.example.alone.signIn_singUp.botChatInterface.BotChatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class RegisterActivity : AppCompatActivity() {


    // Username: only letters, digits and '_' allowed, at least 1 char
    private val USERNAME_REGEX = "^[A-Za-z0-9_]+$".toRegex()

    // Email: ONLY @gmail.com domain allowed
    private val EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@gmail\\.com$".toRegex()
    // Password: >= 9 chars, at least 1 upper, 1 lower, 1 digit, 1 special char
    private val PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{9,}$".toRegex()

    /**
     * Validates username: non-null, only letters/digits/underscore
     */
    fun isUsernameValid(username: String?): Boolean {
        return username != null && username.matches(USERNAME_REGEX)
    }

    /**
     * Validates email: non-null, valid format, NOT @gmail.com
     */
    fun isEmailValid(email: String?): Boolean {
        return email != null && email.matches(EMAIL_REGEX)
    }

    /**
     * Validates password: non-null, >=9 chars, 1 upper, 1 lower, 1 digit, 1 special
     */
    fun isPasswordValid(password: String?): Boolean {
        return password != null && password.matches(PASSWORD_REGEX)
    }

    /**
     * Validates profileName: non-null, different from username (case-insensitive)
     */
    fun isProfileNameValid(profileName: String?, username: String?): Boolean {
        return profileName != null &&
                username != null &&
                username.trim().equals(profileName.trim(), ignoreCase = true).not()
    }

    /**
     * Validates age: non-null, greater than 18
     */
    fun isAgeValid(age: Int?): Boolean {
        return age != null && age > 18
    }

    /**
     * Validates gender: non-null, M/F/O (case-insensitive)
     */
    fun isGenderValid(gender: String?): Boolean {
        if (gender == null) return false
        val g = gender.trim().uppercase()
        return g == "M" || g == "F" || g == "O"
    }

//    suspend fun registerUser(
//        unique_id: String, tempUsername: String,
//        tempEmail: String, tempPassword: String,
//        tempProfileName: String, tempProfilePhoto: String,
//        tempGender: String, tempAge: Int){
//
//        val newUser = User(
//            unique_id = unique_id,
//            username = tempUsername ,
//            email = tempEmail,
//            password = tempPassword,
//            profile_name = tempProfileName,
//            profile_photo = tempProfilePhoto,
//            gender = tempGender,
//            age = tempAge
//        )
//        val created = withContext(Dispatchers.IO) {
//            ApiClient.userApi.createUser(newUser)
//        }
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        val intent = Intent(this, BotChatActivity::class.java)
        intent.putExtra("AUTH_MODE", "SIGNUP")
        startActivity(intent)

    }
}