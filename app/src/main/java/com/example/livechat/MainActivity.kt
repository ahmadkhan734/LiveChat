package com.example.livechat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.livechat.screens.ChatListScreen
import com.example.livechat.screens.LoginScreen
import com.example.livechat.screens.ProfileScreen
import com.example.livechat.screens.SignUpScreen
import com.example.livechat.screens.SingleChatScreen
import com.example.livechat.screens.SingleStatusScreen
import com.example.livechat.screens.StatusScreen
import com.example.livechat.ui.theme.LiveChatTheme
import dagger.hilt.android.AndroidEntryPoint


sealed class Destination(val route: String){
    object SignUp: Destination("signup")
    object Login: Destination("login")
    object Profile: Destination("profile")
    object ChatList: Destination("chatList")
    object SingleChat: Destination("singleChat/{chatId}"){
        fun createRoute(id: String) = "singleChat/$id"
    }

    object StatusList: Destination("statusList")
    object SingleStatus: Destination("singleStatus/{userId}"){
        fun createRoute(userId: String) = "singleChat/$userId"
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiveChatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()
                    .padding(top = 32.dp, bottom = 16.dp)) { innerPadding ->

                    ChatAppNavigation()

                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun ChatAppNavigation() {

    val navController = rememberNavController()
    val vm = hiltViewModel<LCViewModel>()

    NavHost(navController = navController, startDestination = Destination.SignUp.route) {
        composable(Destination.SignUp.route) {
            SignUpScreen(navController, vm)
        }
        composable(Destination.Login.route) {
            LoginScreen(vm = vm, navController = navController)
        }
        composable(Destination.ChatList.route) {
            ChatListScreen(navController, vm)
        }
        composable(Destination.SingleChat.route) {
            val chatId = it.arguments?.getString("chatId")
            chatId?.let {
                SingleChatScreen(navController, vm, chatId)
            }
        }
        composable(Destination.StatusList.route) {
            StatusScreen(navController, vm)
        }
        composable(Destination.Profile.route) {
            ProfileScreen(navController, vm)
        }
        composable(Destination.SingleStatus.route) {
            val userId = it.arguments?.getString("userId")
            userId?.let {
                SingleStatusScreen(navController = navController, vm = vm, userId = it)
            }
        }

    }
}

