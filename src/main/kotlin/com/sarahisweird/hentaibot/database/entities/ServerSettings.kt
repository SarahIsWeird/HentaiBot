package com.sarahisweird.hentaibot.database.entities

import com.sarahisweird.hentaibot.database.tables.ServerSettingsTable
import com.sarahisweird.hentaibot.i18n.Languages
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class ServerSettings(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ServerSettings>(ServerSettingsTable) {
        fun languageOf(guild: Guild?) =
            transaction { findById(guild!!.id.value.toLong()) ?: new(guild.id.value.toLong()) {} }.serverLanguage

        fun findById(snowflake: Snowflake) =
            findById(snowflake.value.toLong())
        fun new(id: Snowflake, init: ServerSettings.() -> Unit): ServerSettings =
            new(id.value.toLong(), init)
    }

    var serverLanguage by ServerSettingsTable.serverLanguage.transform(
        { Languages.values().find { it2 -> it == it2.resources }!! },
        { it.resources }
    )
}