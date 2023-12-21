/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class DebugOptionsTest {

    @Test
    fun `check that all debug options are turned off`() {
        assertFalse(DebugOptions.INFINITE_TORCHES)
        assertFalse(DebugOptions.SIMULATE_TIME)
        assertFalse(DebugOptions.CLOUD_RASTER)
        assertFalse(DebugOptions.LIGHTMAP_DEBUG_WINDOW)
        assertFalse(DebugOptions.LIGHT_DEBUG_MODE)
        assertFalse(DebugOptions.LOG_RAW_CHAT)
        assertFalse(DebugOptions.LIGHT_DEBUG_MODE)
        assertFalse(DebugOptions.FORCE_CHEST_ANIMATION)
        assertFalse(DebugOptions.EMPTY_BUFFERS)
    }
}
