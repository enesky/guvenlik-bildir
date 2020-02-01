package com.enesky.guvenlikbildir.utils

import android.app.Activity
import android.util.Log
import com.enesky.guvenlikbildir.App
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential

/**
 * Created by Enes Kamil YILMAZ on 31.01.2020
 */

fun Activity.signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
    App.managerAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
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
    App.managerFirestore.collection(Constants.usersCollection)
        .add(hashMapOf(
            Constants.usersCollectionUid to firebaseUser.uid,
            Constants.usersCollectionPhoneNumber to firebaseUser.phoneNumber
        ))
        .addOnSuccessListener { documentReference ->
            Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error adding document", e)
        }
}