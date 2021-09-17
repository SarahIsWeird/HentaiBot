package com.sarahisweird.hentaibot.commands

import com.beust.klaxon.Klaxon
import com.sarahisweird.hentaibot.waitUntilDone
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import me.jakejmattson.discordkt.api.arguments.AnyArg
import me.jakejmattson.discordkt.api.dsl.commands
import me.jakejmattson.discordkt.api.dsl.listeners
import okhttp3.OkHttpClient
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Integer.min
import javax.imageio.ImageIO
import kotlin.math.ceil

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