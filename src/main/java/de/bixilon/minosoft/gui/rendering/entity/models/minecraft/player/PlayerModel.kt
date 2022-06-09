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

import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.player.properties.textures.PlayerTexture.Companion.isSteve
import de.bixilon.minosoft.data.player.properties.textures.metadata.SkinModel
import de.bixilon.minosoft.gui.rendering.entity.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entity.models.SkeletalEntityModel
import de.bixilon.minosoft.gui.rendering.models.ModelLoader.Companion.bbModel
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.SkeletalAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.model.elements.SkeletalElement
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicStateChangeCallback
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureState
import de.bixilon.minosoft.protocol.packets.c2s.play.SettingsC2SP
import de.bixilon.minosoft.util.KUtil.toResourceLocation

open class PlayerModel(renderer: EntityRenderer, player: PlayerEntity) : SkeletalEntityModel<PlayerEntity>(renderer, player), DynamicStateChangeCallback {
    open val skinParts: Set<SettingsC2SP.SkinParts> = player.getSkinParts()
    private var skin: DynamicTexture? = null
    protected var refreshModel = false

    private var _instance: SkeletalInstance? = null
    private val animations: MutableList<SkeletalAnimation> = mutableListOf(LegAnimator(this), ArmAnimator(this))
    override var instance = createModel()


    private fun createModel(): SkeletalInstance {
        val skinModel = entity.tabListItem.properties?.textures?.skin?.metadata?.model ?: if (entity.uuid?.isSteve() == true) SkinModel.NORMAL else SkinModel.SLIM
        val unbaked = renderWindow.modelLoader.entities.loadUnbakedModel(if (skinModel == SkinModel.SLIM) SLIM_MODEL else NORMAL_MODEL)

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
        val skin = renderWindow.textureManager.getSkin(entity)
        skin.usages.incrementAndGet()
        this.skin?.usages?.decrementAndGet()
        this.skin = skin
        skin += this

        val model = unbaked.copy(elements = elements, animations = animations).bake(renderWindow, mapOf(0 to skin))

        val instance = SkeletalInstance(renderWindow, model)

        for (animation in animations) {
            instance.playAnimation(animation)
        }

        return instance
    }

    override fun prepareAsync() {
        if (refreshModel) {
            _instance = instance
            instance = createModel()
            refreshModel = false
        }
        super.prepareAsync()
    }

    override fun prepare() {
        _instance?.unload()
        _instance = null
        super.prepare()
    }

    private fun SkeletalElement.skinCopy(parts: Set<SettingsC2SP.SkinParts>, part: SettingsC2SP.SkinParts): SkeletalElement {
        if (part in parts) {
            return this
        }
        return this.copy(visible = false)
    }

    override fun onStateChange(texture: DynamicTexture, state: DynamicTextureState) {
        if (skin === texture) {
            refreshModel = true
        }
    }

    override fun unload() {
        skin?.usages?.decrementAndGet()
    }


    companion object {
        private val NORMAL_MODEL = "minecraft:entities/player/normal".toResourceLocation().bbModel()
        private val SLIM_MODEL = "minecraft:entities/player/slim".toResourceLocation().bbModel()
    }
}
