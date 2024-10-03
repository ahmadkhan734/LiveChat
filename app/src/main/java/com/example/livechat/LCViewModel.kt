package com.example.livechat

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.livechat.data.CHATS
import com.example.livechat.data.ChatData
import com.example.livechat.data.ChatUser
import com.example.livechat.data.Events
import com.example.livechat.data.MESSAGES
import com.example.livechat.data.Message
import com.example.livechat.data.STATUS
import com.example.livechat.data.Status
import com.example.livechat.data.USER_NODE
import com.example.livechat.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LCViewModel @Inject constructor(
    val auth: FirebaseAuth,
    var db: FirebaseFirestore,
    val storage: FirebaseStorage

) : ViewModel() {

    var inProcess = mutableStateOf(false)
    var inProcessChats = mutableStateOf(false)
    val eventMutableState = mutableStateOf<Events<String>?>(null)
    var signIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val inProgressChatMessages = mutableStateOf(false)
    var currentChatMessageListener: ListenerRegistration? = null

    val status = mutableStateOf<List<ChatUser>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun onSendReply(message: String, chatId: String) {
        val time = Calendar.getInstance().time.toString()
        val msg = Message(userData.value?.userId, message, time)
        db.collection(CHATS).document(chatId).collection(MESSAGES).add(msg)
    }

    fun populateMessages(chatId: String) {
        inProgressChatMessages.value = true
        currentChatMessageListener = db.collection(CHATS).document(chatId).collection(MESSAGES)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    chatMessages.value = value.documents.mapNotNull {
                        it.toObject<Message>()
                    }.sortedBy { it.timestamp }
                    inProgressChatMessages.value = false
                }
            }
    }

    fun depopulateMessages() {
        chatMessages.value = listOf()
        currentChatMessageListener = null
    }


    fun populateChats() {
        inProcessChats.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter
                    .equalTo("user1.userId", userData.value?.userId),
                Filter
                    .equalTo("user2.userId", userData.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }
            if (value != null) {
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }
                inProcessChats.value = false
            }
        }
    }


    fun signUp(email: String, password: String, name: String, number: String) {
        inProcess.value = true
        if (name.isEmpty() or number.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill all the fields")
            return
        }

        inProcess.value = true
        db.collection(USER_NODE).whereEqualTo("number", number).get().addOnSuccessListener {
            if (it.isEmpty) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        signIn.value = true
                        createOrUpdateProfile(name, number)
                    } else {
                        handleException(it.exception, customMessage = "signUp Failed")
                        inProcess.value = false

                    }
                }
            } else {
                handleException(customMessage = "User Already Exists")
                inProcess.value = false
            }
        }

    }

    fun login(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill all the fields")
            return
        } else {
            inProcess.value = true
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    signIn.value = true
                    inProcess.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                } else {
                    handleException(it.exception, customMessage = "Login Failed")
                    inProcess.value = false
                }
            }
        }

    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageurl = it.toString())

        }

    }

    fun uploadImage(uri: Uri, onSuccess: (uri: Uri) -> Unit) {
        inProcess.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val upLoadTask = imageRef.putFile(uri)
        upLoadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSuccess)
            inProcess.value = false
        }
            .addOnFailureListener {
                handleException(it)
            }
    }

    fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        imageurl: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            number = number ?: userData.value?.number,
            imageUrl = imageurl ?: userData.value?.imageUrl

        )
        uid?.let {
            inProcess.value = true
            db.collection(USER_NODE).document(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    db.collection(USER_NODE).document(uid).update(userData.toMap())
                        .addOnSuccessListener {
                            inProcess.value = false
                            getUserData(uid)
                        }

                } else {
                    db.collection(USER_NODE).document(uid).set(userData)
                    inProcess.value = false
                    getUserData(uid)

                }
            }
                .addOnFailureListener {
                    handleException(it, "Cannot Retrieve User Data")
                }
        }

    }

    private fun getUserData(uid: String) {
        inProcess.value = true
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error, "Cannot Retrieve User Data")
            }
            if (value != null) {
                var user = value.toObject<UserData>()
                userData.value = user
                inProcess.value = false
                populateChats()
                populateStatuses()

            }

        }
    }


    fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("LiveChat", "LiveChatException: ", exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isNullOrEmpty()) errorMsg else "$customMessage: $errorMsg"

        eventMutableState.value = Events(message)
        inProcess.value = false
    }

    fun logout() {
        auth.signOut()
        signIn.value = false
        userData.value = null
        depopulateMessages()
        currentChatMessageListener = null
        eventMutableState.value = Events("Logged Out")
    }

    fun onAddChat(number: String) {
        // Ensure input is valid
        if (number.isEmpty() || !number.isDigitsOnly()) {
            handleException(customMessage = "Number must contain digits only")
            return
        }

        // Check if a chat already exists
        db.collection(CHATS).where(
            Filter.or(
                Filter.and(
                    Filter.equalTo("user1.number", number),
                    Filter.equalTo("user2.number", userData.value?.number)
                ),
                Filter.and(
                    Filter.equalTo("user1.number", userData.value?.number),
                    Filter.equalTo("user2.number", number)
                )
            )
        ).get().addOnSuccessListener { chatSnapshot ->
            if (chatSnapshot.isEmpty) {
                // If chat doesn't exist, check if the number is a valid user
                db.collection(USER_NODE).whereEqualTo("number", number).get()
                    .addOnSuccessListener { userSnapshot ->
                        if (userSnapshot.isEmpty) {
                            // If no user found with that number
                            handleException(customMessage = "Number not found")
                            return@addOnSuccessListener
                        } else {
                            // Get the user data
                            val chatPartner = userSnapshot.toObjects<UserData>()[0]
                            val id = db.collection(CHATS).document().id
                            val chat = ChatData(
                                chatId = id,
                                user1 = ChatUser(
                                    userData.value?.userId,
                                    userData.value?.name,
                                    userData.value?.imageUrl,
                                    userData.value?.number
                                ),
                                user2 = ChatUser(
                                    chatPartner.userId,
                                    chatPartner.name,
                                    chatPartner.imageUrl,
                                    chatPartner.number
                                )
                            )

                            // Add the chat to Firestore
                            db.collection(CHATS).document(id).set(chat)
                                .addOnSuccessListener {
                                    // Chat added successfully, handle UI updates here if needed
                                }
                                .addOnFailureListener { exception ->
                                    handleException(exception)
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        handleException(exception)
                    }
            } else {
                // Chat already exists
                handleException(customMessage = "Chat Already Exists")
            }
        }.addOnFailureListener { exception ->
            handleException(exception)
        }
    }

    fun uploadStatus(uri: Uri) {
        uploadImage(uri) {
            createStatus(it.toString())
        }
    }

    fun createStatus(imageurl: String) {
        val newStatus = Status(
            ChatUser(
                userData.value?.userId,
                userData.value?.name,
                userData.value?.imageUrl,
                userData.value?.number
            ),
            imageurl,
            System.currentTimeMillis()
        )
        db.collection(STATUS).add(newStatus)

    }

    fun populateStatuses() {
        val timeDelta = 24L * 60L * 60L * 1000L
        val cutOff = System.currentTimeMillis() - timeDelta
        inProgressStatus.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }
            if (value != null) {
                val currentConnections = arrayListOf(userData.value?.userId)
                val chats = value.toObjects<ChatData>()
                chats.forEach{
                    chat ->
                    if (chat.user1.userId == userData.value?.userId) {
                        currentConnections.add(chat.user2.userId)
                    }
                    else{
                        currentConnections.add(chat.user1.userId)
                    }
                    db.collection(STATUS).whereGreaterThan("timestamp", cutOff).whereIn("user.userId",
                        currentConnections).addSnapshotListener { value, error ->
                            if (error != null) {
                        handleException(error)
                    }

                        if (value != null) {
                        status.value = value.toObjects()
                        inProgressStatus.value = false
                    }
                    }
                }

            }
        }


    }
}

