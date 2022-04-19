/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.world.entities

import de.bixilon.minosoft.assets.util.FileUtil.readJson
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.model.SkeletalModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture

class EntityModels(val renderWindow: RenderWindow) {
    private val unbakedModels: MutableMap<ResourceLocation, SkeletalModel> = mutableMapOf()
    val skeletal: MutableMap<ResourceLocation, BakedSkeletalModel> = mutableMapOf()

    @Synchronized
    fun loadUnbakedModel(path: ResourceLocation): SkeletalModel {
        return unbakedModels.getOrPut(path) { renderWindow.connection.assetsManager[path].readJson() }
    }

    fun loadModel(name: ResourceLocation, path: ResourceLocation, textureOverride: MutableMap<Int, AbstractTexture> = mutableMapOf()): BakedSkeletalModel {
        return skeletal.getOrPut(name) { loadUnbakedModel(path).bake(renderWindow, textureOverride) }
    }

    fun cleanup() {
        unbakedModels.clear()
    }
}
