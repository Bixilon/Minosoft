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

package de.bixilon.minosoft.gui.rendering.camera.arm

import de.bixilon.kotlinglm.GLM
import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.data.entities.entities.player.Arms
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.properties.textures.metadata.SkinModel
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRenderer
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRenderer.Companion.SKIN
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRenderer.Companion.SLIM
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRenderer.Companion.WIDE
import de.bixilon.minosoft.gui.rendering.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.renderer.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.system.base.IntegratedBufferTypes
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class ArmRenderer(override val context: RenderContext) : Renderer, Drawable {
    private var perspective = Mat4()
    override val renderSystem = context.system
    override val framebuffer get() = context.framebuffer.gui
    val shader = context.system.createShader(minosoft("entities/player/arm")) { ArmShader(it) }

    init {
        registerModels()
    }

    override fun postInit(latch: AbstractLatch) {
        shader.load()
        context.connection.events.listen<ResizeWindowEvent> { perspective = GLM.perspective(60.0f.rad, it.size.aspect, CameraDefinition.NEAR_PLANE, CameraDefinition.FAR_PLANE) }
    }

    private fun registerModels() {
        val skeletal = context.models.skeletal
        val override = mapOf(SKIN to context.textures.debugTexture) // disable textures, they all dynamic

        skeletal.register(LEFT_ARM_WIDE, WIDE, override) { ArmMesh(context, Arms.LEFT) }
        skeletal.register(RIGHT_ARM_WIDE, WIDE, override) { ArmMesh(context, Arms.RIGHT) }

        skeletal.register(LEFT_ARM_SLIM, SLIM, override) { ArmMesh(context, Arms.LEFT) }
        skeletal.register(RIGHT_ARM_SLIM, SLIM, override) { ArmMesh(context, Arms.RIGHT) }
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
        val entity = context.connection.camera.entity.nullCast<PlayerEntity>() ?: return
        val renderer = entity.renderer?.nullCast<PlayerRenderer<*>>() ?: return
        val arm = entity.mainArm
        val skin = renderer.model?.type ?: return
        val model = getModel(arm, skin) ?: return

        context.system.clear(IntegratedBufferTypes.DEPTH_BUFFER)

        context.system.reset(faceCulling = false, depthTest = true, blending = true, depthMask = true)

        shader.use()
        shader.skinParts = renderer.model?.skinParts ?: 0xFF
        shader.texture = renderer.skin?.shaderId ?: context.textures.debugTexture.shaderId
        shader.tint = ChatColors.WHITE

        val pivot = Vec3((if (arm == Arms.RIGHT) 4 else -4) / 16f, 24 / 16f, 0)

        // TODO: arm animation
        val matrix = Mat4()
            .translateAssign(Vec3((if (arm == Arms.RIGHT) 20f / 16f else -20f / 16f), -11 / 16f, -0.2f))
            .rotateXassign(120.0f.rad)
            .rotateYassign((if (arm == Arms.RIGHT) -5.0f else 5.0f).rad)

            .translateAssign(-pivot)


        shader.transform = perspective * matrix


        model.mesh.draw()
    }


    companion object : RendererBuilder<ArmRenderer> {
        private val LEFT_ARM_WIDE = minosoft("left_arm_wide")
        private val RIGHT_ARM_WIDE = minosoft("right_arm_wide")
        private val LEFT_ARM_SLIM = minosoft("left_arm_slim")
        private val RIGHT_ARM_SLIM = minosoft("right_arm_slim")

        override fun build(connection: PlayConnection, context: RenderContext) = ArmRenderer(context)
    }
}
