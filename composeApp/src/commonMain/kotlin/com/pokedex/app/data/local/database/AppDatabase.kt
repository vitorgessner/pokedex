package com.pokedex.app.data.local.database

import androidx.room.Database
import androidx.room.ConstructedBy
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.pokedex.app.data.local.dao.PokemonDao
import com.pokedex.app.data.local.dao.TeamDao
import com.pokedex.app.data.local.entity.PokemonEntity
import com.pokedex.app.data.local.entity.TeamEntity

@Database(entities = [PokemonEntity::class, TeamEntity::class], version = 4, exportSchema = false)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun teamDao(): TeamDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

expect fun getImageStorage(): com.pokedex.app.domain.storage.ImageStorage

fun getDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .fallbackToDestructiveMigration(true)
        .build()
}
