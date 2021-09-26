package com.example.maps.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maps.R
import com.example.maps.databinding.ViewholderStepBinding
import com.example.maps.utils.extensions.setHtml
import com.github.core.models.directions.Step

class StepAdapter(private val clickHandler: (Step) -> Unit)
    : ListAdapter<Step, StepAdapter.StepViewHolder>(StepDiffUtil()) {

    inner class StepViewHolder(private val binding: ViewholderStepBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(step: Step, last: Boolean) {
            if(!last) {
                binding.root.setBackgroundResource(R.drawable.step_viewholder_bg)
            }

            binding.distance.setHtml(step.distance.text)
            binding.duration.setHtml(step.duration.text)
            binding.instructions.setHtml(step.html_instructions)

            binding.root.setOnClickListener {
                clickHandler(step)
            }
        }
    }

    companion object {
        class StepDiffUtil : DiffUtil.ItemCallback<Step>() {
            override fun areItemsTheSame(oldItem: Step, newItem: Step): Boolean {
                return false
            }

            override fun areContentsTheSame(oldItem: Step, newItem: Step): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val binding = ViewholderStepBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return StepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        getItem(position).let { holder.bind(it, position == (itemCount - 1)) }
    }

}