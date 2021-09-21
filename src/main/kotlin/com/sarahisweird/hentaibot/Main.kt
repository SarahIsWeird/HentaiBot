package com.sarahisweird.hentaibot

import com.sarahisweird.hentaibot.database.Database
import dev.kord.common.annotation.KordPreview
import me.jakejmattson.discordkt.api.dsl.bot

@OptIn(KordPreview::class)
fun main() {
    val token = System.getenv("hentaibot_token")

    val db = Database.db

    println("Successfully connected to database at ${db.url} (${db.dialect.name}).")

    bot(token) {
        prefix { "!" }

        configure {
            permissions(commandDefault = Permissions.EVERYONE)
        }
    }
}