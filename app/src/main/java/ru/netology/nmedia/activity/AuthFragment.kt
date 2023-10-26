package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentAuthBinding
import ru.netology.nmedia.viewmodel.SignInViewModel

class AuthFragment : Fragment() {

    private val viewModel: SignInViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAuthBinding.inflate(layoutInflater)

        binding.signInButton.setOnClickListener {
            val login = binding.loginTextField.editText?.text.toString()
            val password = binding.passwordTextField.editText?.text.toString()
            viewModel.saveIdAndToken(login, password)
            findNavController().navigate(R.id.action_authFragment_to_feedFragment)
        }

        return binding.root
    }
}