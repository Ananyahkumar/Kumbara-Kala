package com.example.kumbarakala.data

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProfileRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("artisan_profile", Context.MODE_PRIVATE)

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Remote Authentication and Database
    suspend fun signUp(email: String, password: String, name: String, phone: String?) {
        // 1. Create User in Firebase Auth
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid ?: throw Exception("Failed to get User ID")

        // Save locally right after auth succeeds so navigation can proceed
        // even if Firestore is temporarily slow/unavailable.
        val normalizedPhone = phone?.trim().orEmpty()
        saveProfile(name = name, phone = normalizedPhone, title = "", bio = "")
        saveAuthDetails(email, password)

        // 2. Save profile in Firestore
        val userData = hashMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "title" to "",
            "bio" to "" // default bio
        )
        if (normalizedPhone.isNotEmpty()) {
            userData["phone"] = normalizedPhone
        }
        try {
            firestore.collection("users").document(userId).set(userData).await()
        } catch (_: Exception) {
            // Keep signup successful; profile can be synced later.
        }
    }

    suspend fun login(email: String, password: String) {
        // 1. Authenticate with Firebase Auth
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid ?: throw Exception("Failed to get User ID")

        // 2. Fetch profile from Firestore
        val documentSnapshot = firestore.collection("users").document(userId).get().await()
        
        if (documentSnapshot.exists()) {
            val name = documentSnapshot.getString("name") ?: "Artisan"
            val phone = documentSnapshot.getString("phone") ?: ""
            val title = documentSnapshot.getString("title") ?: ""
            val bio = sanitizeLegacyBio(documentSnapshot.getString("bio") ?: "")
            val profilePicPath = documentSnapshot.getString("profilePicPath")

            // 3. Save to local cache
            saveProfile(name = name, phone = phone, title = title, bio = bio, profilePicPath = profilePicPath)
            saveAuthDetails(email, password)
        } else {
            throw Exception("User profile data not found")
        }
    }

    // Local Cache Methods
    fun saveProfile(name: String, phone: String, title: String, bio: String, profilePicPath: String? = null) {
        val editor = sharedPreferences.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_PHONE, phone)
            .putString(KEY_TITLE, title)
            .putString(KEY_BIO, bio)
            .putBoolean(KEY_HAS_PROFILE, true)
            
        if (profilePicPath != null) {
            editor.putString(KEY_PROFILE_PIC_PATH, profilePicPath)
        }
            
        editor.apply()
    }

    fun saveAuthDetails(email: String, password: String) {
        sharedPreferences.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun hasProfile(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_PROFILE, false)
    }

    fun getName(): String {
        return sharedPreferences.getString(KEY_NAME, "") ?: ""
    }

    fun getPhone(): String {
        return sharedPreferences.getString(KEY_PHONE, "") ?: ""
    }

    fun getTitle(): String {
        val title = sharedPreferences.getString(KEY_TITLE, "") ?: ""
        // Backward compatibility for previously saved default title.
        return if (title == "Master Potter" || title == "Master Potter, 3rd Generation") "" else title
    }

    fun getBio(): String {
        val bio = sharedPreferences.getString(KEY_BIO, "") ?: ""
        return sanitizeLegacyBio(bio)
    }
    
    fun getProfilePicPath(): String? {
        return sharedPreferences.getString(KEY_PROFILE_PIC_PATH, null)
    }

    fun getEmail(): String {
        return sharedPreferences.getString(KEY_EMAIL, "") ?: ""
    }

    fun getPassword(): String {
        return sharedPreferences.getString(KEY_PASSWORD, "") ?: ""
    }

    fun saveMakerDetails(
        email: String,
        location: String,
        experience: String,
        specialization: String
    ) {
        sharedPreferences.edit()
            .putString(KEY_MAKER_EMAIL, email)
            .putString(KEY_MAKER_LOCATION, location)
            .putString(KEY_MAKER_EXPERIENCE, experience)
            .putString(KEY_MAKER_SPECIALIZATION, specialization)
            .apply()
    }

    fun getMakerEmail(): String {
        return sharedPreferences.getString(KEY_MAKER_EMAIL, getEmail()) ?: ""
    }

    fun getMakerLocation(): String {
        return sharedPreferences.getString(KEY_MAKER_LOCATION, "") ?: ""
    }

    fun getMakerExperience(): String {
        return sharedPreferences.getString(KEY_MAKER_EXPERIENCE, "") ?: ""
    }

    fun getMakerSpecialization(): String {
        return sharedPreferences.getString(KEY_MAKER_SPECIALIZATION, "") ?: ""
    }
    
    fun clearProfile() {
        sharedPreferences.edit().clear().apply()
        auth.signOut()
    }

    fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    companion object {
        private const val KEY_NAME = "key_name"
        private const val KEY_PHONE = "key_phone"
        private const val KEY_TITLE = "key_title"
        private const val KEY_BIO = "key_bio"
        private const val KEY_PROFILE_PIC_PATH = "key_profile_pic_path"
        private const val KEY_HAS_PROFILE = "key_has_profile"
        private const val KEY_EMAIL = "key_email"
        private const val KEY_PASSWORD = "key_password"
        private const val KEY_MAKER_EMAIL = "key_maker_email"
        private const val KEY_MAKER_LOCATION = "key_maker_location"
        private const val KEY_MAKER_EXPERIENCE = "key_maker_experience"
        private const val KEY_MAKER_SPECIALIZATION = "key_maker_specialization"
        private const val LEGACY_DEFAULT_BIO =
            "For over 80 years, my family has been shaping the earth into vessels that nourish the body and soul. " +
                "Every pot, pan, and lamp is crafted using natural clay sourced directly from our ancestral lands, " +
                "without any harmful chemicals or glazes.\n\nWe believe in preserving the science of our ancestors—" +
                "where clay naturally alkaline, keeps food fresh, and cools water without electricity. " +
                "Thank you for bringing our legacy into your modern home."
    }

    private fun sanitizeLegacyBio(value: String): String {
        return if (value.trim() == LEGACY_DEFAULT_BIO.trim()) "" else value
    }
}
