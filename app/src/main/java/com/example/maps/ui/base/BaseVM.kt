package com.example.maps.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class BaseVM: ViewModel() {

    private val jobs = mapOf<Int, Job>()

    private var key = 0

    private fun launch() {
        val job = viewModelScope.launch {

        }
    }
}
