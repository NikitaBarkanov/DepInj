package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.viewmodel.RegistrationViewModel


class RegistrationFragment : Fragment() {

    private val viewModel: RegistrationViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentRegistrationBinding.inflate(inflater)

        binding.signUpButton.setOnClickListener {
            val login = binding.nameTextField.editText?.text.toString()
            val pass = binding.regPasswordTextField.editText?.text.toString()
            val name = binding.nameTextField.editText?.text.toString()

            viewModel.registerImage.observe(viewLifecycleOwner) { media ->
                if(media != null) {
                    viewModel.saveUserWithRegister(login, pass, name, media.file)
                }
            }

            findNavController().navigate(R.id.action_registrationFragment_to_feedFragment)
        }

        return binding.root
    }
}