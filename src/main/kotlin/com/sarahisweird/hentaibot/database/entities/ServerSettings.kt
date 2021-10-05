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
            transaction { findById(guild!!.id.value) ?: new(guild.id.value) {} }.serverLanguage
    }

    var serverLanguage by ServerSettingsTable.serverLanguage.transform(
        { Languages.values().find { it2 -> it == it2.resources }!! },
        { it.resources }
    )
}