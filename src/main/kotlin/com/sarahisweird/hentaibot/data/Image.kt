package com.sarahisweird.hentaibot.data

import com.beust.klaxon.Json

data class Image(
    @Json("preview_url")
    val previewUrl: String,

    @Json("sample_url")
    val sampleUrl: String,

    @Json("file_url")
    val fileUrl: String,

    @Json("directory")
    val directory: Int,

    @Json("hash")
    val hash: String,

    @Json("height")
    val height: Int,

    @Json("id")
    val id: Int,

    @Json("image")
    val image: String,

    @Json("change")
    val change: Long,

    @Json("owner")
    val owner: String,

    @Json("parent_id")
    val parentId: Int,

    @Json("rating")
    val rating: String,

    @Json("sample")
    val sample: Int,

    @Json("sample_height")
    val sampleHeight: Int,

    @Json("sample_width")
    val sampleWidth: Int,

    @Json("score")
    val score: Int,

    @Json("tags")
    val tags: String,

    @Json("width")
    val width: Int
)