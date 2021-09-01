package com.sarahisweird.hentaibot.commands

import com.beust.klaxon.Klaxon
import com.sarahisweird.hentaibot.data.Image
import com.sarahisweird.hentaibot.multipleRandom
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.MutableImage
import com.sksamuel.scrimage.metadata.ImageMetadata
import com.sksamuel.scrimage.nio.PngWriter
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.x.emoji.Emojis
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

private fun buildRequest(tags: List<String>) =
    Request.Builder().url("https://rule34.xxx/index.php?page=dapi&s=post&q=index" +
            "&limit=100&json=1&tags=${tags.joinToString("+")}").build()

private fun getResponse(request: Request) =
    client.newCall(request).execute().body?.string()

@OptIn(KordPreview::class)
@Suppress("BlockingMethodInNonBlockingContext")
fun hentaiCommands() = commands("Hentai") {
    command("rule34", "r34") {
        description = "Durchsucht rule34.xxx nach Tags. Du kannst mehrere Tags angeben."

        execute(AnyArg.multiple()) {
            val request = buildRequest(args.first)
            val bodyContent = getResponse(request)

            if (bodyContent == null || bodyContent == "") {
                respond("Das hat nicht geklappt. :(")
                return@execute
            }

            val images = klaxon.parseArray<Image>(bodyContent)?.filter {
                !sentHashes.contains(it.hash)
            }?.multipleRandom(12)

            if ((images?.size == 0) or (images == null)) {
                respond("Es wurde nix gefunden :(")
                return@execute
            }

            channel.type()

            val compositeImage = BufferedImage(
                800,
                600,
                BufferedImage.TYPE_INT_ARGB
            )

            val g2 = compositeImage.graphics

            g2.color = Color.BLACK

            val threadPool = mutableListOf<Thread>()

            // thread me daddy
            images?.forEachIndexed { i, it ->
                threadPool += thread {
                    val req = Request.Builder().url(it.previewUrl).build()
                    val res = client.newCall(req).execute()

                    val imageData = res.body?.byteStream() ?: return@thread
                    val image = ImmutableImage.loader().fromStream(imageData)
                        .fit(200, 200, Color.LIGHT_GRAY)

                    while (!g2.drawImage(
                        image.awt(),
                        (i % 4) * 200,
                        (i / 4) * 200,
                        null
                    )) {}
                }
            }

            while (threadPool.find { it.isAlive } != null) {}

            val file = File("composite.png")

            channel.createMessage {
                ImageIO.write(compositeImage, "png", file)
                addFile(file.toPath())

                // indexed to deduplicate forEach label kekw
                (0 until 3).forEachIndexed { _, y ->
                    actionRow {
                        (0 until 4).forEach { x ->
                            val index = x + y * 4
                            val image = images?.getOrNull(index) ?: return@forEach

                            button("Bild ${index + 1}", Emojis.link) {
                                customId = "r34-${image.directory}-${image.image}"
                            }
                        }
                    }
                }
            }
        }
    }

    command("raw34") {
        execute(AnyArg.multiple()) {
            val request = buildRequest(args.first)
            val bodyContent = getResponse(request)

            if (bodyContent == null || bodyContent == "") {
                respond("Das hat nicht geklappt. :(")
                return@execute
            }

            val image = klaxon.parseArray<Image>(bodyContent)?.randomOrNull()

            respond {
                title = "Daten"

                if (image == null) {
                    description = "Nix :("
                    return@respond
                }

                addInlineField("Image", image.image)
                addInlineField("Owner", image.owner)
                addInlineField("Score", image.score.toString())
                addInlineField("Width", image.width.toString())
                addInlineField("Height", image.height.toString())
                addField("Tags", image.tags)
            }

            respond(image?.fileUrl ?: "Es wurde nix gefunden :(")
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