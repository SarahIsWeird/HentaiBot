package com.sarahisweird.hentaibot.database.entities

import com.sarahisweird.hentaibot.database.tables.BannedTagsTable
import dev.kord.common.entity.Snowflake
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class BannedTags(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BannedTags>(BannedTagsTable) {
        fun findByIdOrPut(id: Snowflake, supplier: () -> List<String>) =
            transaction {
                find {
                    BannedTagsTable.userId eq id.value.toLong()
                }.firstOrNull() ?: new {
                    userId = id
                    tags = supplier()
                }
            }
    }

    var userId by BannedTagsTable.userId.transform(
        toColumn = { it.value.toLong() },
        toReal = { Snowflake(it) }
    )
    var tags by BannedTagsTable.tags.transform(
        toColumn = { it.joinToString(",") },
        toReal = {
            if (it == "") listOf()
            else it.split(",")
        }
    )
}