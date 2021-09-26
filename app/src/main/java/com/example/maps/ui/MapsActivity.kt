package com.example.maps.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.maps.R
import com.example.maps.databinding.ActivityMapsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel


class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding

    val globalVM by viewModel<GlobalVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        globalVM.isSignedIn.observe(this) {
            binding.bottomNavigationView.menu.findItem(R.id.signInFragment).isVisible = !it
            binding.bottomNavigationView.menu.findItem(R.id.markdownFragment).isVisible = it
            binding.bottomNavigationView.menu.findItem(R.id.profileFragment).isVisible = it
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility = when(destination.id) {
                R.id.fullPhotoFragment -> View.GONE
                else -> View.VISIBLE
            }
        }
    }
}