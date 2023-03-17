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

package de.bixilon.minosoft.data.registries.blocks.types.pvp

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil
import de.bixilon.minosoft.data.registries.blocks.BlockTest
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EmptyCollisionContext
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.shapes.voxel.AbstractVoxelShape
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.Assert.assertEquals
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
        assertEquals(AbstractVoxelShape.FULL, block.getOutlineShape(createConnection(), state))
    }

    fun testCollisionShape() {
        if (block !is CollidableBlock) return
        assertEquals(AbstractVoxelShape.EMPTY, block.getCollisionShape(EmptyCollisionContext, Vec3i.EMPTY, state, null))
    }

    fun testStates() {
        assertEquals(1, block.states.size)
        assertEquals(0, block.properties.size)
    }

    fun testLightProperties() {
        state.testLightProperties(0, true, false, true, booleanArrayOf(true, true, true, true, true, true))
    }
}

var CobwebTest0: CobwebTest = CastUtil.unsafeNull()
