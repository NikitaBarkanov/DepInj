package ru.netology.nmedia.repository

import android.accounts.NetworkErrorException
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.Auth.AppAuth
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Add
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.AttachmentEmbeddable
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import java.io.File
import java.io.IOException
import java.util.Random
import java.util.concurrent.CancellationException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : PostRepository {

    @Inject
    lateinit var appAuth: AppAuth

    @OptIn(ExperimentalPagingApi::class)
    override val data : Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = {
            dao.getPagingSource()
        },
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = dao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb
            )
    ).flow
        .map { it.map(PostEntity :: toDto)
            .insertSeparators { previous, next ->
                if (previous?.id?.rem(5) ==0L) {
                    Add(kotlin.random.Random.nextLong(),"figma.jpg")
                } else {
                    null
                }
            }
        }
    override fun switchHidden() {
        dao.getAllInvisible()
    }


    override suspend fun registerWithAvatar(login: String, pass: String, name: String, file: File) {
        val formData = MultipartBody.Part.createFormData(
            "file", file.name, file.asRequestBody()
        )

        val userLogin = login.toRequestBody("text/plain".toMediaType())
        val password = pass.toRequestBody("text/plain".toMediaType())
        val userName = pass.toRequestBody("text/plain".toMediaType())

        val response = apiService.registerWithPhoto(userLogin, password, userName, formData)

        if(!response.isSuccessful) {
            throw RuntimeException(response.message())
        }

        val result = response.body() ?: throw RuntimeException("body is null")
        appAuth.setAuth(result.id, result.token)
    }

    override suspend fun setDataForRegistration(login: String, password: String, name: String) {
        try {
            val response = apiService.registerUser(login, password, name)

            if(!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            val result = response.body() ?: throw RuntimeException("Body is null")
            appAuth.setAuth(result.id, result.token)
        }
        catch (e: Exception) {
            throw NetworkErrorException()
        }
    }

    override suspend fun setIdAndTokenToAuth(id: String, token: String) {
        try {
            val response = apiService.updateUser(id, token)

            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            val result = response.body() ?: throw RuntimeException("body is null")
            appAuth.setAuth(result.id, result.token)
        }
        catch (e: IOException) {
            throw NetworkErrorException()
        }
    }

    override suspend fun saveWithAttachment(post: Post, file: File) {
        try {

            val media = uploadMedia(file)

            val response = apiService.savePost(
                post.copy(
                    attachment = AttachmentEmbeddable(
                        url = media.id,
                        "",
                        AttachmentType.IMAGE
                    )
                )
            )

            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            val result = response.body() ?: throw RuntimeException("body is null")
            dao.save(PostEntity.fromDto(result))

        } catch (e: IOException) {
            throw NetworkErrorException()
        }
    }

    private suspend fun uploadMedia(file: File): Media {
        val formData = MultipartBody.Part.createFormData(
            "file", file.name, file.asRequestBody()
        )

        val response = apiService.uploadMedia(formData)

        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }

        return response.body() ?: throw RuntimeException("body is null")
    }

    override fun getNewerCount(): Flow<Int> = flow {
        while (true) {
            try {
                delay(10_000)

                val postId = dao.getLatest().first().firstOrNull()?.id ?: 0

                val response = apiService.getNewer(postId)

                val posts = response.body().orEmpty()

                dao.insert(posts.toEntity(true))

                emit(dao.newerCount())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    override suspend fun likeById(id: Long): Post {
        try {
            dao.likeById(id)

            val response = apiService.likeById(id)

            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            return response.body() ?: throw RuntimeException("body is null")
        } catch (e: IOException) {
            throw NetworkErrorException()
        }
    }


    override suspend fun unlikeById(id: Long): Post {
        try {
            dao.likeById(id)

            val response = apiService.unlikeById(id)

            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            return response.body() ?: throw RuntimeException("body is null")
        } catch (e: IOException) {
            throw NetworkErrorException()
        }
    }

    override suspend fun edit(post: Post) {
        try {
            val response = apiService.editPost(post)

            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            val result = response.body() ?: throw RuntimeException("body is null")
            dao.save(PostEntity.fromDto(result))

        } catch (e: IOException) {
            throw NetworkErrorException()
        }
    }


    override suspend fun save(post: Post) {
        try {
            val response = apiService.savePost(post)

            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            val result = response.body() ?: throw RuntimeException("body is null")
            dao.save(PostEntity.fromDto(result))

        } catch (e: IOException) {
            throw NetworkErrorException()
        }
    }


    override suspend fun removeById(id: Long) {
        try {
            val response = apiService.deletePost(id)

            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            dao.removeById(id)
        } catch (e: IOException) {
            throw NetworkErrorException()
        }
    }
}

