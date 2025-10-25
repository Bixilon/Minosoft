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

package de.bixilon.minosoft.gui.rendering.entities.renderer.living.player

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.properties.textures.metadata.SkinModel
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.factory.RegisteredEntityModelFactory
import de.bixilon.minosoft.gui.rendering.entities.feature.text.score.EntityScoreFeature
import de.bixilon.minosoft.gui.rendering.entities.model.human.PlayerModel
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.LivingEntityRenderer
import de.bixilon.minosoft.gui.rendering.entities.visibility.EntityVisibility
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.loader.SkeletalLoader.Companion.sModel
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.mesh.SkeletalMeshBuilder
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureListener
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureState
import de.bixilon.minosoft.gui.rendering.system.base.texture.skin.SkinManager.Companion.isReallyWide
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

open class PlayerRenderer<E : PlayerEntity>(renderer: EntitiesRenderer, entity: E) : LivingEntityRenderer<E>(renderer, entity), DynamicTextureListener {
    var model: PlayerModel? = null
    var skin: DynamicTexture? = null
    private var refresh = true

    val score = EntityScoreFeature(this).register()

    init {
        entity.additional::properties.observe(this) { refresh = true }
    }

    private fun canSeeTeam(): Boolean {
        val camera = renderer.session.camera.entity.nullCast<PlayerEntity>() ?: return false
        val team = camera.additional.team ?: return false

        return team.canSee(entity.additional.team)
    }

    override fun updateVisibility(visibility: EntityVisibility) {
        if (visibility >= EntityVisibility.OCCLUDED) {
            this.isInvisible = entity.isInvisible && !canSeeTeam() // TODO: only update if that changes
        }
        super.updateVisibility(visibility)
    }


    override fun update(time: ValueTimeMark) {
        if (refresh) updateModel()
        super.update(time)
    }

    private fun setSkin(): SkinModel? {
        val skin = renderer.context.textures.skins.getSkin(entity, fetch = false, async = true) ?: return null
        this.skin?.removeListener(this)
        if (skin.texture.state == DynamicTextureState.LOADED) {
            this.skin = skin.texture
            if (skin.model == SkinModel.WIDE && renderer.profile.features.player.detectSlim) return if (skin.isReallyWide()) SkinModel.WIDE else SkinModel.SLIM
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

        return PlayerModel(this, model, skin)
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

    override fun updateMatrix(delta: Float) {
        super.updateMatrix(delta)
        when (entity.pose) {
            Poses.SNEAKING -> matrix.apply { translateYAssign(SNEAKING_OFFSET) } // TODO: interpolate
            else -> Unit
        }
    }


    companion object : RegisteredEntityModelFactory<PlayerEntity>, Identified, SkeletalMeshBuilder {
        override val identifier get() = PlayerEntity.identifier
        val WIDE = minecraft("entities/player/wide").sModel()
        val SLIM = minecraft("entities/player/slim").sModel()
        val SKIN = minecraft("skin")

        private const val SNEAKING_OFFSET = -0.125f

        override fun create(renderer: EntitiesRenderer, entity: PlayerEntity) = PlayerRenderer(renderer, entity)
        override fun buildMesh(context: RenderContext) = PlayerModelMeshBuilder(context)

        override fun register(loader: ModelLoader) {
            val override = mapOf(SKIN to loader.context.textures.debugTexture) // disable textures, they all dynamic
            loader.skeletal.register(WIDE, override = override, mesh = this)
            loader.skeletal.register(SLIM, override = override, mesh = this)
        }
    }
}
