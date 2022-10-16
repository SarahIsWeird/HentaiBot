package com.sarahisweird.hentaibot

import com.sarahisweird.hentaibot.database.Database
import dev.kord.common.annotation.KordPreview
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import me.jakejmattson.discordkt.dsl.bot

@OptIn(KordPreview::class, PrivilegedIntent::class)
fun main() {
    val token = System.getenv("hentaibot_token")

    val db = Database.db

    println("Successfully connected to database at ${db.url} (${db.dialect.name}).")

    bot(token) {
        prefix { "!" }

        configure {
            intents = Intents.all
        }
    }
}