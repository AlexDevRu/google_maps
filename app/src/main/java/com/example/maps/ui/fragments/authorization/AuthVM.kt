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
    private val _openProfile = MutableLiveData<Boolean>()
    val openProfile: LiveData<Boolean> = _openProfile

    private val _openSignIn = MutableLiveData<Boolean>()
    val openSignIn: LiveData<Boolean> = _openSignIn

    fun openProfile() {
        _openProfile.value = true
    }

    fun openSignIn() {
        Firebase.auth.signOut()
        client.signOut()
        _openSignIn.value = true
    }
}