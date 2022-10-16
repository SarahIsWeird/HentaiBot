package com.sarahisweird.hentaibot.commands

import com.sarahisweird.hentaibot.database.entities.ServerSettings
import com.sarahisweird.hentaibot.entryMenu
import com.sarahisweird.hentaibot.i18n.Languages
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.channel.createMessage
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.commands.commands
import org.jetbrains.exposed.sql.transactions.transaction

fun administrationCommands() = commands("Administration") {
    text("Eintritt") {
        requiredPermissions = Permissions(Permission.Administrator)

        execute {
            message.delete()

            channel.createMessage {
                entryMenu()
            }
        }
    }

    text("Language") {
        requiredPermissions = Permissions(Permission.Administrator)

        execute(AnyArg) {
            val newLanguage: Languages

            try {
                newLanguage = Languages.valueOf(args.first.uppercase())
            } catch (e: IllegalArgumentException) {
                respond("Invalid language!")
                return@execute
            }

            transaction {
                val column = ServerSettings.findById(guild.id)

                if (column != null) {
                    column.serverLanguage = newLanguage.resources
                    return@transaction
                }

                ServerSettings.new(guild.id) {
                    serverLanguage = newLanguage.resources
                }
            }

            respond(ServerSettings.languageOf(guild).languageUpdated)
        }
    }
}