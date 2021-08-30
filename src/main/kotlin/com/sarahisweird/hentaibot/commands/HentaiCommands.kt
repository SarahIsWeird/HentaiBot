package com.sarahisweird.hentaibot.commands

import com.beust.klaxon.Klaxon
import com.sarahisweird.hentaibot.data.Image
import me.jakejmattson.discordkt.api.arguments.AnyArg
import me.jakejmattson.discordkt.api.dsl.commands
import okhttp3.OkHttpClient
import okhttp3.Request

private val client = OkHttpClient()
private val klaxon = Klaxon()

private val sentHashes = mutableListOf<String>()

@Suppress("BlockingMethodInNonBlockingContext")
fun hentaiCommands() = commands("Hentai") {
    command("r34") {
        description = "Durchsucht rule34.xxx nach Tags. Du kannst mehrere Tags angeben," +
                " indem du sie mit '+' trennst."

        execute(AnyArg) {
            val request = Request.Builder().url(
                "https://rule34.xxx/index.php?page=dapi&s=post&q=index" +
                        "&limit=50&json=1&tags=${args.first}"
            ).build()

            val response = client.newCall(request).execute()
            val bodyContent = response.body?.string()

            if (bodyContent == null || bodyContent == "") {
                respond("Das hat nicht geklappt. :(")
                return@execute
            }

            val image = klaxon.parseArray<Image>(bodyContent)?.filter {
                !sentHashes.contains(it.hash)
            }?.randomOrNull()

            if (image != null) sentHashes += image.hash

            respond(image?.fileUrl ?: "Es wurde nix gefunden :(")
        }
    }
}