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

package de.bixilon.minosoft.gui.rendering.entity.models.minecraft.player

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.entity.models.SkeletalEntityModel
import de.bixilon.minosoft.gui.rendering.models.ModelLoader.Companion.bbModel
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class PlayerModel(renderWindow: RenderWindow, player: PlayerEntity) : SkeletalEntityModel<PlayerEntity>(renderWindow, player) {
    override val instance = createModel()

    private fun createModel(): SkeletalInstance {
        val unbaked = renderWindow.modelLoader.entities.loadUnbakedModel(BB_MODEL)
        val texture = renderWindow.textureManager.getSkin(entity)
        val model = unbaked.bake(renderWindow, mutableMapOf(0 to texture))
        model.loadMesh(renderWindow)

        return SkeletalInstance(renderWindow, Vec3i(), model)
    }


    companion object {
        private val BB_MODEL = "minecraft:entities/player/steve".toResourceLocation().bbModel()
    }
}
