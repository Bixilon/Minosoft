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
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EmptyCollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.manager.SimpleStateManager
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.test.IT.NULL_CONNECTION
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["block"])
class CobwebTest : BlockTest<Block>() {

    init {
        CobwebTest0 = this
    }

    fun getStone() {
        super.retrieveBlock(CobwebBlock.identifier)
    }

    fun testOutlineShape() {
        if (block !is OutlinedBlock) throw AssertionError("Not shaped!")
        assertEquals(Shape.FULL, block.getOutlineShape(createSession(), BlockPosition.EMPTY, state))
    }

    fun testCollisionShape() {
        if (block !is CollidableBlock) return
        assertEquals(null, block.getCollisionShape(NULL_CONNECTION, EmptyCollisionContext, BlockPosition.EMPTY, state, null))
    }

    fun testStates() {
        assertTrue(block.states is SimpleStateManager)
    }

    fun testLightProperties() {
        state.testLightProperties(0, true, false, true, booleanArrayOf(true, true, true, true, true, true))
    }
}

var CobwebTest0: CobwebTest = CastUtil.unsafeNull()
