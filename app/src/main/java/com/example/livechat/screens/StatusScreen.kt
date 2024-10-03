package com.example.livechat.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.livechat.CommonProgressBar
import com.example.livechat.CommonRow
import com.example.livechat.Destination
import com.example.livechat.LCViewModel
import com.example.livechat.TitleText
import com.example.livechat.commonDivider
import com.example.livechat.navigateTo

@Composable
fun StatusScreen(navController: NavController, vm: LCViewModel) {
    val inProgress = vm.inProgressStatus.value
    if (inProgress) {
        CommonProgressBar()
    } else {
        val statuses = vm.status.value
        val userData = vm.userData.value
        val myStatuses = statuses.filter { it.userId == userData?.userId }
        val otherStatuses = statuses.filter { it.userId != userData?.userId }

        val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()){
            uri ->
            uri?.let {
                vm.uploadStatus(uri)
            }
        }

        Scaffold(floatingActionButton = {
            SFAB {
                launcher.launch("image/*")
            }

        },
            content = {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                ) {
                    TitleText(txt = "Status")
                    if (statuses.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No Status Available")
                        }
                    } else {
                        if (myStatuses.isNotEmpty()) {
                            CommonRow(
                                imageUrl = userData?.imageUrl ?: "",
                                name = userData?.name ?: "",
                            ) {
                                navigateTo(
                                    navController = navController,
                                    Destination.SingleStatus.createRoute(myStatuses[0].userId!!)
                                )
                            }
                            commonDivider()
                            val uniqueUsers = otherStatuses.map { it.userId }.toSet().toList()
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(uniqueUsers.size) { user ->
                                    CommonRow(
                                        imageUrl = userData?.imageUrl ?: "",
                                        name = userData?.name ?: "",
                                    ){
                                        navigateTo(
                                            navController = navController,
                                            Destination.SingleStatus.createRoute(uniqueUsers[user]!!)
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

                }

            }

        )
    }

}

@Composable
fun SFAB(onFabClick: () -> Unit) {
    FloatingActionButton(
        onClick = onFabClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit, contentDescription = "Add Status",
            tint = Color.White
        )

    }

}