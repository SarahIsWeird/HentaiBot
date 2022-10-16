package com.sarahisweird.hentaibot.commands

import com.beust.klaxon.Klaxon
import com.sarahisweird.hentaibot.data.Image
import com.sarahisweird.hentaibot.database.entities.ServerSettings
import com.sarahisweird.hentaibot.util.multipleRandom
import com.sksamuel.scrimage.ImmutableImage
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.Guild
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.x.emoji.Emojis
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.extensions.button
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.Color
import java.awt.Graphics
import java.io.InputStream
import kotlin.concurrent.thread
import kotlin.math.ceil

suspend fun fetchRandomImages(
    client: OkHttpClient,
    klaxon: Klaxon,
    tags: List<String>,
    sentHashes: List<String>,
    respond: suspend (String) -> Unit,
    guild: Guild?
): List<Image>? {
    val bodyContent = getResponse(client, tags)

    if (bodyContent == null) {
        respond(ServerSettings.languageOf(guild).noResults)
        return null
    }

    val images = klaxon.parseArray<Image>(bodyContent)?.filter {
        !sentHashes.contains(it.hash)
    }?.multipleRandom(12).takeIf { it?.size != 0 }

    if (images == null) {
        respond(ServerSettings.languageOf(guild).noResults)
        return null
    }

    return images
}

private fun buildRequest(tags: List<String>) =
    Request.Builder().url("https://rule34.xxx/index.php?page=dapi&s=post&q=index" +
            "&limit=100&json=1&tags=${tags.joinToString("+")}").build()

private fun getResponse(client: OkHttpClient, tags: List<String>) =
    client.newCall(buildRequest(tags)).execute().body?.string().takeIf { it != "" }

private fun buildTagRequest(id: Int) =
    Request.Builder().url("https://rule34.xxx/index.php?page=dapi&s=post&q=index" +
            "&json=1&id=$id").build()

fun getTagResponse(client: OkHttpClient, id: Int) =
    client.newCall(buildTagRequest(id)).execute().body?.string().takeIf { it != "" }

fun createStitchingThread(
    client: OkHttpClient,
    image: Image,
    stitchedGraphics: Graphics,
    stitchToX: Int,
    stitchToY: Int
) = thread {
    val imageData = loadImageData(client, image)
    val resizedImage = tryResizeImage(imageData) ?: return@thread

    while (!stitchedGraphics.drawImage(resizedImage.awt(), stitchToX, stitchToY, null)) {
        runBlocking { delay(200) }
    }
}

private fun loadImageData(client: OkHttpClient, image: Image): InputStream? =
    client.newCall(Request.Builder().url(image.previewUrl).build()).execute()
        .body?.byteStream()

private fun tryResizeImage(imageData: InputStream?) = ImmutableImage.loader().fromStream(imageData)
        .fit(200, 200, Color.LIGHT_GRAY)

@OptIn(KordPreview::class)
fun createButtons(
    images: List<Image>,
    guild: Guild?,
    actionRow: (ActionRowBuilder.() -> Unit) -> Unit
) {
    // indexed to deduplicate forEach label kekw
    (0 until ceil(images.size / 4f).toInt()).forEachIndexed { _, y ->
        actionRow {
            (0 until 4).forEach { x ->
                val index = x + y * 4
                val image = images.getOrNull(index) ?: return@forEach

                button("${ServerSettings.languageOf(guild).picture} ${index + 1}", Emojis.mag) {
                    customId = "r34-${image.directory}-${image.image}-${image.id}"
                }
            }
        }
    }
}