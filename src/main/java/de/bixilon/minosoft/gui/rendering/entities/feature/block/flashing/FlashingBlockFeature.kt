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

package de.bixilon.minosoft.gui.rendering.entities.feature.block.flashing

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.entities.feature.block.BlockFeature
import de.bixilon.minosoft.gui.rendering.entities.feature.block.BlockMesh
import de.bixilon.minosoft.gui.rendering.entities.feature.block.BlockShader
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import kotlin.math.abs


open class FlashingBlockFeature(
    renderer: EntityRenderer<*>,
    state: BlockState?,
    scale: Vec3 = DEFAULT_SCALE,
    var flashColor: RGBColor = ChatColors.WHITE,
    var flashInterval: Float = 0.2f,
    var maxFlash: Float = 0.5f
) : BlockFeature(renderer, state, scale) {
    private var progress = 0.0f


    override fun update(millis: Long, delta: Float) {
        super.update(millis, delta)
        updateFlashing(delta)
    }

    private fun updateFlashing(delta: Float) {
        if (delta > flashInterval) return
        // TODO: exponential?
        progress += (delta / flashInterval)
        if (progress > maxFlash) {
            progress -= maxFlash * 2.0f
        }
    }

    override fun draw(mesh: BlockMesh, shader: BlockShader) {
        val shader = renderer.renderer.features.block.flashing
        shader.use()
        shader.flashColor = flashColor
        shader.flashProgress = abs(progress)
        super.draw(mesh, shader)
    }
}
