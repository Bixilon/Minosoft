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
import de.bixilon.minosoft.gui.rendering.entity.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entity.models.SkeletalEntityModel
import de.bixilon.minosoft.gui.rendering.models.ModelLoader.Companion.bbModel
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.skeletal.model.elements.SkeletalElement
import de.bixilon.minosoft.protocol.packets.c2s.play.SettingsC2SP
import de.bixilon.minosoft.util.KUtil.toResourceLocation

open class PlayerModel(renderer: EntityRenderer, player: PlayerEntity) : SkeletalEntityModel<PlayerEntity>(renderer, player) {
    override val instance = createModel()
    open val skinParts: Set<SettingsC2SP.SkinParts> = player.getSkinParts()

    private fun createModel(): SkeletalInstance {
        val unbaked = renderWindow.modelLoader.entities.loadUnbakedModel(BB_MODEL)

        val elements: MutableList<SkeletalElement> = mutableListOf()
        elementLoop@ for (element in unbaked.elements) {
            for (skinPart in SettingsC2SP.SkinParts.VALUES) {
                if (skinPart.name == element.name) {
                    elements += element.skinCopy(skinParts, skinPart)
                    continue@elementLoop
                }
            }
            elements += element
        }
        val texture = renderWindow.textureManager.getSkin(entity)
        val model = unbaked.bake(renderWindow, mutableMapOf(0 to texture))

        return SkeletalInstance(renderWindow, Vec3i(), model)
    }

    private fun SkeletalElement.skinCopy(parts: Set<SettingsC2SP.SkinParts>, part: SettingsC2SP.SkinParts): SkeletalElement {
        if (part in parts) {
            return this
        }
        return this.copy(visible = false)
    }


    companion object {
        private val BB_MODEL = "minecraft:entities/player/steve".toResourceLocation().bbModel()
    }
}
