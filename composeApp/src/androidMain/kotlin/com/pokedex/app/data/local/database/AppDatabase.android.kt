package com.pokedex.app.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pokedex.app.data.storage.AndroidImageStorage
import com.pokedex.app.domain.storage.ImageStorage

// Precisamos de uma forma de obter o contexto do Android.
// Geralmente isso é feito via DI ou passando o contexto na inicialização.
lateinit var appContext: Context

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = appContext.getDatabasePath("pokedex.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

actual fun getImageStorage(): ImageStorage = AndroidImageStorage(appContext)
