package com.sarahisweird.hentaibot.data

import com.beust.klaxon.Json

class Image(
    @Json(name = "file_url")
    val fileUrl: String,

    @Json(name = "hash")
    val hash: String
)