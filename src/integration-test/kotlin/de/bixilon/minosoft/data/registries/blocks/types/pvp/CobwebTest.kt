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

package de.bixilon.minosoft.data.registries.blocks.types.pvp

import de.bixilon.kutil.cast.CastUtil
import de.bixilon.minosoft.data.registries.blocks.BlockTest
import de.bixilon.minosoft.data.registries.blocks.state.manager.SingleStateManager
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["block"])
class CobwebTest : BlockTest<CobwebBlock>() {

    init {
        CobwebTest0 = this
    }

    override val type get() = CobwebBlock.identifier

    fun testOutlineShape() {
        assertEquals(Shape.FULL, block.outlineShape)
    }

    fun testCollisionShape() {
        assertTrue(block !is CollidableBlock)
    }

    fun testStates() {
        assertTrue(block.states is SingleStateManager)
    }

    fun testLightProperties() {
        state.testLightProperties(0, true, false, true, booleanArrayOf(true, true, true, true, true, true))
    }
}

var CobwebTest0: CobwebTest = CastUtil.unsafeNull()
