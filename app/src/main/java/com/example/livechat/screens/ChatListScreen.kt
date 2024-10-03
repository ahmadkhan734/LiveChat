package com.example.livechat.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.livechat.CommonProgressBar
import com.example.livechat.CommonRow
import com.example.livechat.Destination
import com.example.livechat.LCViewModel
import com.example.livechat.TitleText
import com.example.livechat.navigateTo

@Composable
fun ChatListScreen(navController: NavController, vm: LCViewModel) {
    val inProgress = vm.inProcessChats
    if (inProgress.value) {
        CommonProgressBar()
    } else {
        val chats = vm.chats.value
        val userData = vm.userData.value
        val showDialog = remember {
            mutableStateOf(false)
        }
        val onFabClick: () -> Unit = { showDialog.value = true }
        val onDismiss: () -> Unit = { showDialog.value = false }
        val onAddChat: (String) -> Unit = {
            vm.onAddChat(it)
            showDialog.value = false
        }
        Scaffold(
            floatingActionButton = {
                FAB(
                    onFabClick = onFabClick,
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    TitleText(txt = "Chats")
                    if (chats.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No Chats Available")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(chats) { chat ->
                                val chatUser = if (chat.user1.userId == userData?.userId) {
                                    chat.user2
                                } else {
                                    chat.user1
                                }
                                CommonRow(imageUrl = chatUser.imageUrl, name = chatUser.name) {
                                    chat.chatId?.let {
                                        navigateTo(
                                            navController,
                                            Destination.SingleChat.createRoute(id = it)
                                        )

                                    }
                                }

                            }
                        }
                    }
                    Box(
                        modifier = Modifier.wrapContentHeight(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        BottomNavigationMenu(
                            selectedItem = BottomNavigationItem.CHATLIST,
                            navController = navController
                        )
                    }
                    if (showDialog.value) {
                        AddChatDialog(
//            showDialog = showDialog.value,
                            onDismiss = onDismiss,
                            onAddChat = onAddChat
                        )
                    }
                }

            }


        )


    }


}

@Composable
fun AddChatDialog(
//    showDialog: Boolean,
    onDismiss: () -> Unit,
    onAddChat: (String) -> Unit
) {
    val AddChatNumber = remember {
        mutableStateOf("")
    }
    AlertDialog(onDismissRequest = {
        onDismiss.invoke()
        AddChatNumber.value = ""
    },
        confirmButton = {
            Button(onClick = {
                onAddChat(AddChatNumber.value)
            }) {
                Text(text = "Add Chat")
            }
        },
        title = { Text(text = "Add Chat") },
        text = {
            OutlinedTextField(
                value = AddChatNumber.value,
                onValueChange = { AddChatNumber.value = it },
                placeholder = { Text(text = "Enter Chat Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    )

}


@Composable
fun FAB(onFabClick: () -> Unit) {
    FloatingActionButton(
        onClick = onFabClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add, contentDescription = null,
            tint = Color.White
        )

    }

}
