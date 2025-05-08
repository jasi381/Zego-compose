package com.jasmeet.myapplication.home

import android.util.Log
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

import com.google.firebase.auth.FirebaseAuth
import com.jasmeet.myapplication.Creds
import com.jasmeet.myapplication.MainActivity
import com.jasmeet.myapplication.MainApp
import com.jasmeet.myapplication.R
import com.zegocloud.uikit.prebuilt.call.config.ZegoHangUpConfirmDialogInfo
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig
import com.zegocloud.uikit.prebuilt.call.core.invite.advanced.ZegoCallInvitationInCallingConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig.generateDefaultConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallType
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallUser
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInnerText
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInvitationCallListener
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val act = context  as MainActivity
    val app = context.applicationContext as MainApp


    LaunchedEffect(Unit) {

        val user = FirebaseAuth.getInstance().currentUser

        // Create and configure the invitation config
        val invitationConfig = ZegoUIKitPrebuiltCallInvitationConfig().apply {
            // Set basic properties
            showDeclineButton = true
            endCallWhenInitiatorLeave = true

            // Configure ringtones (optional)
            // incomingCallRingtone = "your_ringtone_path"
            // outgoingCallRingtone = "your_ringtone_path"

            // Configure backgrounds (optional)
             incomingCallBackground = ContextCompat.getDrawable(context, R.drawable.img)
            outgoingCallBackground = ContextCompat.getDrawable(context, R.drawable.img_1)

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
              app,
             Creds.appID,
            Creds.appSign,
            user?.email,
            user?.email,
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


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val targetUserId = remember {
            mutableStateOf("")
        }
        Text(text = "UserID : ${FirebaseAuth.getInstance().currentUser?.email}")
        OutlinedTextField(
            value = targetUserId.value,
            onValueChange = { targetUserId.value = it },
            label = { Text(text = "Add user email") },
            modifier = Modifier.fillMaxWidth()
        )
        Row {
            CallButton(isVideoCall = false) { button ->
                if (targetUserId.value.isNotEmpty()) button.setInvitees(
                    mutableListOf(
                        ZegoUIKitUser(
                            targetUserId.value, targetUserId.value
                        )
                    )
                )
            }
            CallButton(isVideoCall = true) { button ->
                if (targetUserId.value.isNotEmpty()) button.setInvitees(
                    mutableListOf(
                        ZegoUIKitUser(
                            targetUserId.value, targetUserId.value
                        )
                    )
                )
            }
        }
    }
}

@Composable
fun CallButton(isVideoCall: Boolean, onClick: (ZegoSendCallInvitationButton) -> Unit) {
    AndroidView(factory = { context ->
        val button = ZegoSendCallInvitationButton(context)
        button.setIsVideoCall(isVideoCall)
        button.resourceID = "zego_data"
        button
    }, modifier = Modifier.size(50.dp)) { zegoCallButton ->
        zegoCallButton.setOnClickListener { _ -> onClick(zegoCallButton) }
    }
}