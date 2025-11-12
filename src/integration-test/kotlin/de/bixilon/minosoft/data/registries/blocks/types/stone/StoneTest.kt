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

package de.bixilon.minosoft.data.registries.blocks.types.stone

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.data.registries.blocks.BlockTest
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EmptyCollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.manager.SingleStateManager
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.StoneBlock
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.test.IT.NULL_CONNECTION
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["block"])
class StoneTest : BlockTest<StoneBlock.Block>() {

    init {
        StoneTest0 = this
    }

    fun getStone() {
        super.retrieveBlock(StoneBlock.Block.identifier)
    }

    fun testOutlineShape() {
        assertEquals(Shape.FULL, block.getOutlineShape(createSession(), BlockPosition.EMPTY, state))
    }

    fun testCollisionShape() {
        assertEquals(Shape.FULL, block.getCollisionShape(NULL_CONNECTION, EmptyCollisionContext, BlockPosition.EMPTY, state))
    }

    fun testStates() {
        assertTrue(block.states is SingleStateManager)
    }

    fun testLightProperties() {
        state.testLightProperties(0, false, false, true, booleanArrayOf(false, false, false, false, false, false))
    }
}

var StoneTest0: StoneTest = unsafeNull()
