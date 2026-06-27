package com.pokedex.app.domain.storage

interface ImageStorage {
    suspend fun saveImage(fileName: String, bytes: ByteArray): String?
    suspend fun getImage(path: String): ByteArray?
    suspend fun deleteImage(path: String)
}