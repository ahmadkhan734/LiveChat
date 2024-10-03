package com.example.livechat.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.livechat.CommonImage
import com.example.livechat.LCViewModel
import com.example.livechat.commonDivider
import com.example.livechat.data.Message

@Composable
fun SingleChatScreen(navController: NavController, vm: LCViewModel, chatId: String) {
    var reply by remember {
        mutableStateOf("")
    }

    val onSendReply = {
        vm.onSendReply(reply, chatId)
        reply = ""
    }
    var chatMessage = vm.chatMessages
   val myUser = vm.userData.value
    val currentChat = vm.chats.value.first { it.chatId == chatId }
    val chatUser = if (myUser?.userId == currentChat.user1.userId) currentChat.user2 else currentChat.user1

    LaunchedEffect(key1 = Unit){
        vm.populateMessages(chatId)
    }
    BackHandler {
        vm.depopulateMessages()
//        navController.popBackStack()
    }

Column(){
        ChatHeader(name = chatUser.name?: "", imageUrl = chatUser.imageUrl?: ""){
            navController.popBackStack()
            vm.depopulateMessages()
        }
            MessageBox(modifier = Modifier.weight(1f),
                chatMessage = chatMessage.value,
                currentUserId = myUser?.userId?: "")

    ReplyBox(
        reply =reply,
        onReplyChange = {reply = it},
        onSendReply = onSendReply
    )

    }

}



@Composable
fun MessageBox(modifier: Modifier,chatMessage: List<Message>,currentUserId: String){
    LazyColumn(modifier = modifier){
        items(chatMessage){
            msg ->
            val alignment = if (msg.sendBy == currentUserId) Alignment.End else Alignment.Start
            val color = if (msg.sendBy == currentUserId) Color(0xFFA4DDB3) else Color(0xFFF6FAF4)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = alignment
            ){
                Text(text = msg.message?: "",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .padding(12.dp),
                    color = Color.Black,
                    fontSize = 16.sp)
            }
        }
    }
}


@Composable
fun ChatHeader(name: String, imageUrl: String, onBackClick: () -> Unit){
    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,){
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier
            .clickable {
                onBackClick.invoke()
            }
            .padding(8.dp)
        )

      CommonImage(data = imageUrl,
          modifier = Modifier
              .padding(8.dp)
              .size(50.dp)
              .clip(CircleShape)
              .background(Color.Gray))
        Text(text = name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))

    }

}

@Composable
fun ReplyBox(reply: String, onReplyChange: (String) -> Unit, onSendReply: () -> Unit){
    Column(modifier = Modifier.fillMaxWidth()){
        commonDivider()
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween){
            TextField(
                value = reply, onValueChange = onReplyChange, maxLines = 3,
            )
            Button(onClick = onSendReply ) {
                Text(text = "Send")
            }

        }

    }

}