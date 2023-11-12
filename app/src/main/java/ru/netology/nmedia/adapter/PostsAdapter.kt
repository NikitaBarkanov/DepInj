package ru.netology.nmedia.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAddBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Add
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.handler.load
import ru.netology.nmedia.handler.loadAttachmentImage

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onUnLike(post: Post)
    fun onShowImageAsSeparate(post: Post)
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Add -> R.layout.card_add
            is Post -> R.layout.card_post
            null -> error("unknown item type")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }

            R.layout.card_add -> {
                val binding = CardAddBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AddViewHolder(binding)
            }

            else -> error("unknown viewtype: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Add -> (holder as AddViewHolder).bind(item)
            is Post -> (holder as PostViewHolder).bind(item)
            null -> error("unknown item type")
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    private var index = 0

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            // в адаптере
            like.isChecked = post.likedByMe
            like.text = "${post.likes}"

            binding.attachmentImage.isVisible = !post.attachment?.url.isNullOrBlank()

            menu.isVisible = post.ownedByMe

            var url = "${"http://10.0.2.2:9999/"}avatars/${post.authorAvatar}"
            var attachmentUrl = "${"http://10.0.2.2:9999/"}media/${post.attachment?.url}"

            Log.d("url: ", url)
            Log.d("attachmentUrl: ", attachmentUrl)

            binding.attachmentImage.loadAttachmentImage(attachmentUrl)

            attachmentImage.setOnClickListener {
                onInteractionListener.onShowImageAsSeparate(post)
            }

            binding.avatar.load(url)

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                if(!post.likedByMe) onInteractionListener.onLike(post)
                else onInteractionListener.onUnLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
        }
    }
}


class AddViewHolder(
    private val binding: CardAddBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Add) {
        binding.advertismentImage.load("${"http://10.0.2.2:9999/"}media/${ad.image}")
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if(oldItem::class != newItem::class) return false

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}
