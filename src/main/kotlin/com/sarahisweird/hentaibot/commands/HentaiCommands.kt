package com.sarahisweird.hentaibot.commands

import com.beust.klaxon.Klaxon
import com.sarahisweird.hentaibot.data.Image
import com.sarahisweird.hentaibot.multipleRandom
import com.sarahisweird.hentaibot.waitUntilDone
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.MutableImage
import com.sksamuel.scrimage.metadata.ImageMetadata
import com.sksamuel.scrimage.nio.PngWriter
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.x.emoji.Emojis
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.api.arguments.AnyArg
import me.jakejmattson.discordkt.api.arguments.MultipleArg
import me.jakejmattson.discordkt.api.dsl.commands
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.addField
import me.jakejmattson.discordkt.api.extensions.addInlineField
import me.jakejmattson.discordkt.api.extensions.button
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.*
import javax.imageio.ImageIO
import kotlin.concurrent.thread

private val client = OkHttpClient()
private val klaxon = Klaxon()

private val sentHashes = mutableListOf<String>()

@OptIn(KordPreview::class)
@Suppress("BlockingMethodInNonBlockingContext")
fun hentaiCommands() = commands("Hentai") {
    command("rule34", "r34") {
        description = "Durchsucht rule34.xxx nach Tags. Du kannst mehrere Tags angeben."

        execute(AnyArg.multiple()) {
            channel.type()

            val images = fetchRandomImages(
                client,
                klaxon,
                tags = args.first,
                sentHashes
            ) ?: return@execute

            val compositeImage = BufferedImage(
                800,
                600,
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

            val file = File("composite.png")
            ImageIO.write(compositeImage, "png", file)

            channel.createMessage {
                addFile(file.toPath())

                createButtons(images)
            }
        }
    }
}

@OptIn(KordPreview::class)
fun hentaiCommandListener() = listeners {
    on<InteractionCreateEvent> {
        val ci = interaction as? ComponentInteraction ?: return@on
        if (!ci.componentId.startsWith("r34-")) return@on

        val split = ci.componentId.split("-")

        ci.respondPublic {
            content = "https://api-cdn.rule34.xxx/images/${split[1]}/${split[2]}"
        }
    }
}