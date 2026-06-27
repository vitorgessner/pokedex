package com.pokedex.app.data.storage

import android.content.Context
import com.pokedex.app.domain.storage.ImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidImageStorage(private val context: Context) : ImageStorage {
    override suspend fun saveImage(fileName: String, bytes: ByteArray): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, fileName)
            file.writeBytes(bytes)
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getImage(path: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            File(path).readBytes()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteImage(path: String) {
        withContext(Dispatchers.IO) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}