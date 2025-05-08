package com.jasmeet.myapplication.homme

import android.Manifest.permission
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.jasmeet.myapplication.Creds
import com.jasmeet.myapplication.R
import com.jasmeet.myapplication.VideoCall.MainActivity3
import com.jasmeet.myapplication.homme.ui.theme.MyApplicationTheme
import com.jasmeet.myapplication.viewModel.User
import com.jasmeet.myapplication.viewModel.UserState
import com.jasmeet.myapplication.viewModel.UserViewModel
import com.permissionx.guolindev.PermissionX
import com.zegocloud.uikit.internal.ZegoUIKitLanguage
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.config.ZegoHangUpConfirmDialogInfo
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData
import com.zegocloud.uikit.prebuilt.call.core.invite.advanced.ZegoCallInvitationInCallingConfig
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener
import com.zegocloud.uikit.prebuilt.call.event.ErrorEventsListener
import com.zegocloud.uikit.prebuilt.call.event.SignalPluginConnectListener
import com.zegocloud.uikit.prebuilt.call.event.ZegoCallEndReason
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig.generateDefaultConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallType
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallUser
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInnerText
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInvitationCallListener
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoTranslationText
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import dagger.hilt.android.AndroidEntryPoint
import im.zego.zim.enums.ZIMConnectionEvent
import im.zego.zim.enums.ZIMConnectionState
import org.json.JSONObject

const val TAGGY ="TAGGY"

@AndroidEntryPoint
class MainActivity2 : FragmentActivity() {

    val userViewModel: UserViewModel by viewModels()
    private lateinit var currentUser:User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(Transparent.toArgb(), Transparent.toArgb())
        )
         currentUser = userViewModel.currentUser
        setContent {
            MyApplicationTheme {
                    UsersScreen(userViewModel)

            }
        }
        permissionHandling(this)
     //   initUser()
        initZegoInviteService(
            Creds.appID,
            Creds.appSign,
            currentUser.email,
            currentUser.username
        )
    }

    private fun permissionHandling(activityContext: FragmentActivity) {
        PermissionX.init(activityContext).permissions(permission.SYSTEM_ALERT_WINDOW)
            .onExplainRequestReason { scope, deniedList ->
                val message =
                    "We need your consent for the following permissions in order to use the offline call function properly"
                scope.showRequestReasonDialog(deniedList, message, "Allow", "Deny")
            }.request { allGranted, grantedList, deniedList -> }
    }


    fun initUser(){


            // Create and configure the invitation config
            val invitationConfig = ZegoUIKitPrebuiltCallInvitationConfig().apply {
                // Set basic properties
                showDeclineButton = true
                endCallWhenInitiatorLeave = true

                // Configure ringtones (optional)
                // incomingCallRingtone = "your_ringtone_path"
                // outgoingCallRingtone = "your_ringtone_path"

                // Configure backgrounds (optional)
                incomingCallBackground = ContextCompat.getDrawable(this@MainActivity2, R.drawable.img)
                outgoingCallBackground = ContextCompat.getDrawable(this@MainActivity2, R.drawable.img_1)

                //TODO: Configure notification settings (optional)
                notificationConfig = ZegoNotificationConfig().apply {
                    sound = "true"
                    channelID = "call_invitation_channel"
                    channelName = "Call Notifications"
                    channelDesc = "Notifications for incoming and outgoing calls"

                }

                // Configure custom text (optional)
                innerText = ZegoInnerText().apply {
                    // Customize text elements
                    // incomingCallPageTitle = "Incoming Call"
                }

                // Set call configuration provider (optional)
                provider = ZegoUIKitPrebuiltCallConfigProvider { invitationData ->
                    // Get default config based on call type
                    val config = generateDefaultConfig(invitationData)

                    // Add custom hang-up confirmation
                    config.hangUpConfirmDialogInfo = ZegoHangUpConfirmDialogInfo().apply {
                        title = "Leave the call?"
                        message = "Are you sure you want to leave this call?"
                        cancelButtonName = "Cancel"
                        confirmButtonName = "Leave"
                    }

                    return@ZegoUIKitPrebuiltCallConfigProvider config
                }

                // Configure in-call settings (optional)
                callingConfig = ZegoCallInvitationInCallingConfig().apply {
                    // Configure in-call behavior
                    canInvitingInCalling = true
                    onlyInitiatorCanInvite = false
                }
            }


            ZegoUIKitPrebuiltCallInvitationService.init(
                application,
                Creds.appID,
                Creds.appSign,
                currentUser.email,
                currentUser.username,
                invitationConfig
            )

            // After initializing the service, register event listeners
            // After initializing the service, register event listeners
            ZegoUIKitPrebuiltCallInvitationService.addInvitationCallListener(object:ZegoInvitationCallListener{
                override fun onIncomingCallReceived(
                    callID: String?,
                    caller: ZegoCallUser?,
                    callType: ZegoCallType?,
                    callees: List<ZegoCallUser?>?
                ) {
                    Log.d("CallEvents", "Incoming call received from: ${caller?.name}, callID: $callID, callType: $callType")
                }

                override fun onIncomingCallCanceled(
                    callID: String?,
                    caller: ZegoCallUser?
                ) {
                    Log.d("CallEvents", "Incoming call canceled by: ${caller?.name}, callID: $callID")
                }

                override fun onIncomingCallTimeout(
                    callID: String?,
                    caller: ZegoCallUser?
                ) {
                    Log.d("CallEvents", "Incoming call timeout from: ${caller?.name}, callID: $callID")
                }

                override fun onOutgoingCallAccepted(
                    callID: String?,
                    callee: ZegoCallUser?
                ) {
                    Log.d("CallEvents", "Outgoing call accepted by: ${callee?.name}, callID: $callID")
                }

                override fun onOutgoingCallRejectedCauseBusy(
                    callID: String?,
                    callee: ZegoCallUser?
                ) {
                    Log.d("CallEvents", "Outgoing call rejected (busy) by: ${callee?.name}, callID: $callID")
                }

                override fun onOutgoingCallDeclined(
                    callID: String?,
                    callee: ZegoCallUser?
                ) {
                    Log.d("CallEvents", "Outgoing call declined by: ${callee?.name}, callID: $callID")
                }

                override fun onOutgoingCallTimeout(
                    callID: String?,
                    callees: List<ZegoCallUser?>?
                ) {
                    val calleeNames = callees?.mapNotNull { it?.name }?.joinToString() ?: "unknown"
                    Log.d("CallEvents", "Outgoing call timeout for: $calleeNames, callID: $callID")
                }
            })
        }

    fun initZegoInviteService(appID: Long, appSign: String, userID: String, userName: String) {
        try {
            val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
            callInvitationConfig.translationText = ZegoTranslationText(ZegoUIKitLanguage.ENGLISH)
            callInvitationConfig.provider =
                ZegoUIKitPrebuiltCallConfigProvider { invitationData: ZegoCallInvitationData? ->
                    ZegoUIKitPrebuiltCallInvitationConfig.generateDefaultConfig(
                        invitationData
                    )
                }

            ZegoUIKitPrebuiltCallService.events.errorEventsListener =
                ErrorEventsListener { errorCode: Int, message: String ->
                    Log.e(TAGGY, "Error: code=$errorCode, message=$message")
                }

            ZegoUIKitPrebuiltCallService.events.invitationEvents.pluginConnectListener =
                SignalPluginConnectListener { state: ZIMConnectionState, event: ZIMConnectionEvent, extendedData: JSONObject ->
                    Log.d(TAGGY, "Connection state changed: state=$state, event=$event, extendedData=$extendedData")
                }

            ZegoUIKitPrebuiltCallService.init(
                application, appID, appSign, userID, userName, callInvitationConfig
            )

            ZegoUIKitPrebuiltCallService.enableFCMPush()

            ZegoUIKitPrebuiltCallService.events.callEvents.callEndListener =
                CallEndListener { callEndReason: ZegoCallEndReason?, jsonObject: String? ->
                    Log.d(
                        TAGGY,
                        "Call ended: reason=$callEndReason, data=$jsonObject"
                    )
                }

            Log.i(TAGGY, "Service initialized successfully for user: $userID")
        } catch (e: Exception) {
            Log.e(TAGGY, "Failed to initialize Zego service", e)
            // You might want to handle the exception based on your app's requirements
            // For example, show an error message to the user or try to recover
        }
    }



}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(viewModel : UserViewModel) {
    val users = viewModel.users.collectAsState()
    val currentUser = viewModel.currentUser
    val userState = viewModel.userState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.fetchAllUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Users") },
                actions = {
                    IconButton (onClick = { viewModel.signOut() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (userState) {
                is UserState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.Center)
                    )
                }

                is UserState.Error -> {
                    val errorMessage = (userState as UserState.Error).message
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchAllUsers() }) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Current User Card
                        currentUser.let { user ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Current User",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "Current User",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = user.username ,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = user.email ,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Users List Header
                        Text(
                            text = "All Users (${users.value.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Users List
                        if (users.value.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No users found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn {
                                items(users.value) { user ->
                                    // Skip showing current user in the list
                                    if (user.id != currentUser.id) {
                                        UserItem(
                                            user = user,
                                            onItemClick = {
                                                val intent = Intent(context, MainActivity3::class.java)
                                                intent.putExtra("userId", user.id)
                                                intent.putExtra("userName", user.username)
                                                intent.putExtra("userEmail", user.email)
                                                context.startActivity(intent)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun UserItem(user: User,onItemClick : () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onItemClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username.first().uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
