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

package de.bixilon.minosoft.gui.rendering.models

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.extend
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.building.WoolBlock
import de.bixilon.minosoft.data.registries.blocks.types.legacy.CustomBlockModel
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.createAssets
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.state.DirectBlockModel
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.variant.SingleVariantBlockModel
import de.bixilon.minosoft.gui.rendering.models.loader.BlockLoader.Companion.blockState
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["models"])
class BlockStateApplyTest {

    private fun loadModel(block: Block, state: String, version: Version = IT.VERSION, files: Map<String, String>): DirectBlockModel {
        val loader = ModelTestUtil.createLoader()
        loader.block::version.forceSet(version)
        val assets = loader.createAssets(files)
        val modelName = (if (block is CustomBlockModel) block.getModelName(version) else block.identifier).blockState()
        assets.push(modelName, state)


        return loader.block.loadState(block) ?: throw NullPointerException("empty block model!")
    }


    fun redWool() {
        val state = """{"variants":{"":{"model":"minecraft:block/red_wool"}}}"""
        val models = BlockModelTest.FILES.extend<String, String>(
            "block/red_wool" to """{"parent":"minecraft:block/cube_all","textures":{"all":"minecraft:block/red_wool"}}""",
        )
        val model = loadModel(WoolBlock.RedWool(settings = BlockSettings()), state, files = models)
        val texture = minecraft("block/red_wool").texture()

        assertEquals(model.unsafeCast<SingleVariantBlockModel>().apply, SingleBlockStateApply(
            model = BlockModel(
                BlockModelTest.CUBE_ALL_MODEL.guiLight,
                BlockModelTest.CUBE_ALL_MODEL.display,
                BlockModelTest.CUBE_ALL_MODEL.elements,
                textures = mapOf(
                    "particle" to texture,
                    "down" to texture,
                    "up" to texture,
                    "north" to texture,
                    "east" to texture,
                    "south" to texture,
                    "west" to texture,
                    "all" to texture,
                ),
                BlockModelTest.CUBE_ALL_MODEL.ambientOcclusion,
            ),
            x = 0,
            y = 0,
            uvLock = false,
            weight = 1,
        ))
    }


    // TODO: simple, variants, pre-flattening variants (e.g. grass snowy), multipart, multipart pre-flattening
    // TODO: model rename (silver wool vs light_gray_wool)

    // TODO: bakery
}
