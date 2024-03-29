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

package de.bixilon.minosoft.gui.rendering.entities.feature.text

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.entities.feature.properties.MeshedFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entities.visibility.EntityLayer
import de.bixilon.minosoft.gui.rendering.font.renderer.component.ChatComponentRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.element.CharSpacing
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.reset
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.translateXAssign
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.translateYAssign

open class BillboardTextFeature(
    renderer: EntityRenderer<*>,
    text: ChatComponent?,
    offset: Float = DEFAULT_OFFSET,
) : MeshedFeature<BillboardTextMesh>(renderer) {
    override val priority: Int get() = 10000
    private var info: TextRenderInfo? = null
    private var matrix = Mat4()
    var text: ChatComponent? = text
        set(value) {
            if (field == value) return
            field = value
            unload()
        }
    var offset: Float = offset
        set(value) {
            if (field == value) return
            field = value
        }

    override val layer get() = EntityLayer.Translucent

    override fun update(millis: Long, delta: Float) {
        if (!_enabled) return unload()
        if (!isInRenderDistance()) return unload()
        if (this.mesh == null) {
            val text = this.text ?: return unload()
            if (text.length == 0) return unload()
            createMesh(text)
        }
        updateMatrix()
    }

    protected open fun isInRenderDistance(): Boolean {
        return renderer.distance <= (RENDER_DISTANCE * RENDER_DISTANCE)
    }

    private fun createMesh(text: ChatComponent) {
        val mesh = BillboardTextMesh(renderer.renderer.context)
        val info = ChatComponentRenderer.render3d(renderer.renderer.context, PROPERTIES, MAX_SIZE, mesh, text)

        this.mesh = mesh
        this.info = info
    }

    private fun updateMatrix() {
        // TODO: update matrix only on demand (and maybe do the camera rotation somewhere else and cached)
        val width = this.info?.size?.x ?: return
        val mat = renderer.renderer.context.camera.view.view.rotation
        this.matrix.reset()
        this.matrix
            .translateYAssign(renderer.entity.dimensions.y + offset)
            .rotateYassign((EntityRotation.HALF_CIRCLE_DEGREE - mat.yaw).rad)
            .rotateXassign((180.0f - mat.pitch).rad)
            .translateXAssign(width / -2.0f * BillboardTextMesh.SCALE).translateYAssign(-PROPERTIES.lineHeight * BillboardTextMesh.SCALE)

        this.matrix = renderer.matrix * matrix
    }

    override fun draw(mesh: BillboardTextMesh) {
        renderer.renderer.context.system.reset(
            blending = true,
            sourceRGB = BlendingFunctions.SOURCE_ALPHA,
            destinationRGB = BlendingFunctions.ONE_MINUS_SOURCE_ALPHA,
            sourceAlpha = BlendingFunctions.SOURCE_ALPHA,
            destinationAlpha = BlendingFunctions.DESTINATION_ALPHA,
            depth = DepthFunctions.ALWAYS,
            faceCulling = false,
        )
        val shader = renderer.renderer.features.text.shader
        shader.use()
        shader.matrix = matrix
        shader.tint = renderer.light.value
        super.draw(mesh)
    }

    override fun updateVisibility(occluded: Boolean) {
        this.visible = true
    }

    override fun unload() {
        this.info = null
        super.unload()
    }

    companion object {
        val PROPERTIES = TextRenderProperties(allowNewLine = false, shadow = false, charSpacing = CharSpacing(top = 1.0f, bottom = 1.0f))
        val MAX_SIZE = Vec2(300.0f, PROPERTIES.lineHeight)
        const val DEFAULT_OFFSET = 0.25f
        const val RENDER_DISTANCE = 48
    }
}
