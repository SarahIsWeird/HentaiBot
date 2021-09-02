# HentaiBot

![Kotlin](https://img.shields.io/badge/Kotlin-1.5.10-blue?link=https://kotlinlang.org&logo=kotlin&logoColor=blue)
![DiscordKt](https://img.shields.io/badge/DiscordKt-0.22.0-blueviolet?logo=data:image/png;base64,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&link=https://github.com/DiscordKt/DiscordKt)
![OkHttp](https://img.shields.io/badge/OkHttp-4.9.0-brightgreen?link=https://square.github.io/okhttp/)
![Klaxon](https://img.shields.io/badge/Klaxon-5.5-blue?link=https://github.com/cbeust/klaxon)
![Scrimage](https://img.shields.io/badge/Scrimage-4.0.22-yellow?link=https://github.com/sksamuel/scrimage)

A Discord bot that can search through rule34.xxx, written in [Kotlin](https://kotlinlang.org) using
[DiscordKt](https://github.com/DiscordKt/DiscordKt).

## Usage:

Define your bot token as an environment variable called ``hentaibot_token``. The bot can then be started via the command
``java -jar HentaiBot-<version>.jar``.

## Commands:

### !rule34 \<tags>

Allows you to search through rule34.xxx. You can specify more than one tag. Same rules apply as in the normal Rule34
search.