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
    Glide.with(this)
        .load(url)
        .timeout(10_000)
        .circleCrop()
        .placeholder(R.drawable.baseline_crop_original_24)
        .transition(DrawableTransitionOptions.withCrossFade())
        .error(R.drawable.baseline_error_24)
        .into(this)
}