package ru.netology.nmedia.handler

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import ru.netology.nmedia.R

fun ImageView.loadAttachmentImage(url: String) {
    Glide.with(this)
        .load(url)
        .timeout(10_000)
        .placeholder(R.drawable.baseline_crop_original_24)
        .transition(DrawableTransitionOptions.withCrossFade())
        .error(R.drawable.baseline_error_24)
        .into(this)
}