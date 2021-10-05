package com.sarahisweird.hentaibot.database.tables

import com.sarahisweird.hentaibot.i18n.Languages
import org.jetbrains.exposed.dao.id.LongIdTable

object ServerSettingsTable : LongIdTable() {
    val serverLanguage = customEnumeration(
        "server_language",
        "ENUM(${Languages.values().joinToString { "'${it}'" }})",
        { Languages.valueOf(it as String) },
        { it.name }
    ).default(Languages.ENGLISH)
}