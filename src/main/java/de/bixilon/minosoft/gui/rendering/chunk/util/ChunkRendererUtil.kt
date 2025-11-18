/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.util

import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object ChunkRendererUtil {
    val STILL_LOADING_TIME = 20.milliseconds
    val MOVING_LOADING_TIME = 3.milliseconds


    // If the player is still, then we can load more chunks (to not cause lags)
    val ChunkRenderer.maxBusyTime: Duration
        get() {
            if (!limitChunkTransferTime) return Duration.INFINITE
            if (session.camera.entity.physics.velocity.isEmpty()) {
                return STILL_LOADING_TIME
            }
            return MOVING_LOADING_TIME
        }
}
