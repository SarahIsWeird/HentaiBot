package com.sarahisweird.hentaibot.commands

import com.sarahisweird.hentaibot.Permissions
import com.sarahisweird.hentaibot.database.entities.ServerSettings
import com.sarahisweird.hentaibot.entryMenu
import com.sarahisweird.hentaibot.i18n.Languages
import dev.kord.core.behavior.channel.createMessage
import me.jakejmattson.discordkt.api.arguments.AnyArg
import me.jakejmattson.discordkt.api.dsl.commands
import org.jetbrains.exposed.sql.transactions.transaction

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

    command("Language") {
        requiredPermission = Permissions.BOT_OWNER

        execute(AnyArg) {
            val newLanguage: Languages

            try {
                newLanguage = Languages.valueOf(args.first.uppercase())
            } catch (e: IllegalArgumentException) {
                respond("Invalid language!")
                return@execute
            }

            transaction {
                val column = ServerSettings.findById(guild!!.id.value)

                if (column != null) {
                    column.serverLanguage = newLanguage.resources
                    return@transaction
                }

                ServerSettings.new(guild!!.id.value) {
                    serverLanguage = newLanguage.resources
                }
            }

            respond(ServerSettings.languageOf(guild).languageUpdated)
        }
    }
}