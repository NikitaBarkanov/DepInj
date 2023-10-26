package ru.netology.nmedia.dto

import ru.netology.nmedia.entity.AttachmentEmbeddable

data class Post(
    val id: Long,
    val authorId: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val authorAvatar: String,
    val attachment: AttachmentEmbeddable? = null,
    val ownedByMe: Boolean = false
)

data class Attachment(
    val url: String,
    val description: String?,
    val type: AttachmentType
)
