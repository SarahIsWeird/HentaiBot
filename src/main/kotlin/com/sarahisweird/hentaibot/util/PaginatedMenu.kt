package com.sarahisweird.hentaibot.util

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.acknowledgePublicUpdateMessage
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.MessageCreateBuilder
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.api.TypeContainer
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.button

val menus = mutableMapOf<String, PaginatedMenuContext>()

class PaginatedMenuContext(
    val forWhom: Snowflake
) {
    private var pageCreator: (EmbedBuilder.(Int, Int) -> Unit)? = null

    @OptIn(KordPreview::class)
    var actionRowCreator: (ActionRowBuilder.(Int, Int) -> Unit)? = null

    private var currentPage: Int = 0

    @OptIn(KordPreview::class)
    suspend fun updatePage(interaction: ComponentInteraction) {
        val oldPage = currentPage

        if (interaction.componentId.endsWith("-p")) previousPage()
        else if (interaction.componentId.endsWith("-n")) nextPage()
        else return

        if (currentPage == oldPage) {
            interaction.acknowledgePublicDeferredMessageUpdate()
            return
        }

        interaction.acknowledgePublicUpdateMessage {
            embed {
                createEmbed(this, oldPage)
            }

            actionRow {
                createButtons(this)
                actionRowCreator?.invoke(this, oldPage, currentPage)
            }
        }
    }

    fun createEmbed(embedBuilder: EmbedBuilder, oldPage: Int) =
        pageCreator?.invoke(embedBuilder, oldPage, currentPage)
            ?: error("No pageCreator specified.")

    @OptIn(KordPreview::class)
    fun createButtons(actionRowBuilder: ActionRowBuilder) {
        actionRowBuilder.button(null, Emojis.arrowLeft) {
            customId = "pm-${forWhom.asString}-p"
        }

        actionRowBuilder.button(null, Emojis.arrowRight) {
            customId = "pm-${forWhom.asString}-n"
        }
    }

    var limit: Int = 0
    var wrapOnEnd: Boolean = false

    fun pageCreator(
        content: EmbedBuilder.(last: Int, next: Int) -> Unit
    ) {
        pageCreator = content
    }

    @OptIn(KordPreview::class)
    fun actionRowCreator(
        content: ActionRowBuilder.(last: Int, next: Int) -> Unit
    ) {
        actionRowCreator = content
    }

    private fun nextPage() {
        if (currentPage < limit) currentPage++
        else if (wrapOnEnd) currentPage = 0
    }

    private fun previousPage() {
        if (currentPage > 0) currentPage--
        else if (wrapOnEnd) currentPage = limit
    }
}

@OptIn(KordPreview::class)
fun MessageCreateBuilder.paginatedMenu(forWhom: Snowflake, content: PaginatedMenuContext.() -> Unit) {
    val context = PaginatedMenuContext(forWhom)
    context.content()
    
    menus["pm-${forWhom.asString}"] = context
    
    embed {
        context.createEmbed(this, 0)
    }

    actionRow {
        context.createButtons(this)
        context.actionRowCreator?.invoke(this, 0, 0)
    }
}

suspend fun <T : TypeContainer> CommandEvent<T>.paginatedMenu(forWhom: Snowflake, content: PaginatedMenuContext.() -> Unit) {
    channel.createMessage { paginatedMenu(forWhom, content) }
}

@OptIn(KordPreview::class)
fun paginatedMenuListener() = listeners {
    on<InteractionCreateEvent> {
        val ci = interaction as? ComponentInteraction ?: return@on

        if (!ci.componentId.startsWith("pm-")) return@on
        if (ci.componentId.drop(3).dropLast(2) != ci.user.id.asString) {
            ci.respondEphemeral { content = "Du kannst dieses Menü nicht benutzen." }
            return@on
        }

        menus[ci.componentId.dropLast(2)]?.updatePage(ci)
    }
}