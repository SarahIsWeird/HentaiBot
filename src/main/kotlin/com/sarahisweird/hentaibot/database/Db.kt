package com.sarahisweird.hentaibot.database

import com.sarahisweird.hentaibot.database.tables.BannedTagsTable
import com.sarahisweird.hentaibot.database.tables.FavouritesTable
import com.sarahisweird.hentaibot.database.tables.ServerSettingsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object Database {
    private const val jdbcUrl = "jdbc:mysql://localhost:3306/hentaibot"
    private const val driver = "com.mysql.jdbc.Driver"
    private val username = System.getenv("hentaibot_dbuser")
    private val password = System.getenv("hentaibot_dbpassword")

    val db by lazy {
        val db = Database.connect(
            url = jdbcUrl,
            driver = driver,
            user = username,
            password = password
        )

        transaction {
            SchemaUtils.create(
                FavouritesTable,
                BannedTagsTable,
                ServerSettingsTable
            )
        }

        db
    }
}