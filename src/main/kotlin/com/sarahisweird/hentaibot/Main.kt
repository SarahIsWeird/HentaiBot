package com.sarahisweird.hentaibot

import dev.kord.common.annotation.KordPreview
import me.jakejmattson.discordkt.api.dsl.bot

@OptIn(KordPreview::class)
fun main() {
    val token = System.getenv("hentaibot_token")

    bot(token) {
        prefix { "!" }

        configure {
            permissions(commandDefault = Permissions.EVERYONE)
        }
    }
}