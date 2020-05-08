package com.enesky.guvenlikbildir.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.model.Contact
import com.enesky.guvenlikbildir.model.User
import com.enesky.guvenlikbildir.others.Constants
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import timber.log.Timber

/**
 * Created by Enes Kamil YILMAZ on 31.01.2020
 */

fun Activity.signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
    App.mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
            Timber.tag("Login").d("signInWithCredential:success")
            val data = Bundle()
            data.putString("phone", task.result?.user?.phoneNumber)
            App.mAnalytics.setUserId(task.result?.user?.uid)
            App.mAnalytics.logEvent("signInWithPhoneAuthCredential", data)
            App.mCrashlytics.setUserId(task.result?.user?.phoneNumber!!)
            addUserInfo2Database(task.result?.user!!)
            this.openMainActivity()
        } else {
            Timber.w(task.exception, "signInWithCredential:failure")
            if (task.exception is FirebaseAuthInvalidCredentialsException)
                showToast("Hatalı kod. Tekrar deneyiniz.")
        }
    }
}

fun addUserInfo2Database(firebaseUser: FirebaseUser){
    App.mFirestore.collection(Constants.usersCollection)
        .document(firebaseUser.uid)
        .set(User(firebaseUser.uid, firebaseUser.phoneNumber!!))
        .addOnSuccessListener { _ ->
            Timber.tag("Firestore").d("DocumentSnapshot added with document named: %s", firebaseUser.uid)
        }
        .addOnFailureListener { e ->
            Timber.w(e, "Error adding document")
        }
}

fun checkIfNumberisRegistered(phoneNumber: String) {
    App.mFirestore.collection(Constants.usersCollection)
        .whereEqualTo(Constants.usersCollectionPhoneNumber, phoneNumber)
        .get()
        .addOnSuccessListener { documents ->
            for (document in documents) {
                Timber.tag("Firestore").d("%1s => %2s", document.id, document.data)
            }
        }
        .addOnFailureListener { exception ->
            Timber.w(exception, "Error getting documents: ")
        }
}

@SuppressLint("TimberArgCount")
fun add2ContactList(contactList: MutableList<Contact>) : Boolean {
    var added = false
    val firebaseUser = App.mAuth.currentUser!!
    App.mFirestore.collection(Constants.usersCollection)
        .document(firebaseUser.uid)
        .set(User(firebaseUser.uid, firebaseUser.phoneNumber, contactList), SetOptions.merge())
        .addOnSuccessListener {
            Timber.tag("Firestore").d("Contact list refreshed: %s", firebaseUser.uid)
            added = true
        }
        .addOnFailureListener {
            Timber.tag("Firestore").w(it, "Contact list couldn't refreshed, %s")
        }
    return added
}

@SuppressLint("TimberArgCount")
fun removeFromContactList(contact: Contact, function: () -> Unit) {
    App.mFirestore.collection(Constants.usersCollection)
        .document(App.mAuth.currentUser!!.uid)
        .update(Constants.usersContactList, FieldValue.arrayRemove(contact))
        .addOnSuccessListener {
            function()
            Timber.tag("Firestore").d("%s removed from contact list.", contact)
        }
        .addOnFailureListener { e ->
            Timber.tag("Firestore").w(e, "Contact list couldn't refreshed. %s")
        }
}

fun getUserInfo(uid: String?): User? {
    var user: User? = null
    App.mFirestore.collection(Constants.usersCollection)
        .document(App.mAuth.currentUser!!.uid)
        .get()
        .addOnSuccessListener {
            user = it.toObject(User::class.java)
            Timber.tag("Firestore").d("getUserInfo-Success: %s", it)
        }
        .addOnFailureListener {
            Timber.tag("Firestore").d("getUserInfo-Failure: %s", it.message)
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
            Timber.tag("Firestore").d("getUsersContactList-Success: %s", it)
        }
        .addOnFailureListener {
            function(it.message!!)
            Timber.tag("Firestore").d("getUsersContactList-Failure: %s", it.message)
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
            Timber.tag("Firestore").d("doThingsIfListFilledOrNot-Success: %s", it)
        }
        .addOnFailureListener {
            Timber.tag("Firestore").d("doThingsIfListFilledOrNot-Failure: %s", it.message)
        }
}
