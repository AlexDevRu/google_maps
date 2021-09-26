package com.example.maps.ui.fragments.authorization.sign_in

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.maps.databinding.FragmentSignInBinding
import com.example.maps.ui.fragments.base.BaseFragment
import com.example.maps.utils.extensions.hide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SignInFragment: BaseFragment<FragmentSignInBinding>(FragmentSignInBinding::inflate) {

    private val viewModel by sharedViewModel<SignInVM>()
    private val client by inject<GoogleSignInClient>()

    private lateinit var signInResultLauncher: ActivityResultLauncher<Intent>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signInResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            this::handleSignInResult
        )

        binding.signInButton.setOnClickListener {
            val signInIntent = client.signInIntent
            signInResultLauncher.launch(signInIntent)
        }

        showError(viewModel.signInError)
    }

    private fun handleSignInResult(result: ActivityResult) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w("asd", "signInResult:failed code=" + e.statusCode)
            showError(e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("asd", "signInWithCredential:success")
                    globalVM.checkSignIn()
                    val action = SignInFragmentDirections.actionSignInFragmentToProfileFragment()
                    findNavController().navigate(action)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("asd", "signInWithCredential:failure", task.exception)
                    showError(task.exception)
                }
            }
    }

    private fun showError(exception: Exception?) {
        if(exception == null) {
            binding.signInError.hide()
            return
        }
        viewModel.signInError = exception
        binding.signInError.text = exception.message
    }
}