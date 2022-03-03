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

package de.bixilon.minosoft.gui.rendering.skeletal.model

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.SkeletalAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.model.elements.SkeletalElement
import de.bixilon.minosoft.gui.rendering.skeletal.model.meta.SkeletalMeta
import de.bixilon.minosoft.gui.rendering.skeletal.model.outliner.SkeletalOutliner
import de.bixilon.minosoft.gui.rendering.skeletal.model.resolution.SkeletalResolution
import de.bixilon.minosoft.gui.rendering.skeletal.model.textures.SkeletalTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import glm_.vec3.Vec3
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

data class SkeletalModel(
    val meta: SkeletalMeta = SkeletalMeta(),
    val name: String = "empty",
    val geometryName: String = name,
    val visibleBox: Vec3 = Vec3.EMPTY,
    val resolution: SkeletalResolution = SkeletalResolution(),
    val elements: List<SkeletalElement> = listOf(),
    val outliner: List<SkeletalOutliner> = listOf(),
    val textures: List<SkeletalTexture> = listOf(),
    val animations: List<SkeletalAnimation> = listOf(),
) {
    fun bake(renderWindow: RenderWindow): BakedSkeletalModel {
        val textures: Int2ObjectOpenHashMap<AbstractTexture> = Int2ObjectOpenHashMap()
        for (entry in this.textures) {
            val texture = renderWindow.textureManager.staticTextures.createTexture(entry.resourceLocation)
            textures[entry.id] = texture
        }
        return BakedSkeletalModel(this, textures)
    }
}
