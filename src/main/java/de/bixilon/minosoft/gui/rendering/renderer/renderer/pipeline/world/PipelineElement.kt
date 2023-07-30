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

package de.bixilon.minosoft.gui.rendering.renderer.renderer.pipeline.world

import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.layer.RenderLayer

class PipelineElement(
    val layer: RenderLayer,
    val shader: Shader?,
    val renderer: () -> Unit,
    val skip: (() -> Boolean)?
) : Comparable<PipelineElement> {

    fun draw(context: RenderContext) {
        if (skip != null && skip.invoke()) return

        context.system.set(layer.settings)
        shader?.use()
        renderer.invoke()
    }

    override fun compareTo(other: PipelineElement): Int {
        return layer.priority.compareTo(other.layer.priority)
    }
}
