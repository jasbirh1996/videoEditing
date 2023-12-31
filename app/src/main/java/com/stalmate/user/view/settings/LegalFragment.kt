package com.stalmate.user.view.settings

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.stalmate.user.databinding.FragmentLegalBinding

class LegalFragment : Fragment() {

    private var _binding: FragmentLegalBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLegalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.legalToolbar.tvhead.text = "Legal"
        binding.legalToolbar.topAppBar.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.constLegalTerm.setOnClickListener {
            startActivity(
                Intent(
                    this.requireContext(),
                    TermsAndConditionsAndPrivacyPolicyAndFaqs::class.java
                ).apply {
                    putExtra("comingFor", "0")
                })
        }
        binding.constPrivacyPolicy.setOnClickListener {
            startActivity(
                Intent(
                    this.requireContext(),
                    TermsAndConditionsAndPrivacyPolicyAndFaqs::class.java
                ).apply {
                    putExtra("comingFor", "1")
                })
        }
    }
}