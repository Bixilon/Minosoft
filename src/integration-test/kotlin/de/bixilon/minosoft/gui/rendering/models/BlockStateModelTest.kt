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
import de.bixilon.minosoft.gui.rendering.models.raw.block.state.DirectBlockModel
import de.bixilon.minosoft.gui.rendering.models.raw.block.state.apply.BlockStateModel
import de.bixilon.minosoft.gui.rendering.models.raw.block.state.variant.SingleVariantBlockModel
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["models"])
class BlockStateModelTest {

    private fun loadModel(state: String, files: Map<String, String>): DirectBlockModel = TODO()


    fun redWool() {
        val state = """{"variants":{"":{"model":"minecraft:block/red_wool"}}}"""
        val models = BlockModelTest.FILES.extend<String, String>(
            "block/red_wool" to """{"parent":"minecraft:block/cube_all","textures":{"all":"minecraft:block/red_wool"}}""",
        )
        val model = loadModel(state, models)

        assertEquals(model.unsafeCast<SingleVariantBlockModel>().apply, BlockStateModel(
            model = BlockModelTest.CUBE_ALL_MODEL,
            x = 0,
            y = 0,
            uvLock = false,
            weight = 1,
        ))
    }
}
