package com.sarahisweird.hentaibot.commands

import com.sarahisweird.hentaibot.Permissions
import com.sarahisweird.hentaibot.entryMenu
import dev.kord.core.behavior.channel.createMessage
import me.jakejmattson.discordkt.api.dsl.commands

fun administrationCommands() = commands("Administration") {
    command("Eintritt") {
        requiredPermission = Permissions.BOT_OWNER

        execute {
            message?.delete()

            channel.createMessage {
                entryMenu()
            }
        }
    }
}