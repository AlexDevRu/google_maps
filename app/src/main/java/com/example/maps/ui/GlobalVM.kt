package com.example.maps.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class GlobalVM (
    private val client: GoogleSignInClient
): ViewModel() {

    private val _isSignedIn = MutableLiveData(false)
    val isSignedIn: LiveData<Boolean> = _isSignedIn

    init {
        checkSignIn()
    }

    fun checkSignIn() {
        _isSignedIn.value = Firebase.auth.currentUser != null
    }

    fun signOut() {
        Firebase.auth.signOut()
        client.signOut()
        _isSignedIn.value = false
    }
}