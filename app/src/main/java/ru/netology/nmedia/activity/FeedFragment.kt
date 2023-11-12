package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.BuildConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.Auth.AppAuth
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostLoadingStateAdapter
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment() : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    @OptIn(ExperimentalCoroutinesApi::class)
    private val viewModel: PostViewModel by viewModels()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val photoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = requireNotNull(it.data?.data)
                viewModel.setPhoto(PhotoModel(uri = uri, file = uri.toFile()))
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.pick_photo_error,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    }
                )
            }
            override fun onShowImageAsSeparate(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_separateImageFragment,
                    Bundle().apply {
                        textArg = "${"http://10.0.2.2:9999/"}media/${post.attachment?.url}"
                    }
                )
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }
            override fun onUnLike(post: Post) {
                viewModel.unLikeById(post.id)
            }
            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }
            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }
        })

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter {
                adapter.retry()
            },
            footer = PostLoadingStateAdapter {
                adapter.retry()
            }
        )

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading || state.refreshing
            binding.swipeRefresh.isVisible = !state.refreshing
            binding.errorGroup.isVisible = state.error
            if (state.error) {
                Snackbar.make(
                    binding.root,
                    R.string.error_loading,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.retry_loading) {
                        viewModel.loadPosts()
                    }
                    .show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swipeRefresh.isRefreshing =
                        state.refresh is LoadState.Loading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (appAuth.authStateFlow.value.id != 0L && appAuth.authStateFlow.value.token != null) {
                    adapter.refresh()
                }
            }
        }

        binding.retryButton.setOnClickListener {
            viewModel.loadPosts()
        }
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            Snackbar.make(
                binding.root,
                getString(R.string.to_authentication),
                Snackbar.LENGTH_LONG
            ).setAction(
                R.string.sign_in
            ) {
                findNavController().navigate(R.id.action_feedFragment_to_authFragment)
            }.show()
        }

        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }

        viewModel.postsLoadError.observe(viewLifecycleOwner)
        {
            Toast.makeText(requireContext(), viewModel.postsLoadError.value, Toast.LENGTH_LONG)
                .show()
        }

        viewModel.savePostError.observe(viewLifecycleOwner)
        {
            Toast.makeText(requireContext(), viewModel.savePostError.value, Toast.LENGTH_LONG)
                .show()
        }

        binding.toNewPostsButton.isVisible = false

        binding.toNewPostsButton.setOnClickListener {
            binding.toNewPostsButton.isVisible = false
            viewModel.changeHiddenStatus()
        }

        return binding.root
    }
}
