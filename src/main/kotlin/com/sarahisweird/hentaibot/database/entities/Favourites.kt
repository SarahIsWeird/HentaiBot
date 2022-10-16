package com.sarahisweird.hentaibot.database.entities

import com.sarahisweird.hentaibot.database.tables.FavouritesTable
import dev.kord.common.entity.Snowflake
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Favourites(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Favourites>(FavouritesTable)

    var userId: Snowflake by FavouritesTable.userId.transform(
        toColumn = { it.value.toLong() },
        toReal = { Snowflake(it) }
    )

    var postId by FavouritesTable.postId
    var imageLink by FavouritesTable.imageLink
    var timestamp by FavouritesTable.timestamp
}