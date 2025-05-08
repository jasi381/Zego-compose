package com.jasmeet.myapplication

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.jasmeet.myapplication.home.HomeScreen
import com.jasmeet.myapplication.homme.MainActivity2
import com.jasmeet.myapplication.login.LoginScreen
import com.jasmeet.myapplication.signup.SignUpScreen
import com.jasmeet.myapplication.ui.theme.MyApplicationTheme
import com.permissionx.guolindev.PermissionX
import com.zegocloud.uikit.internal.ZegoUIKitLanguage
import com.zegocloud.uikit.plugin.common.IZegoUIKitSignalingPlugin
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener
import com.zegocloud.uikit.prebuilt.call.event.ErrorEventsListener
import com.zegocloud.uikit.prebuilt.call.event.SignalPluginConnectListener
import com.zegocloud.uikit.prebuilt.call.event.ZegoCallEndReason
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoTranslationText
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import dagger.hilt.android.AndroidEntryPoint
import im.zego.zim.enums.ZIMConnectionEvent
import im.zego.zim.enums.ZIMConnectionState
import org.json.JSONObject
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isUserLoggedIn = checkIfUserLoggedIn()

        // If user is logged in, direct to MainActivity2
        if (isUserLoggedIn) {
            startActivity(Intent(this, MainActivity2::class.java))
            finish() // Close this activity so users can't go back to login
            return
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(Transparent.toArgb(), Transparent.toArgb())
        )
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        NavHostScreen()
                    }
                }
            }
        }

    }


    private fun checkIfUserLoggedIn(): Boolean {
        val sharedPrefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val email = sharedPrefs.getString("user_email", null)
        val userId = sharedPrefs.getString("user_id", null)

        // Return true if both email and userId exist in SharedPreferences
        return email != null && userId != null
    }


}

@Composable
fun NavHostScreen() {
    val navController = rememberNavController()
    var start = "login"

    NavHost(navController = navController, startDestination = start) {
        composable("login") {
            LoginScreen(navController)
        }
        composable("signup") {
            SignUpScreen(navController)
        }
    }
}

