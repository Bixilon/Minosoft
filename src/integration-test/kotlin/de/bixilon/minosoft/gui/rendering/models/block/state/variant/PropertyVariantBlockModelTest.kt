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

package de.bixilon.minosoft.gui.rendering.models.block.state.variant

import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.BlockStateApply
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["models"])
class PropertyVariantBlockModelTest {

    // fun readJson() {
    //     val model = DirectBlockModel.deserialize(TODO(), TODO())
    //     assertTrue(model is PropertyVariantBlockModel)
    //     model as PropertyVariantBlockModel
    //     assertEquals(model.variants, mapOf(
    //         mapOf(BlockProperties.FACING to Directions.EAST, BlockProperties.LIT to true) to null,
    //         mapOf(BlockProperties.FACING to Directions.WEST, BlockProperties.LIT to false) to null,
    //         mapOf(BlockProperties.FACING to Directions.SOUTH) to null,
    //     ))
    // }


    fun choosing() {
        val model = PropertyVariantBlockModel(mapOf(
            mapOf(BlockProperties.FACING to Directions.EAST, BlockProperties.LIT to true) to A,
            mapOf(BlockProperties.FACING to Directions.WEST, BlockProperties.LIT to false) to B,
            mapOf(BlockProperties.FACING to Directions.SOUTH) to C,
            mapOf(BlockProperties.LIT to false) to D,
        ))
        assertEquals(model.choose(PropertyBlockState(DummyBlock, mapOf(BlockProperties.FACING to Directions.EAST, BlockProperties.LIT to true), 0)), A)
        assertEquals(model.choose(PropertyBlockState(DummyBlock, mapOf(BlockProperties.FACING to Directions.WEST, BlockProperties.LIT to false), 0)), B)
        assertNull(model.choose(PropertyBlockState(DummyBlock, mapOf(BlockProperties.FACING to Directions.WEST, BlockProperties.LIT to true), 0)))

        assertEquals(model.choose(PropertyBlockState(DummyBlock, mapOf(BlockProperties.FACING to Directions.SOUTH, BlockProperties.LIT to false), 0)), C)
        assertEquals(model.choose(PropertyBlockState(DummyBlock, mapOf(BlockProperties.FACING to Directions.SOUTH, BlockProperties.LIT to true), 0)), C)
        assertTrue(model.choose(PropertyBlockState(DummyBlock, mapOf(BlockProperties.FACING to Directions.SOUTH), 0)) is C)

        assertEquals(model.choose(PropertyBlockState(DummyBlock, mapOf(BlockProperties.FACING to Directions.NORTH, BlockProperties.LIT to false), 0)), D)
        assertNull(model.choose(PropertyBlockState(DummyBlock, mapOf(BlockProperties.FACING to Directions.NORTH, BlockProperties.LIT to true), 0)))
        assertEquals(model.choose(PropertyBlockState(DummyBlock, mapOf(BlockProperties.LIT to false), 0)), D)
        assertNull(model.choose(PropertyBlockState(DummyBlock, mapOf(), 0)))
    }

    private object DummyBlock : Block(minosoft("test"), BlockSettings()) {
        override val hardness get() = Broken()
    }


    private object A : BlockStateApply {
        override fun load(textures: TextureManager) = Unit
        override fun bake() = null
    }

    private object B : BlockStateApply {
        override fun load(textures: TextureManager) = Unit
        override fun bake() = null
    }

    private object C : BlockStateApply {
        override fun load(textures: TextureManager) = Unit
        override fun bake() = null
    }

    private object D : BlockStateApply {
        override fun load(textures: TextureManager) = Unit
        override fun bake() = null
    }
}
