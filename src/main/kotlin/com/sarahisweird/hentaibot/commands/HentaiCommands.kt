package com.sarahisweird.hentaibot.commands

import com.beust.klaxon.Klaxon
import com.sarahisweird.hentaibot.data.Image
import com.sarahisweird.hentaibot.database.entities.BannedTags
import com.sarahisweird.hentaibot.database.entities.Favourites
import com.sarahisweird.hentaibot.database.entities.ServerSettings
import com.sarahisweird.hentaibot.database.tables.FavouritesTable
import com.sarahisweird.hentaibot.util.paginatedMenu
import com.sarahisweird.hentaibot.util.waitUntilDone
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.optional
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.*
import dev.kord.core.entity.Guild
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.interaction.actionRow
import dev.kord.x.emoji.Emojis
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import me.jakejmattson.discordkt.api.arguments.AnyArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.dsl.commands
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.button
import okhttp3.OkHttpClient
import okhttp3.internal.wait
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Integer.min
import javax.imageio.ImageIO
import kotlin.concurrent.thread
import kotlin.math.ceil

private val client = OkHttpClient()
private val klaxon = Klaxon()

private val sentHashes = mutableListOf<String>()
private val tags = mutableListOf<List<String>>()

@OptIn(KordPreview::class)
private val publicInteractionResponses = mutableMapOf<Snowflake, PublicInteractionResponseBehavior>()

@OptIn(KordPreview::class)
private val ephemeralInteractionResponses = mutableMapOf<Snowflake, EphemeralInteractionResponseBehavior>()

private val file = File("composite.png")

suspend fun createCompositeImageFromTags(
    tags: List<String>,
    guild: Guild?,
    respond: suspend (String) -> Unit
): List<Image>? {
    val images = fetchRandomImages(
        client,
        klaxon,
        tags,
        sentHashes,
        respond,
        guild
    ) ?: return null

    val compositeImage = BufferedImage(
        min(800, images.size * 200),
        min(600, ceil(images.size / 4f).toInt() * 200),
        BufferedImage.TYPE_INT_ARGB
    )

    val g2 = compositeImage.graphics

    // thread me daddy
    val threadPool = images.mapIndexed { i, image ->
        createStitchingThread(
            client,
            image,
            stitchedGraphics = g2,
            stitchToX = (i % 4) * 200,
            stitchToY = (i / 4) * 200
        )
    }

    threadPool.waitUntilDone(200)

    runCatching { ImageIO.write(compositeImage, "png", file) }

    return images
}

@OptIn(KordPreview::class)
@Suppress("BlockingMethodInNonBlockingContext")
fun hentaiCommands() = commands("Hentai") {
    command("rule34", "r34") {
        description = "Searches through rule34.xxx, based on tags. You can specify multiple tags."

        execute(AnyArg.multiple()) {
            channel.type()

            val bannedTags = transaction {
                BannedTags.findByIdOrPut(author.id, ::mutableListOf).tags
            }
            val actualTags = args.first + bannedTags.map { "-$it" }

            val images = createCompositeImageFromTags(actualTags, guild, ::respond) ?: return@execute

            tags += actualTags

            channel.createMessage {
                addFile(file.toPath())

                createButtons(images, guild, ::actionRow)

                actionRow {
                    button(null, Emojis.arrowsCounterclockwise) {
                        customId = "r34rl-${tags.size - 1}"
                    }
                }
            }
        }
    }

    command("Likes") {
        description = "Lists your liked pictures."

        execute {
            val likedCount = transaction {
                Favourites.find { FavouritesTable.userId eq author.id.value }
                    .count().toInt()
            }

            if (likedCount == 0) {
                respond(ServerSettings.languageOf(guild).noLikedPicture)
                return@execute
            }

            paginatedMenu(author.id) {
                limit = likedCount - 1

                pageCreator { _, next ->
                    author {
                        name = "HentaiBot"
                        icon = "https://cdn.discordapp.com/avatars" +
                                "/881886640254095371/9562bd55dfd4969c403c73c69c89b591.webp"
                    }

                    title = "${ServerSettings.languageOf(guild).picture} ${next + 1}/${limit + 1}"

                    transaction {
                        val res = Favourites.find { FavouritesTable.userId eq forWhom.value }
                            .orderBy(FavouritesTable.timestamp to SortOrder.DESC)
                            .elementAt(next)

                        image = res.imageLink
                        url = "https://rule34.xxx/index.php?page=post&s=view&id=${res.postId}"
                    }
                }

                actionRowCreator { _, next ->
                    button(null, Emojis.brokenHeart) {
                        customId = "r34ul-${transaction {
                            Favourites.find { FavouritesTable.userId eq forWhom.value }
                            .orderBy(FavouritesTable.timestamp to SortOrder.DESC)
                            .elementAt(next).id
                        }}"
                    }
                }
            }
        }
    }

    command("bannedTags", "bt", "blacklist", "bl") {
        description = "Modifies your blacklist."

        execute(AnyArg.optional(""), AnyArg.multiple()) {
            when (args.first) {
                "add" -> {
                    transaction {
                        val res = BannedTags.findByIdOrPut(author.id, ::mutableListOf)

                        res.tags = (res.tags + args.second.map { it.removePrefix("-") })
                            .distinct()
                    }

                    respond(ServerSettings.languageOf(guild).blacklistUpdated)
                }
                "remove" -> {
                    transaction {
                        val res = BannedTags.findByIdOrPut(author.id, ::mutableListOf)

                        res.tags -= args.second
                    }

                    respond(ServerSettings.languageOf(guild).blacklistUpdated)
                }
                "show" -> {
                    val tags = transaction {
                        BannedTags.findByIdOrPut(author.id, ::mutableListOf).tags
                    }

                    if (tags.isEmpty()) {
                        respond(ServerSettings.languageOf(guild).emptyBlacklist)
                        return@execute
                    }

                    respond("${ServerSettings.languageOf(guild).listBlacklist}: ${tags.joinToString()}")
                }
                else -> respond(ServerSettings.languageOf(guild).invalidFirstBlacklistArgument)
            }
        }
    }
}

@OptIn(KordPreview::class, dev.kord.common.annotation.KordUnsafe::class)
fun hentaiCommandListener() = listeners {
    on<InteractionCreateEvent> {
        val ci = interaction as? ComponentInteraction ?: return@on
        if (!ci.componentId.startsWith("r34")) return@on

        when {
            (ci.componentId.startsWith("r34-")) -> onImageSelect(ci)
            (ci.componentId.startsWith("r34l-")) -> onImageLink(ci)
            (ci.componentId.startsWith("r34n-")) -> onImageLike(ci)
            (ci.componentId.startsWith("r34ul-")) -> onImageUnlike(ci)
            (ci.componentId.startsWith("r34rl-")) -> onImageReload(ci)
        }
    }
}

@OptIn(KordPreview::class)
suspend fun InteractionCreateEvent.onImageSelect(ci: ComponentInteraction) {
    val interactionResponse = publicInteractionResponses.getOrPut(
        ci.message!!.id
    ) { ci.acknowledgePublicDeferredMessageUpdate() }

    runCatching { ci.acknowledgePublicDeferredMessageUpdate() }

    val split = ci.componentId.split("-")

    interactionResponse.followUp {
        content = "https://api-cdn.rule34.xxx/images/${split[1]}/${split[2]}"

        actionRow {
            button(null, Emojis.link) {
                customId = "r34l-${split[3]}"
            }

            button(null, Emojis.blackHeart) {
                customId = ci.componentId.replace("r34-", "r34n-")
            }
        }
    }
}

@OptIn(KordPreview::class, dev.kord.common.annotation.KordUnsafe::class)
suspend fun onImageLink(ci: ComponentInteraction) {
    val interactionResponse = ephemeralInteractionResponses.getOrPut(
        ci.message!!.id
    ) { ci.acknowledgeEphemeralDeferredMessageUpdate() }

    val split = ci.componentId.split("-")

    interactionResponse.followUpPublic {
        content = "https://rule34.xxx/index.php?page=post&s=view&id=${split[1]}"
    }
}

@OptIn(KordPreview::class)
suspend fun onImageLike(ci: ComponentInteraction) {
    val interactionResponse = ephemeralInteractionResponses.getOrPut(
        ci.message!!.id
    ) { ci.acknowledgeEphemeralDeferredMessageUpdate() }

    val split = ci.componentId.split("-")

    val inserted = transaction {
        if (!Favourites.find {
                (FavouritesTable.userId eq ci.user.id.value) and
                        (FavouritesTable.postId eq split[3].toInt())
            }.empty()) return@transaction false

        Favourites.new {
            userId = ci.user.id
            postId = split[3].toInt()
            imageLink = "https://api-cdn.rule34.xxx/images/${split[1]}/${split[2]}"
            timestamp = Clock.System.now().epochSeconds
        }

        return@transaction true
    }

    interactionResponse.followUpEphemeral {
        content = if (inserted) ServerSettings.languageOf(ci.message?.getGuild()).addedToLikes
        else ServerSettings.languageOf(ci.message?.getGuild()).alreadyLiked
    }
}

@OptIn(KordPreview::class)
suspend fun onImageUnlike(ci: ComponentInteraction) {
    val interactionResponse = ephemeralInteractionResponses.getOrPut(
        ci.message!!.id
    ) { ci.acknowledgeEphemeralDeferredMessageUpdate() }

    val split = ci.componentId.split("-")

    transaction {
        Favourites.findById(split[1].toInt())?.delete()
    }

    ci.message?.delete()
    interactionResponse.followUpEphemeral {
        content = ServerSettings.languageOf(ci.message?.getGuild()).likeRemoved
    }
}

@OptIn(KordPreview::class)
suspend fun onImageReload(ci: ComponentInteraction) {
    val interactionResponse = publicInteractionResponses.getOrPut(
        ci.message!!.id
    ) { ci.acknowledgePublicDeferredMessageUpdate() }

    val split = ci.componentId.split("-")

    ci.message?.channel?.type()
    val tagsIndex = split[1].toInt()

    if (tagsIndex !in tags.indices) {
        ci.message?.channel?.createMessage(
            ServerSettings.languageOf(ci.message?.getGuild()).somethingWentWrong
        )

        return
    }

    val images = createCompositeImageFromTags(tags[tagsIndex], ci.message?.getGuild()) {
        ci.message?.channel?.createMessage(it)
    } ?: return

    interactionResponse.followUp {
        files.clear()
        files += "composite.png" to file.inputStream()

        createButtons(images, ci.message?.getGuild(), ::actionRow)

        actionRow {
            button(null, Emojis.arrowsCounterclockwise) {
                customId = "r34rl-${tags.size - 1}"
            }
        }
    }
}