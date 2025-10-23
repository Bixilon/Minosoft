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

package de.bixilon.minosoft.gui.rendering.models

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.minosoft.assets.MemoryAssetsManager
import de.bixilon.minosoft.assets.TestAssetsManager.box
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.loader.BlockLoader
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader.Companion.model
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.test.ITUtil.allocate
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object ModelTestUtil {
    private val cache = BlockLoader::class.java.getFieldOrNull("cache")!!

    fun createLoader(): ModelLoader {
        val instance = ModelLoader::class.java.allocate()
        instance::block.forceSet(BlockLoader::class.java.allocate())
        cache[instance.block] = HashMap<Any, Any>()


        return instance
    }

    fun ModelLoader.createAssets(files: Map<String, String>): MemoryAssetsManager {
        val assets = MemoryAssetsManager()

        for ((name, value) in files) {
            assets.push(name.toResourceLocation().model(), value)
        }
        this.block::assets.forceSet(assets.box())

        return assets
    }

    fun block(vararg elements: Int): FloatArray {
        val result = FloatArray(elements.size)

        for ((index, value) in elements.withIndex()) {
            result[index] = value / ModelElement.BLOCK_SIZE
        }

        return result
    }

    fun SingleBlockStateApply.bake(textures: TextureManager): BakedModel? {
        load(textures)
        return bake()
    }


    val rbgy = floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f)
    val bgyr = floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f)
    val gyrb = floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f)
    val yrbg = floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f)
}
