package com.sarahisweird.hentaibot

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.message.MessageCreateBuilder
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.button

val guild = Snowflake(881874507877453885L)
val role = Snowflake(881905612420710400L)

@OptIn(KordPreview::class)
fun MessageCreateBuilder.entryMenu() {
    embed {
        title = "Willkommen!"
        description = "Auf diesem Server gibts Hentai.\n" +
                "Wenn du damit einverstanden bist, solche Inhalte zu sehen, " +
                "dann klicke bitte auf den Button.\n" +
                "\n" +
                "Indem du auf den Button klickst, akzeptierst du auch, dass du nicht " +
                "Leute auf diesem Server als pervers usw. abstempelst, insbesondere " +
                "bei anderen. Außerdem akzeptierst du, dass du nicht rumnörgeln darfst, " +
                "weil Leute über ihre Kinks o. Ä. reden, oder eben Hentai/Porn " +
                "reinschicken.\n" +
                "\n" +
                "Danke! uwu"
    }

    actionRow {
        button("Das ist ok!", Emojis.whiteCheckMark) {
            customId = "em-accept"
        }
    }
}

@OptIn(KordPreview::class)
fun entryMenuListener() = listeners {
    on<InteractionCreateEvent> {
        val ci = interaction as? ComponentInteraction ?: return@on
        if (ci.componentId != "em-accept") return@on

        ci.respondEphemeral {
            content = "Danke!"
        }

        ci.user.asMember(guild).addRole(role)
    }
}