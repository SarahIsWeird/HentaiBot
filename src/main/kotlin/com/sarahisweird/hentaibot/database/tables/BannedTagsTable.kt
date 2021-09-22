package com.sarahisweird.hentaibot.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object BannedTagsTable : IntIdTable() {
    val userId = long("user_id")
    val tags = text("tags")
}