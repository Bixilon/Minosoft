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

package de.bixilon.minosoft.gui.rendering.camera.arm

import de.bixilon.kmath.mat.mat4.f.MMat4f
import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.primitive.FloatUtil.rad
import de.bixilon.minosoft.data.entities.entities.player.Arms
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.properties.textures.metadata.SkinModel
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition.FALLBACK_FAR_PLANE
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition.NEAR_PLANE
import de.bixilon.minosoft.gui.rendering.camera.CameraUtil
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRenderer
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRenderer.Companion.SKIN
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRenderer.Companion.SLIM
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRenderer.Companion.WIDE
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.renderer.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.system.base.IntegratedBufferTypes
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class ArmRenderer(override val context: RenderContext) : Renderer, Drawable {
    private var perspective = Mat4f()
    override val framebuffer get() = context.framebuffer.gui
    val shader = context.system.shader.create(minosoft("entities/player/arm")) { ArmShader(it) }

    override fun init(latch: AbstractLatch) {
        registerModels()
    }

    override fun postInit(latch: AbstractLatch) {
        shader.load()
        context.window::size.observe(this, true) {
            perspective = CameraUtil.perspective(60.0f.rad, it.x.toFloat() / it.y, NEAR_PLANE, FALLBACK_FAR_PLANE)
        }
    }

    private fun registerModels() {
        val skeletal = context.models.skeletal
        val override = mapOf(SKIN to context.textures.debugTexture) // disable textures, they all dynamic

        skeletal.register(LEFT_ARM_WIDE, WIDE, override) { ArmMeshBuilder(context, Arms.LEFT) }
        skeletal.register(RIGHT_ARM_WIDE, WIDE, override) { ArmMeshBuilder(context, Arms.RIGHT) }

        skeletal.register(LEFT_ARM_SLIM, SLIM, override) { ArmMeshBuilder(context, Arms.LEFT) }
        skeletal.register(RIGHT_ARM_SLIM, SLIM, override) { ArmMeshBuilder(context, Arms.RIGHT) }
    }


    private fun getModel(arm: Arms, model: SkinModel): BakedSkeletalModel? {
        val name = when {
            arm == Arms.LEFT && model == SkinModel.WIDE -> LEFT_ARM_WIDE
            arm == Arms.RIGHT && model == SkinModel.WIDE -> RIGHT_ARM_WIDE
            arm == Arms.LEFT && model == SkinModel.SLIM -> LEFT_ARM_SLIM
            arm == Arms.RIGHT && model == SkinModel.SLIM -> RIGHT_ARM_SLIM
            else -> Broken()
        }

        return context.models.skeletal[name]
    }

    override fun draw() {
        if (!context.camera.view.view.renderArm) return
        val entity = context.session.camera.entity.nullCast<PlayerEntity>() ?: return
        val renderer = entity.renderer?.nullCast<PlayerRenderer<*>>() ?: return
        val arm = entity.mainArm
        val skin = renderer.model?.type ?: return
        val model = getModel(arm, skin) ?: return

        context.system.clear(IntegratedBufferTypes.DEPTH_BUFFER)

        context.system.reset(faceCulling = true, depthTest = true, blending = true, depthMask = true)

        shader.use()
        shader.skinParts = renderer.model?.skinParts ?: 0xFF
        shader.texture = renderer.skin?.shaderId ?: context.textures.debugTexture.shaderId
        shader.tint = ChatColors.WHITE.rgb()

        val pivot = Vec3f((if (arm == Arms.RIGHT) 6f else -6f) / 16f, 24 / 16f, 0f)

        // TODO: arm animation
        val matrix = MMat4f().apply {
            translateAssign(Vec3f((if (arm == Arms.RIGHT) 23f / 16f else -23f / 16f), -17 / 16f, -0.7f))
            rotateXAssign(120.0f.rad)
            rotateYAssign((if (arm == Arms.RIGHT) -20.0f else 20.0f).rad)

            translateAssign(-pivot)
        }


        shader.transform = perspective * matrix


        model.mesh.draw()
    }


    companion object : RendererBuilder<ArmRenderer> {
        private val LEFT_ARM_WIDE = minosoft("left_arm_wide")
        private val RIGHT_ARM_WIDE = minosoft("right_arm_wide")
        private val LEFT_ARM_SLIM = minosoft("left_arm_slim")
        private val RIGHT_ARM_SLIM = minosoft("right_arm_slim")

        override fun build(session: PlaySession, context: RenderContext) = ArmRenderer(context)
    }
}
