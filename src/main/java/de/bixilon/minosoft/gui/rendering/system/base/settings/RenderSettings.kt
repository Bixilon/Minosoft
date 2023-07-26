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

package de.bixilon.minosoft.gui.rendering.system.base.settings

import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions

data class RenderSettings(
    val depthTest: Boolean = true,
    val blending: Boolean = false,
    val faceCulling: Boolean = true,
    val polygonOffset: Boolean = false,
    val depthMask: Boolean = true,
    val sourceRGB: BlendingFunctions = BlendingFunctions.ONE,
    val destinationRGB: BlendingFunctions = BlendingFunctions.ONE_MINUS_SOURCE_ALPHA,
    val sourceAlpha: BlendingFunctions = BlendingFunctions.ONE,
    val destinationAlpha: BlendingFunctions = BlendingFunctions.ZERO,
    val depth: DepthFunctions = DepthFunctions.LESS_OR_EQUAL,
    val clearColor: RGBColor = Colors.TRANSPARENT,
    val polygonOffsetFactor: Float = 0.0f,
    val polygonOffsetUnit: Float = 0.0f,
)
