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

package de.bixilon.minosoft.gui.rendering.entities.renderer.player

import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.data.entities.entities.player.properties.textures.metadata.SkinModel
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.factory.RegisteredEntityModelFactory
import de.bixilon.minosoft.gui.rendering.entities.feature.SkeletalFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.loader.SkeletalLoader.Companion.sModel
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.skin.PlayerSkin
import java.util.*

open class PlayerRenderer<E : PlayerEntity>(renderer: EntitiesRenderer, entity: E) : EntityRenderer<E>(renderer, entity) {
    private var properties: PlayerProperties? = null
    private var registered = false


    override fun update(millis: Long) {
        if (registered) return
        val update = updateProperties()

        val model = getModel()

        val instance = model?.createInstance(renderer.context) ?: return
        this.registered = true

        this.features += SkeletalFeature(this, instance)
    }

    private fun updateProperties(): Boolean {
        val properties = entity.additional.properties

        if (this.properties == properties) return false
        unload()
        this.properties = properties
        return true
    }

    open fun getSkin(): PlayerSkin? {
        return renderer.context.textures.skins.default[UUID.randomUUID()] // TODO
        // val properties = this.properties?.textures?.skin
        // if(properties == null){
        //    return renderer.context.textures.skins.getSkin(entity, properties, )
        //}
    }


    open fun getModel(): BakedSkeletalModel? {
        val skin = getSkin() ?: return null
        val name = when (skin.model) {
            SkinModel.WIDE -> WIDE
            SkinModel.SLIM -> SLIM
        }
        return renderer.context.models.skeletal[name]


        val properties = this.properties
        val model = properties?.textures?.skin?.metadata?.model ?: SkinModel.WIDE // TODO: failover according

    }


    companion object : RegisteredEntityModelFactory<PlayerEntity>, Identified {
        override val identifier get() = PlayerEntity.identifier
        private val WIDE = minecraft("entities/player/wide").sModel()
        private val SLIM = minecraft("entities/player/slim").sModel()

        private val SKIN = minecraft("skin")

        override fun create(renderer: EntitiesRenderer, entity: PlayerEntity) = PlayerRenderer(renderer, entity)

        override fun register(loader: ModelLoader) {
            val override = mapOf(SKIN to loader.context.textures.debugTexture) // no texture
            loader.skeletal.register(WIDE, override = override)
            loader.skeletal.register(SLIM, override = override)
        }
    }
}
