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

package de.bixilon.minosoft.data.world.chunk.light

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.primitive.IntUtil.toHex
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import org.testng.Assert

object LightTestUtil {

    fun Chunk.assertLight(x: Int, y: Int, z: Int, expected: Int) {
        val light = this.light[x, y, z] and 0xFF
        Assert.assertEquals(light.toHex(2), expected.toHex(2))
    }

    fun World.assertLight(x: Int, y: Int, z: Int, expected: Int) {
        val light = this.getLight(Vec3i(x, y, z)) and 0xFF
        Assert.assertEquals(light.toHex(2), expected.toHex(2))
    }
}
