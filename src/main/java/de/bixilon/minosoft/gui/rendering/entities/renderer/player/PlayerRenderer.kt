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

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.properties.textures.metadata.SkinModel
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.factory.RegisteredEntityModelFactory
import de.bixilon.minosoft.gui.rendering.entities.model.biped.PlayerModel
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.loader.SkeletalLoader.Companion.sModel
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.mesh.SkeletalMeshBuilder
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureListener
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureState

open class PlayerRenderer<E : PlayerEntity>(renderer: EntitiesRenderer, entity: E) : EntityRenderer<E>(renderer, entity), DynamicTextureListener {
    protected var model: PlayerModel? = null
    var skin: DynamicTexture? = null
    private var refresh = true

    init {
        entity.additional::properties.observe(this) { refresh = true }
    }


    override fun update(millis: Long) {
        if (refresh) updateModel()
        super.update(millis)
    }

    private fun setSkin(): SkinModel? {
        val skin = renderer.context.textures.skins.getSkin(entity, fetch = false, async = true) ?: return null
        this.skin?.removeListener(this)
        if (skin.texture.state == DynamicTextureState.LOADED) {
            this.skin = skin.texture
            return skin.model
        } else {
            this.skin = skin.default
            skin.texture.addListener(this)
        }

        return skin.model
    }

    private fun updateModel() {
        this.model?.let { this.features -= it }
        val model = createModel()
        this.model = model
        this.refresh = false
        if (model == null) return

        this.features += model
    }

    private fun createModel(): PlayerModel? {
        val skin = setSkin() ?: return null
        return createModel(skin)
    }

    protected open fun createModel(skin: SkinModel): PlayerModel? {
        val model = getModel(skin) ?: return null

        return PlayerModel(this, model)
    }


    private fun getModel(skin: SkinModel): BakedSkeletalModel? {
        val name = when (skin) {
            SkinModel.WIDE -> WIDE
            SkinModel.SLIM -> SLIM
        }
        return renderer.context.models.skeletal[name]
    }

    override fun onDynamicTextureChange(texture: DynamicTexture): Boolean {
        if (texture.state != DynamicTextureState.LOADED) return false
        this.skin = texture
        return true
    }


    companion object : RegisteredEntityModelFactory<PlayerEntity>, Identified, SkeletalMeshBuilder {
        override val identifier get() = PlayerEntity.identifier
        private val WIDE = minecraft("entities/player/wide").sModel()
        private val SLIM = minecraft("entities/player/slim").sModel()

        private val SKIN = minecraft("skin")

        override fun create(renderer: EntitiesRenderer, entity: PlayerEntity) = PlayerRenderer(renderer, entity)
        override fun buildMesh(context: RenderContext) = PlayerModelMesh(context)

        override fun register(loader: ModelLoader) {
            val override = mapOf(SKIN to loader.context.textures.debugTexture) // disable textures, they all dynamic
            loader.skeletal.register(WIDE, override = override, mesh = this)
            loader.skeletal.register(SLIM, override = override, mesh = this)
        }
    }
}
