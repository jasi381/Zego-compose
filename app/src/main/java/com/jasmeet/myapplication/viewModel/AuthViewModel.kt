package com.jasmeet.myapplication.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Normal)
    val authState = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        // Load current user on initialization
        loadCurrentUserFromPrefs()

        // Observe auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                // User signed out, clear preferences
                clearCurrentUserFromPrefs()
                _currentUser.value = null
            }
        }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Get user details from Firestore and save to SharedPreferences
                        fetchAndSaveUserDetails(user.uid, isNewUser = false)
                    } else {
                        _authState.value = AuthState.Error("Failed to retrieve user after login")
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error during login")
                }
            }
    }

    fun signUp(email: String, password: String, username: String) {
        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Store user data in Firestore
                        val userData = hashMapOf(
                            "email" to email,
                            "username" to username,
                            "uid" to user.uid,
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        db.collection("users").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                // Save user to SharedPreferences
                                val newUser = User(
                                    id = user.uid,
                                    email = email,
                                    username = username
                                )
                                saveCurrentUserToPrefs(newUser)
                                _currentUser.value = newUser
                                _authState.value = AuthState.Success(isNewUser = true)
                            }
                            .addOnFailureListener { e ->
                                // User created in Auth but Firestore storage failed
                                _authState.value = AuthState.Error("Account created, but failed to save user data: ${e.message}")
                            }
                    } else {
                        _authState.value = AuthState.Error("Failed to retrieve user after sign up")
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error during sign up")
                }
            }
    }

    private fun fetchAndSaveUserDetails(userId: String, isNewUser: Boolean) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val email = document.getString("email")
                    val username = document.getString("username")

                    if (email != null && username != null) {
                        val user = User(
                            id = userId,
                            email = email,
                            username = username
                        )
                        _currentUser.value = user
                        saveCurrentUserToPrefs(user)
                        _authState.value = AuthState.Success(isNewUser = isNewUser)
                    } else {
                        _authState.value = AuthState.Error("Incomplete user data found")
                    }
                } else {
                    _authState.value = AuthState.Error("User data not found")
                }
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error("Failed to fetch user details: ${exception.message}")
            }
    }

    private fun loadCurrentUserFromPrefs() {
        val email = sharedPrefs.getString(KEY_EMAIL, null)
        val username = sharedPrefs.getString(KEY_USERNAME, null)
        val userId = sharedPrefs.getString(KEY_USER_ID, null)

        if (email != null && username != null && userId != null) {
            _currentUser.value = User(
                id = userId,
                email = email,
                username = username
            )
        }
    }

    private fun saveCurrentUserToPrefs(user: User) {
        sharedPrefs.edit().apply {
            putString(KEY_EMAIL, user.email)
            putString(KEY_USERNAME, user.username)
            putString(KEY_USER_ID, user.id)
            apply()
        }
    }

    private fun clearCurrentUserFromPrefs() {
        sharedPrefs.edit().clear().apply()
    }

    fun signOut() {
        _authState.value = AuthState.Loading
        auth.signOut()
        clearCurrentUserFromPrefs()
        _currentUser.value = null
        _authState.value = AuthState.Normal
    }

    fun resetState() {
        _authState.value = AuthState.Normal
    }

    companion object {
        const val PREFS_NAME = "user_preferences"
        const val KEY_EMAIL = "user_email"
        internal const val KEY_USERNAME = "user_username"
        const val KEY_USER_ID = "user_id"
    }
}

data class User(
    val id: String,
    val email: String,
    val username: String
)

sealed class AuthState {
    object Normal : AuthState()
    object Loading : AuthState()
    data class Success(val isNewUser: Boolean) : AuthState()
    data class Error(val message: String) : AuthState()
}