package com.example.maps.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding


typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<TBinding: ViewBinding>(
    private val inflate: Inflate<TBinding>
): Fragment() {

    protected lateinit var binding: TBinding

    //protected lateinit var internetObserver: InternetUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate.invoke(inflater, container, false)
        //internetObserver = InternetUtil(requireContext())
        return binding.root
    }
}