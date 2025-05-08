package com.jasmeet.myapplication.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jasmeet.myapplication.viewModel.AuthViewModel.Companion.KEY_EMAIL
import com.jasmeet.myapplication.viewModel.AuthViewModel.Companion.KEY_USERNAME
import com.jasmeet.myapplication.viewModel.AuthViewModel.Companion.KEY_USER_ID
import com.jasmeet.myapplication.viewModel.AuthViewModel.Companion.PREFS_NAME
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
   @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    // Get current user directly from AuthViewModel
    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val currentUser = User(
        id = sharedPrefs.getString(KEY_USER_ID, "") ?: "",
        email = sharedPrefs.getString(KEY_EMAIL, "") ?: "",
        username = sharedPrefs.getString(KEY_USERNAME, "") ?: ""
    )

    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    val userState: StateFlow<UserState> = _userState

    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchAllUsers()
    }

    fun fetchAllUsers() {
        _userState.value = UserState.Loading

        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val usersList = documents.mapNotNull { doc ->
                    try {
                        val userId = doc.id
                        val email = doc.getString("email") ?: return@mapNotNull null
                        val username = doc.getString("username") ?: return@mapNotNull null

                        User(
                            id = userId,
                            email = email,
                            username = username
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                _users.value = usersList
                _userState.value = UserState.Success
            }
            .addOnFailureListener { exception ->
                _userState.value = UserState.Error("Failed to fetch users: ${exception.message}")
            }
    }

    fun getUserDetails(userId: String, onResult: (User?) -> Unit) {
        firestore.collection("users").document(userId)
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
                        onResult(user)
                    } else {
                        onResult(null)
                    }
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun signOut() {
        //authViewModel.signOut()
    }
}

sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    object Success : UserState()
    data class Error(val message: String) : UserState()
}