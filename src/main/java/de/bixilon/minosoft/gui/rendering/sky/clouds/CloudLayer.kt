/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.sky.clouds

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.hash.HashUtil.murmur64
import de.bixilon.kutil.random.RandomUtil.nextFloat
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import java.util.*
import kotlin.math.abs

class CloudLayer(
    private val sky: SkyRenderer,
    val clouds: CloudRenderer,
    val index: Int,
    var height: IntRange,
) {
    private var position = Vec2i(Int.MIN_VALUE)
    private val arrays: Array<CloudArray> = arrayOfNulls<CloudArray?>(3 * 3).unsafeCast()
    private var offset = 0.0f
    var movement = true
    private var day = -1L
    private var randomSpeed = 0.0f

    private fun push(from: Int, to: Int) {
        arrays[to].unload()
        arrays[to] = arrays[from]
    }

    private fun fill(index: Int) {
        val offset = Vec2i((index % 3) - 1, (index / 3) - 1)
        arrays[index] = CloudArray(this, position.cloudPosition() + offset)
    }

    fun pushX(negative: Boolean) {
        if (negative) {
            push(1, 0); push(2, 1)
            push(4, 3); push(5, 4)
            push(7, 6); push(8, 7)
            fill(2); fill(5); fill(8)
        } else {
            push(1, 2); push(0, 1)
            push(4, 5); push(3, 4)
            push(7, 8); push(6, 7)
            fill(0); fill(3); fill(6)
        }
    }

    fun pushZ(negative: Boolean) {
        if (negative) {
            push(3, 0); push(6, 3)
            push(4, 1); push(7, 4)
            push(5, 2); push(8, 5)
            fill(6); fill(7); fill(8)
        } else {
            push(3, 6); push(0, 3)
            push(4, 7); push(1, 4)
            push(5, 8); push(2, 5)
            fill(0); fill(1); fill(2)
        }
    }

    fun push(offset: Vec2i) {
        if (offset.x != 0) pushX(offset.x == 1)
        if (offset.y != 0) pushZ(offset.y == 1)
    }

    fun reset() {

    }

    private fun reset(cloudPosition: Vec2i) {
        for (array in arrays.unsafeCast<Array<CloudArray?>>()) {
            array?.unload()
        }
        for (x in -1..1) {
            for (z in -1..1) {
                arrays[(x + 1) + 3 * (z + 1)] = CloudArray(this, cloudPosition + Vec2i(x, z))
            }
        }
    }

    private fun Vec2i.cloudPosition(): Vec2i {
        return this shr 4
    }

    private fun calculateCloudPosition(): Vec2i {
        val offset = this.offset.toInt()
        return clouds.connection.player.physics.positionInfo.chunkPosition + Vec2i(offset / CloudArray.CLOUD_SIZE, 0)
    }

    private fun updatePosition() {
        val position = calculateCloudPosition()
        if (position == this.position) {
            return
        }

        val cloudPosition = position.cloudPosition()
        val arrayDelta = cloudPosition - this.position.cloudPosition()


        this.position = position
        if (abs(arrayDelta.x) > 1 || abs(arrayDelta.y) > 1) {
            // major position change (e.g. teleport)
            reset(cloudPosition)
        } else {
            push(arrayDelta)
        }
    }

    fun prepareAsync() {
        val day = sky.time.day
        if (day != this.day) {
            this.day = day
            randomSpeed = Random(index.toLong().murmur64() * (sky.time.age + 1000L).murmur64()).nextFloat(0.0f, 0.1f)
        }
    }

    fun prepare() {
        updateOffset()
        updatePosition()
    }

    private fun getCloudSpeed(): Float {
        return randomSpeed + 0.1f
    }

    private fun updateOffset() {
        if (!movement) {
            return
        }
        var offset = this.offset
        offset += getCloudSpeed() * clouds.delta * 10
        if (offset > MAX_OFFSET) {
            offset -= MAX_OFFSET
        }
        this.offset = offset
    }

    fun draw() {
        if (movement) {
            clouds.shader.offset = offset
        }


        for (array in arrays) {
            array.draw()
        }
    }

    fun unload() {
        for (array in arrays.unsafeCast<Array<CloudArray?>>()) {
            array?.unload()
        }
    }

    companion object {
        private const val MAX_OFFSET = CloudMatrix.CLOUD_MATRIX_MASK * CloudArray.CLOUD_SIZE
    }
}
