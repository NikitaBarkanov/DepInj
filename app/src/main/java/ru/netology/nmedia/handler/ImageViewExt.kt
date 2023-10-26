package ru.netology.nmedia.handler

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding

fun ImageView.load(url: String) {

    val binding = CardPostBinding.inflate(LayoutInflater.from(context))

    Glide.with(this)
        .load(url)
        .timeout(10_000)
        .placeholder(R.drawable.baseline_crop_original_24)
        .transition(DrawableTransitionOptions.withCrossFade())
        .circleCrop()
        .error(R.drawable.baseline_error_24)
        .into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                val width = resource.intrinsicWidth
                val height = resource.intrinsicHeight

                val displayMetrics = binding.root.context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels

                val calculatedHeight = (screenWidth.toFloat() / width.toFloat() * height).toInt()
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                TODO("Not yet implemented")
            }

        })
}