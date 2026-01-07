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

package de.bixilon.minosoft.data.registries.blocks

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.test.IT
import org.testng.Assert
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

abstract class BlockTest<T : Block> {
    private var _block: T? = null
    private var _state: BlockState? = null
    val block: T by lazy { setup(); _block!! }
    val state: BlockState by lazy { setup(); _state!! }


    abstract val type: Any

    protected fun setup() {
        if (_block != null) return

        val identifier = when (val type = this.type) {
            is ResourceLocation -> type
            is Identified -> type.identifier
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
        val block = IT.REGISTRIES.block[identifier]
        Assert.assertNotNull(block)
        block!!
        assertEquals(block.identifier, identifier)

        _block = block.unsafeCast()
        _state = block.states.default
    }

    @Test(groups = ["block"], priority = -1)
    open fun `retrieve block`() {
        setup()
    }

    companion object {
        fun BlockState.testLightProperties(
            luminance: Int,
            propagatesLight: Boolean,
            skylightEnters: Boolean,
            filtersSkylight: Boolean,
            propagates: BooleanArray,
        ) {
            assertEquals(this.luminance, luminance)
            val light = this.block.getLightProperties(this)
            assertEquals(light.propagatesLight, propagatesLight, "Mismatching: propagates")
            assertEquals(light.skylightEnters, skylightEnters, "Mismatching: enters")
            assertEquals(light.filtersSkylight, filtersSkylight, "Mismatching: filters")

            for (direction in Directions.VALUES) {
                assertEquals(light.propagatesLight(direction), propagates[direction.ordinal], "$direction failed")
            }
        }
    }
}
