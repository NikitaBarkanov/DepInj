package ru.netology.nmedia.util

class ApiError(
    code: Int,
    message: String
) : RuntimeException(message)