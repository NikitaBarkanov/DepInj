package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentImageInSeparateBinding
import ru.netology.nmedia.handler.loadAttachmentImage
import ru.netology.nmedia.util.SeparateImageArg

class SeparateImageFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by SeparateImageArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentImageInSeparateBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.textArg?.let(binding.preview::loadAttachmentImage)

        binding.backToPostImage.setOnClickListener {
            findNavController().navigate(R.id.action_separateImageFragment_to_feedFragment)
        }

        return binding.root
    }
}