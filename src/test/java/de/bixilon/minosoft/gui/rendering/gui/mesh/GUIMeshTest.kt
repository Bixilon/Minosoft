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

package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.kmath.vec.vec2.f.Vec2f
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GUIMeshTest {

    @Test
    fun transforming1() {
        val position = Vec2f(0, 0)
        val halfSize = Vec2f(1000, 1000) / 2

        assertEquals(Vec2f(-1.0f, 1.0f), GUIMeshBuilder.transformPosition(position, halfSize))
    }

    @Test
    fun transforming2() {
        val position = Vec2f(400, 600)
        val halfSize = Vec2f(1000, 1000) / 2

        assertEquals(Vec2f(-0.19999999f, -0.20000005f), GUIMeshBuilder.transformPosition(position, halfSize))
    }

    @Test
    fun transforming3() {
        val position = Vec2f(1000, 1000)
        val halfSize = Vec2f(1000, 1000) / 2

        assertEquals(Vec2f(1.0f, -1.0f), GUIMeshBuilder.transformPosition(position, halfSize))
    }
}
