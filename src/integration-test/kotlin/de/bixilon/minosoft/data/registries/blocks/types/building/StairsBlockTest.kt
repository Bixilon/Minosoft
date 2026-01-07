/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.blocks.types.building

import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.kutil.unsafe.UnsafeUtil.setUnsafeAccessible
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.BlockTest.Companion.testLightProperties
import de.bixilon.minosoft.data.registries.blocks.properties.Halves
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.building.StairsBlock.Companion.FACING
import de.bixilon.minosoft.data.registries.blocks.types.building.StairsBlock.Companion.HALF
import de.bixilon.minosoft.data.registries.blocks.types.wood.Oak
import de.bixilon.minosoft.test.IT
import org.testng.AssertJUnit.assertEquals
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["blocks"])
class StairsBlockTest {
    private val block by lazy { IT.REGISTRIES_LEGACY.block[Oak.Stairs] ?: throw SkipException("") }
    private val state by lazy { block.states.default }
    private val stateModern by lazy { IT.REGISTRIES.block[Oak.Stairs]!!.states.default }
    private val getShape by lazy { StairsBlock::class.java.getDeclaredMethod("getShape", BlockState::class.java, Array<BlockState?>::class.java).apply { setUnsafeAccessible() } }

    fun BlockState.getShape(neighbours: Array<BlockState?>): StairsBlock.Shapes {
        assert(neighbours.size == 4)

        return getShape.invoke(this.block, this, arrayOfNulls<BlockState?>(2) + neighbours).cast()
    }


    fun `straight without neighbours`() {
        val state = this.state
        val neighbours = arrayOf<BlockState?>(null, null, null, null)
        val shape = state.getShape(neighbours)

        assertEquals(shape, StairsBlock.Shapes.STRAIGHT)
    }

    fun `straight with straight neighbours z`() {
        val state = this.state.withProperties(FACING to Directions.EAST)
        val neighbours = arrayOf(state, state, null, null)
        val shape = state.getShape(neighbours)

        assertEquals(shape, StairsBlock.Shapes.STRAIGHT)
    }

    fun `outer right on z axis`() {
        val state = this.state.withProperties(FACING to Directions.NORTH)
        val neighbours = arrayOf(this.state.withProperties(FACING to Directions.EAST), null, null, null)
        val shape = state.getShape(neighbours)

        assertEquals(shape, StairsBlock.Shapes.OUTER_RIGHT)
    }

    fun `outer left on z axis`() {
        val state = this.state.withProperties(FACING to Directions.NORTH)
        val neighbours = arrayOf(this.state.withProperties(FACING to Directions.WEST), null, null, null)
        val shape = state.getShape(neighbours)

        assertEquals(shape, StairsBlock.Shapes.OUTER_LEFT)
    }

    fun `inner right on z axis`() {
        val state = this.state.withProperties(FACING to Directions.NORTH)
        val neighbours = arrayOf(null, this.state.withProperties(FACING to Directions.EAST), null, null)
        val shape = state.getShape(neighbours)

        assertEquals(shape, StairsBlock.Shapes.INNER_RIGHT)
    }

    fun `inner left on z axis`() {
        val state = this.state.withProperties(FACING to Directions.NORTH)
        val neighbours = arrayOf(null, this.state.withProperties(FACING to Directions.WEST), null, null)
        val shape = state.getShape(neighbours)

        assertEquals(shape, StairsBlock.Shapes.INNER_LEFT)
    }

    fun `straight when neighbour other half`() {
        val state = this.state.withProperties(FACING to Directions.NORTH, HALF to Halves.LOWER)
        val neighbours = arrayOf(null, this.state.withProperties(FACING to Directions.WEST, HALF to Halves.UPPER), null, null)
        val shape = state.getShape(neighbours)

        assertEquals(shape, StairsBlock.Shapes.STRAIGHT)
    }

    fun `outer left on z axis, fucked`() {
        val state = this.state.withProperties(FACING to Directions.NORTH)
        val neighbours = arrayOf(this.state.withProperties(FACING to Directions.WEST), this.state.withProperties(FACING to Directions.WEST), null, null)
        val shape = state.getShape(neighbours)

        assertEquals(shape, StairsBlock.Shapes.OUTER_LEFT)
    }

    // TODO: test shape (x axis), collision/outline shape


    fun `light properties north`() {
        stateModern.withProperties(FACING to Directions.NORTH).testLightProperties(0, true, true, false, booleanArrayOf(false, true, false, true, true, true))
    }

    fun `light properties south`() {
        stateModern.withProperties(FACING to Directions.SOUTH).testLightProperties(0, true, true, false, booleanArrayOf(false, true, true, false, true, true))
    }

    fun `light properties west`() {
        stateModern.withProperties(FACING to Directions.WEST).testLightProperties(0, true, true, false, booleanArrayOf(false, true, true, true, false, true))
    }

    fun `light properties east`() {
        stateModern.withProperties(FACING to Directions.EAST).testLightProperties(0, true, true, false, booleanArrayOf(false, true, true, true, true, false))
    }
}
