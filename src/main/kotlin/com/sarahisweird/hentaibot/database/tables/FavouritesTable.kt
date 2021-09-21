package com.sarahisweird.hentaibot.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object FavouritesTable : IntIdTable() {
    val userId = long("user_id")
    val postId = integer("post_id")
    val imageLink = varchar("image_link", 256)
    val timestamp = long("timestamp")
}