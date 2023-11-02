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

package de.bixilon.minosoft.gui.rendering.entities.model.human

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.observer.set.SetObserver.Companion.observeSet
import de.bixilon.minosoft.data.entities.entities.player.SkinParts.Companion.pack
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRenderer
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel

open class PlayerModel(
    renderer: PlayerRenderer<*>,
    model: BakedSkeletalModel,
) : HumanModel<PlayerRenderer<*>>(renderer, model) {
    private val shader = manager.playerShader
    private var skinParts = 0xFF

    init {
        renderer.entity::skinParts.observeSet(this, instant = true) { skinParts = renderer.entity.skinParts.pack() }
    }


    override fun draw() {
        val renderer = this.renderer.unsafeCast<PlayerRenderer<*>>()
        manager.context.system.reset(faceCulling = false) // TODO:  !renderSelf

        shader.use()
        shader.texture = renderer.skin?.shaderId ?: renderer.renderer.context.textures.debugTexture.shaderId
        shader.tint = renderer.light.value.mix(renderer.damage.value)
        shader.skinParts = this.skinParts


        manager.upload(instance, instance.matrix)
        instance.model.mesh.draw()
    }
}
