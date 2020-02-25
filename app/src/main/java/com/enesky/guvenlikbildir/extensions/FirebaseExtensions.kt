package com.enesky.guvenlikbildir.extensions

import android.app.Activity
import android.util.Log
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.model.Contact
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import java.lang.reflect.Type

/**
 * Created by Enes Kamil YILMAZ on 31.01.2020
 */

data class User(
    val uid: String? = null,
    val phoneNumber: String? = null,
    val contactList: List<Contact>? = null
)

fun Activity.signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
    App.mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
            Log.d("Login", "signInWithCredential:success")
            addUserInfo2Database(task.result?.user!!)
            this.openMainActivity()
        } else {
            Log.w("Login", "signInWithCredential:failure", task.exception)
            if (task.exception is FirebaseAuthInvalidCredentialsException)
                showToast("Hatalı kod")
        }
    }
}

fun addUserInfo2Database(firebaseUser: FirebaseUser){
    App.mFirestore.collection(Constants.usersCollection)
        .add(User(firebaseUser.uid, firebaseUser.phoneNumber))
        .addOnSuccessListener { documentReference ->
            Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error adding document", e)
        }
}

fun checkIfNumberisRegistered(phoneNumber: String) {
    App.mFirestore.collection(Constants.usersCollection)
        .whereEqualTo(Constants.usersCollectionPhoneNumber, phoneNumber).get()
        .addOnSuccessListener { documents ->
            for (document in documents) {
                Log.d("Firestore", "${document.id} => ${document.data}")
            }
        }
        .addOnFailureListener { exception ->
            Log.w("Firestore", "Error getting documents: ", exception)
        }
}

fun addContactList(contactList: MutableList<Contact>, firebaseUser: FirebaseUser) {
    App.mFirestore.collection(Constants.usersCollection)
        .add(User(firebaseUser.uid, firebaseUser.phoneNumber, contactList))
        .addOnSuccessListener { documentReference ->
            Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error adding document", e)
        }
}

fun getUserInfo(uid: String?): User? {
    var user: User? = null

    App.mFirestore.collection(Constants.usersCollection)
        .whereEqualTo(Constants.usersCollectionUid, uid)
        .get()
        .addOnSuccessListener {
            if (!it.isEmpty) {
                val userList: List<User> = it.toObjects(User::class.java)
                user = userList[0]
            }
            Log.d("Firestore", "getUserInfo-Success: $it")
        }
        .addOnFailureListener {
            Log.d("Firestore", "getUserInfo-Failure: ${it.message}")
        }

    return user
}