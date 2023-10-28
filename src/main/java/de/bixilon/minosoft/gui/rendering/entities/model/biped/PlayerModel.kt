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

package de.bixilon.minosoft.gui.rendering.entities.model.biped

import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureState
import de.bixilon.minosoft.gui.rendering.system.base.texture.skin.PlayerSkin

open class PlayerModel(
    renderer: EntityRenderer<*>,
    model: BakedSkeletalModel,
    val skin: PlayerSkin,
) : BipedModel(renderer, model) {
    override val shader get() = manager.playerShader

    override fun draw() {
        manager.context.system.reset(faceCulling = renderer.entity is LocalPlayerEntity) // TODO: and !renderSelf
        shader.use()
        shader.texture = if (skin.texture.state == DynamicTextureState.LOADED) skin.texture.shaderId else 0 // TODO: use default skins if not loaded yet
        manager.upload(instance)
        instance.model.mesh.draw()
    }
}
