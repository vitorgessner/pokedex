package com.pokedex.app.data.storage

import com.pokedex.app.domain.storage.ImageStorage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.*
import platform.posix.memcpy

class IosImageStorage : ImageStorage {
    private val fileManager = NSFileManager.defaultManager

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun saveImage(fileName: String, bytes: ByteArray): String? {
        val documentDirectory = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).first() as NSURL
        val fileUrl = documentDirectory.URLByAppendingPathComponent(fileName) ?: return null
        
        val data = bytes.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
        }
        
        return if (data.writeToURL(fileUrl, true)) {
            fileUrl.path
        } else {
            null
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getImage(path: String): ByteArray? {
        val data = NSData.dataWithContentsOfFile(path) ?: return null
        val bytes = ByteArray(data.length.toInt())
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
        return bytes
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun deleteImage(path: String) {
        fileManager.removeItemAtPath(path, null)
    }
}