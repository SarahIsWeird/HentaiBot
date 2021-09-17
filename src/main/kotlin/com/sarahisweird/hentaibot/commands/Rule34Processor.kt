package com.sarahisweird.hentaibot.commands

import com.beust.klaxon.Klaxon
import com.sarahisweird.hentaibot.data.Image
import com.sarahisweird.hentaibot.multipleRandom
import com.sksamuel.scrimage.ImmutableImage
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.MessageCreateBuilder
import dev.kord.x.emoji.Emojis
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.api.TypeContainer
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.extensions.button
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.Color
import java.awt.Graphics
import java.io.InputStream
import kotlin.concurrent.thread
import kotlin.math.ceil

suspend fun <T : TypeContainer> CommandEvent<T>.fetchRandomImages(
    client: OkHttpClient,
    klaxon: Klaxon,
    tags: List<String>,
    sentHashes: List<String>
): List<Image>? {
    val bodyContent = getResponse(client, tags)

    if (bodyContent == null) {
        respond("Es wurde nix gefunden :(")
        return null
    }

    val images = klaxon.parseArray<Image>(bodyContent)?.filter {
        !sentHashes.contains(it.hash)
    }?.multipleRandom(12).takeIf { it?.size != 0 }

    if (images == null) {
        respond("Es wurde nix gefunden :(")
        return null
    }

    return images
}

private fun buildRequest(tags: List<String>) =
    Request.Builder().url("https://rule34.xxx/index.php?page=dapi&s=post&q=index" +
            "&limit=100&json=1&tags=${tags.joinToString("+")}").build()

private fun getResponse(client: OkHttpClient, tags: List<String>) =
    client.newCall(buildRequest(tags)).execute().body?.string().takeIf { it != "" }

fun <T : TypeContainer> CommandEvent<T>.createStitchingThread(
    client: OkHttpClient,
    image: Image,
    stitchedGraphics: Graphics,
    stitchToX: Int,
    stitchToY: Int
) = thread {
    val imageData = loadImageData(client, image)
    val resizedImage = tryResizeImage(image, imageData) ?: return@thread

    while (!stitchedGraphics.drawImage(resizedImage.awt(), stitchToX, stitchToY, null)) {
        runBlocking { delay(200) }
    }
}

private fun loadImageData(client: OkHttpClient, image: Image): InputStream? =
    client.newCall(Request.Builder().url(image.previewUrl).build()).execute()
        .body?.byteStream()

private fun <T : TypeContainer> CommandEvent<T>.tryResizeImage(
    image: Image,
    imageData: InputStream?
): ImmutableImage? = try {
    ImmutableImage.loader().fromStream(imageData)
        .fit(200, 200, Color.LIGHT_GRAY)
} catch (e: RuntimeException) {
    runBlocking {
        val sarah = guild?.getMember(Snowflake(116927399760756742L))
        respond("${sarah?.mention} Beim Bild ${image.fileUrl} gab es einen Fehler:")
        respond(e.stackTraceToString())
    }

    null
}

@OptIn(KordPreview::class)
fun MessageCreateBuilder.createButtons(images: List<Image>) {
    // indexed to deduplicate forEach label kekw
    (0 until ceil(images.size / 4f).toInt()).forEachIndexed { _, y ->
        actionRow {
            (0 until 4).forEach { x ->
                val index = x + y * 4
                val image = images.getOrNull(index) ?: return@forEach

                button("Bild ${index + 1}", Emojis.link) {
                    customId = "r34-${image.directory}-${image.image}"
                }
            }
        }
    }
}