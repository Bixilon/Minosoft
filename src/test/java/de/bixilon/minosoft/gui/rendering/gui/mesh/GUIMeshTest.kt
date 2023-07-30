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

package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.kotlinglm.vec2.Vec2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GUIMeshTest {

    @Test
    fun transforming1() {
        val position = Vec2(0, 0)
        val halfSize = Vec2(1000, 1000) / 2

        assertEquals(Vec2(-1.0, 1.0), GUIMesh.transformPosition(position, halfSize))
    }

    @Test
    fun transforming2() {
        val position = Vec2(400, 600)
        val halfSize = Vec2(1000, 1000) / 2

        assertEquals(Vec2(-0.19999999, -0.20000005), GUIMesh.transformPosition(position, halfSize))
    }

    @Test
    fun transforming3() {
        val position = Vec2(1000, 1000)
        val halfSize = Vec2(1000, 1000) / 2

        assertEquals(Vec2(1.0f, -1.0f), GUIMesh.transformPosition(position, halfSize))
    }
}
