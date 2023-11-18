package ru.netology.nmedia.handler

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import ru.netology.nmedia.R

fun ImageView.loadAttachmentImage(url: String, context: Context) {

    Glide.with(this)
        .load(url)
        .timeout(10_000)
        .placeholder(R.drawable.baseline_crop_original_24)
        .transition(DrawableTransitionOptions.withCrossFade())
        .error(R.drawable.baseline_error_24)
        .into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                this@loadAttachmentImage.setImageDrawable(resource)
                val layoutParams = this@loadAttachmentImage.layoutParams
                val width = resource.intrinsicWidth
                val height = resource.intrinsicHeight

                val displayMetrics = context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                layoutParams.width = screenWidth

                val calculatedHeight = (screenWidth.toFloat() / width.toFloat() * height).toInt()
                layoutParams.height = calculatedHeight
                this@loadAttachmentImage.layoutParams = layoutParams
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                this@loadAttachmentImage.setImageDrawable(placeholder)
            }

        })
}