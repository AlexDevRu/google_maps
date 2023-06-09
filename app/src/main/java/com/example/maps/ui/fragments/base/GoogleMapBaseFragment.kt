package com.example.maps.ui.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.viewbinding.ViewBinding
import com.example.maps.ui.GlobalVM
import com.example.maps.ui.MapsActivity
import com.example.maps.utils.InternetUtil
import com.github.googlemapfragment.android.GoogleMapsFragment
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable

abstract class GoogleMapBaseFragment<TBinding: ViewBinding>(
    @IdRes private val mapFragmentId: Int,
    private val inflate: Inflate<TBinding>
): GoogleMapsFragment(mapFragmentId) {

    protected val compositeDisposable = CompositeDisposable()

    protected lateinit var binding: TBinding

    protected lateinit var internetObserver: InternetUtil

    protected val globalVM: GlobalVM
        get() = (requireActivity() as MapsActivity).globalVM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate.invoke(inflater, container, false)
        internetObserver = InternetUtil(requireContext())
        return binding.root
    }

    protected fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}