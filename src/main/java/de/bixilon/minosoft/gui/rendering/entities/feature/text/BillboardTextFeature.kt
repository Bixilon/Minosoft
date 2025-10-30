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

package de.bixilon.minosoft.gui.rendering.entities.feature.text

import de.bixilon.kmath.mat.mat4.f.MMat4f
import de.bixilon.kmath.mat.mat4.f.Mat4Operations
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kutil.primitive.FloatUtil.rad
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.entities.feature.mesh.MeshedFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entities.visibility.EntityLayer
import de.bixilon.minosoft.gui.rendering.entities.visibility.EntityVisibilityLevels
import de.bixilon.minosoft.gui.rendering.font.renderer.component.ChatComponentRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.element.CharSpacing
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import kotlin.time.Duration

open class BillboardTextFeature(
    renderer: EntityRenderer<*>,
    text: ChatComponent?,
    offset: Float = DEFAULT_OFFSET,
) : MeshedFeature<Mesh>(renderer) {
    override val priority get() = 10000
    private var info: TextRenderInfo? = null
    private var matrix = MMat4f()
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

    override fun update(delta: Duration) {
        super.update(delta)
        if (unload || this.mesh == null) {
            val text = this.text ?: return
            if (text.length == 0) return
            createMesh(text)
        }
        updateMatrix()
    }

    protected open fun isInRenderDistance(): Boolean {
        return renderer.distance2 <= (RENDER_DISTANCE * RENDER_DISTANCE)
    }

    private fun createMesh(text: ChatComponent) {
        val mesh = BillboardTextMeshBuilder(renderer.renderer.context)
        val info = ChatComponentRenderer.render3d(renderer.renderer.context, PROPERTIES, MAX_SIZE, mesh, text)

        this.mesh = mesh.bake()
        this.info = info
    }

    private fun updateMatrix() {
        // TODO: update matrix only on demand (and maybe do the camera rotation somewhere else and cached)
        val width = this.info?.size?.x ?: return
        val mat = renderer.renderer.context.camera.view.view.rotation
        this.matrix.clearAssign()
        this.matrix.apply {
            translateYAssign(renderer.entity.dimensions.y + offset)
            rotateYAssign((EntityRotation.HALF_CIRCLE_DEGREE - mat.yaw).rad)
            rotateXAssign((180.0f - mat.pitch).rad)
            translateXAssign(width / -2.0f * BillboardTextMeshBuilder.SCALE); translateYAssign(-PROPERTIES.lineHeight * BillboardTextMeshBuilder.SCALE)
        }

        Mat4Operations.times(renderer.matrix.unsafe, matrix.unsafe, matrix)
    }

    override fun draw(mesh: Mesh) {
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
        shader.matrix = matrix.unsafe
        shader.tint = renderer.light.value
        super.draw(mesh)
    }

    override fun updateVisibility(level: EntityVisibilityLevels) = when {
        level < EntityVisibilityLevels.OCCLUDED -> super.updateVisibility(level)
        !isInRenderDistance() -> super.updateVisibility(EntityVisibilityLevels.OUT_OF_VIEW_DISTANCE)
        level == EntityVisibilityLevels.OCCLUDED -> super.updateVisibility(EntityVisibilityLevels.VISIBLE)
        else -> super.updateVisibility(level)
    }

    override fun unload() {
        this.info = null
        super.unload()
    }

    companion object {
        val PROPERTIES = TextRenderProperties(allowNewLine = false, shadow = false, charSpacing = CharSpacing(top = 1.0f, bottom = 1.0f))
        val MAX_SIZE = Vec2f(300.0f, PROPERTIES.lineHeight)
        const val DEFAULT_OFFSET = 0.25f
        const val RENDER_DISTANCE = 48
    }
}
