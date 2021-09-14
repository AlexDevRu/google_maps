package com.example.maps.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.maps.R
import com.example.maps.databinding.LayoutRecyclerviewBinding
import com.example.maps.utils.extensions.hide
import com.example.maps.utils.extensions.show


class CustomRecyclerView @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutRecyclerviewBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = LayoutRecyclerviewBinding.inflate(inflater, this, true)
        initAttrs()
    }

    private fun initAttrs() {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CustomRecyclerView, defStyleAttr, 0)

        val layoutManagerStr = a.getString(R.styleable.CustomRecyclerView_layoutManager)

        layoutManager = when(layoutManagerStr) {
            "grid" -> {
                val columnCount = a.getInteger(R.styleable.CustomRecyclerView_columnCount, 1)
                GridLayoutManager(context, columnCount)
            }
            else -> LinearLayoutManager(context)
        }

        swipeToRefreshEnabled = false

        a.recycle()
    }

    var isLoading: Boolean = false
        set(value) {
            field = value
            if(field) {
                binding.progressBar.show()
                binding.recyclerView.hide()
            }
            else {
                binding.progressBar.hide()
                binding.recyclerView.show()
            }
        }

    var layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        set(value) {
            field = value
            binding.recyclerView.layoutManager = field
        }

    var errorMessage: String? = null
        set(value) {
            field = value
            if(field.isNullOrEmpty()) {
                binding.recyclerView.show()
                binding.error.root.hide()
            }
            else {
                binding.progressBar.hide()
                binding.recyclerView.hide()
                binding.error.root.show()
                binding.error.errorMessage.text = field
            }
        }

    var swipeToRefreshEnabled: Boolean = false
        set(value) {
            field = value
            if(!field) {
                binding.swipeLayout.isRefreshing = false
            }
            binding.swipeLayout.isEnabled = field
        }

    var retryHandler: () -> Unit = {}
        set(value) {
            field = value
            binding.swipeLayout.setOnRefreshListener {
                field.invoke()
                binding.swipeLayout.isRefreshing = false
            }
            binding.error.retryButton.setOnClickListener {
                field.invoke()
            }
        }

    var adapter: RecyclerView.Adapter<*>? = null
        set(value) {
            field = value
            binding.recyclerView.adapter = field
        }

    fun prepareToSharedTransition(fragment: Fragment) {
        binding.recyclerView.apply {
            fragment.postponeEnterTransition()
            viewTreeObserver
                .addOnPreDrawListener {
                    fragment.startPostponedEnterTransition()
                    true
                }
        }
    }

    fun scrollToTop() {
        binding.recyclerView.scrollToPosition(0)
    }
}