package com.pokedex.app.data.local.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.pokedex.app.data.storage.IosImageStorage
import com.pokedex.app.domain.storage.ImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = NSHomeDirectory() + "/pokedex.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile,
        factory = { AppDatabase::class.instantiateImpl() }
    ).setDriver(BundledSQLiteDriver())
     .setQueryCoroutineContext(Dispatchers.IO)
}

actual fun getImageStorage(): ImageStorage = IosImageStorage()
