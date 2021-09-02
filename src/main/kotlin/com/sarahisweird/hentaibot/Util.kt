package com.sarahisweird.hentaibot

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.lang.Integer.min
import kotlin.random.Random

fun <T> List<T>.multipleRandom(n: Int): List<T> {
    val indicesToKeep = mutableListOf<Int>()

    for (i in 0 until (min(n, this.size))) {
        var index: Int

        do index = Random.nextInt(this.size) while (indicesToKeep.contains(index))

        indicesToKeep += index
    }

    return this.filterIndexed { index, _ -> indicesToKeep.contains(index) }
}

fun List<Thread>.waitUntilDone(snoozeTime: Long) {
    while (this.find { it.isAlive } != null) {
        runBlocking { delay(snoozeTime) }
    }
}