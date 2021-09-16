package com.example.maps.ui.fragments.authorization

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthVM @Inject constructor(
    private val client: GoogleSignInClient
): ViewModel() {

    private val _isSignedIn = MutableLiveData(false)
    val isSignedIn: LiveData<Boolean> = _isSignedIn

    fun checkSignIn() {
        _isSignedIn.value = Firebase.auth.currentUser != null
    }

    fun signOut() {
        Firebase.auth.signOut()
        client.signOut()
        _isSignedIn.value = false
    }

    /*private val _openProfile = MutableLiveData(false)
    val openProfile: LiveData<Boolean> = _openProfile

    private val _openSignIn = MutableLiveData(false)
    val openSignIn: LiveData<Boolean> = _openSignIn*/

    /*fun openProfile() {
        _openSignIn.value = false
        _openProfile.value = true
    }

    fun openSignIn() {
        Firebase.auth.signOut()
        client.signOut()
        _openProfile.value = false
        _openSignIn.value = true
    }*/
}