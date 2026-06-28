package com.skyline.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.skyline.app.databinding.FragmentFlightsBinding

class FlightsFragment : Fragment() {

    private var _binding: FragmentFlightsBinding? = null
    private val binding get() = _binding!!

    private var isUpcomingTab = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()
        updateUI()
    }

    private fun setupTabs() {
        binding.tabUpcoming.setOnClickListener {
            isUpcomingTab = true
            updateUI()
        }

        binding.tabCompleted.setOnClickListener {
            isUpcomingTab = false
            updateUI()
        }
    }

    private fun updateUI() {
        if (isUpcomingTab) {
            // Active tab Upcoming
            binding.tabUpcoming.apply {
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_active)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            // Inactive tab Completed
            binding.tabCompleted.apply {
                background = null
                setTextColor(ContextCompat.getColor(requireContext(), R.color.skyline_text_secondary))
                setTypeface(null, android.graphics.Typeface.NORMAL)
            }
            binding.ivEmpty.setImageResource(R.drawable.checkticket_background1)
        } else {
            // Active tab Completed
            binding.tabCompleted.apply {
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_active)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            // Inactive tab Upcoming
            binding.tabUpcoming.apply {
                background = null
                setTextColor(ContextCompat.getColor(requireContext(), R.color.skyline_text_secondary))
                setTypeface(null, android.graphics.Typeface.NORMAL)
            }
            binding.ivEmpty.setImageResource(R.drawable.checkticket_background2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
