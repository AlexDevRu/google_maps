package com.example.maps.ui.fragments.sign_in

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class SignInVM @Inject constructor(

): ViewModel() {

    var signInError: Exception? = null
}