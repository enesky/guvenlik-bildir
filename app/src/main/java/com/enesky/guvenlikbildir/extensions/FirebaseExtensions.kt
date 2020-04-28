package com.enesky.guvenlikbildir.extensions

import android.app.Activity
import android.util.Log
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.model.Contact
import com.enesky.guvenlikbildir.model.User
import com.enesky.guvenlikbildir.others.Constants
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions

/**
 * Created by Enes Kamil YILMAZ on 31.01.2020
 */

fun Activity.signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
    App.mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
            Log.d("Login", "signInWithCredential:success")
            addUserInfo2Database(task.result?.user!!)
            this.openMainActivity()
        } else {
            Log.w("Login", "signInWithCredential:failure", task.exception)
            if (task.exception is FirebaseAuthInvalidCredentialsException)
                showToast("Hatalı kod. Tekrar deneyiniz.")
        }
    }
}

fun addUserInfo2Database(firebaseUser: FirebaseUser){
    App.mFirestore.collection(Constants.usersCollection)
        .document(firebaseUser.uid)
        .set(User(firebaseUser.uid, firebaseUser.phoneNumber!!))
        .addOnSuccessListener { documentReference ->
            Log.d("Firestore", "DocumentSnapshot added with document named: ${firebaseUser.uid}")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error adding document", e)
        }
}

fun checkIfNumberisRegistered(phoneNumber: String) {
    App.mFirestore.collection(Constants.usersCollection)
        .whereEqualTo(Constants.usersCollectionPhoneNumber, phoneNumber)
        .get()
        .addOnSuccessListener { documents ->
            for (document in documents) {
                Log.d("Firestore", "${document.id} => ${document.data}")
            }
        }
        .addOnFailureListener { exception ->
            Log.w("Firestore", "Error getting documents: ", exception)
        }
}

fun add2ContactList(contactList: MutableList<Contact>) : Boolean {
    var added = false
    val firebaseUser = App.mAuth.currentUser!!
    App.mFirestore.collection(Constants.usersCollection)
        .document(firebaseUser.uid)
        .set(User(firebaseUser.uid, firebaseUser.phoneNumber, contactList), SetOptions.merge())
        .addOnSuccessListener {
            Log.d("Firestore", "Contact list refreshed: ${firebaseUser.uid}")
            added = true
        }
        .addOnFailureListener {
            Log.w("Firestore", "Contact list couldn't refreshed", it)
        }
    return added
}

fun removeFromContactList(contact: Contact, function: () -> Unit) {
    App.mFirestore.collection(Constants.usersCollection)
        .document(App.mAuth.currentUser!!.uid)
        .update(Constants.usersContactList, FieldValue.arrayRemove(contact))
        .addOnSuccessListener {
            function()
            Log.d("Firestore", "$contact removed from contact list.")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Contact list couldn't refreshed", e)
        }
}

fun getUserInfo(uid: String?): User? {
    var user: User? = null
    App.mFirestore.collection(Constants.usersCollection)
        .document(App.mAuth.currentUser!!.uid)
        .get()
        .addOnSuccessListener {
            user = it.toObject(User::class.java)
            Log.d("Firestore", "getUserInfo-Success: $it")
        }
        .addOnFailureListener {
            Log.d("Firestore", "getUserInfo-Failure: ${it.message}")
        }

    return user
}

fun getUsersContactList(function: (any: Any) -> Unit) {
    App.mFirestore.collection(Constants.usersCollection)
        .document(App.mAuth.currentUser!!.uid)
        .get()
        .addOnSuccessListener {
            val user = it.toObject(User::class.java)
            function(user!!.contactList)
            Log.d("Firestore", "getUsersContactList-Success: $it")
        }
        .addOnFailureListener {
            function(it.message!!)
            Log.d("Firestore", "getUsersContactList-Failure: ${it.message}")
        }
}

fun doThingsIfListFilledOrNot(successFunction: () -> Unit,
                              failFunction: () -> Unit) {
    App.mFirestore.collection(Constants.usersCollection)
        .document(App.mAuth.currentUser!!.uid)
        .get()
        .addOnSuccessListener {
            if (it.toObject(User::class.java)?.contactList.isNullOrEmpty())
                failFunction()
            else
                successFunction()
            Log.d("Firestore", "doThingsIfListFilledOrNot-Success: $it")
        }
        .addOnFailureListener {
            Log.d("Firestore", "doThingsIfListFilledOrNot-Failure: ${it.message}")
        }
}
