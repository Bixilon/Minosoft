/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.bossbar

import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.util.ProgressElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import glm_.vec2.Vec2i

open class BossbarProgressElement(
    hudRenderer: HUDRenderer,
    emptyProgressAtlasElement: HUDAtlasElement,
    fullProgressAtlasElement: HUDAtlasElement,
    emptyNotchesElement: HUDAtlasElement?,
    fullNotchesElement: HUDAtlasElement?,
    progress: Float = 0.0f,
) : ProgressElement(hudRenderer, emptyProgressAtlasElement, fullProgressAtlasElement, progress) {
    private val emptyNotchesImage = emptyNotchesElement?.let { ImageElement(hudRenderer, it, emptyProgressAtlasElement.size) }
    private val fullNotchesImage = fullNotchesElement?.let { ImageElement(hudRenderer, it, emptyProgressAtlasElement.size) }

    constructor(hudRenderer: HUDRenderer, progressElements: Array<HUDAtlasElement>, notchesElements: Array<HUDAtlasElement>?, progress: Float = 0.0f) : this(hudRenderer, progressElements[0], progressElements[1], notchesElements?.get(0), notchesElements?.get(1), progress)

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        emptyImage.render(offset, z, consumer, options)
        emptyNotchesImage?.render(offset, z + 1, consumer, options)
        progressImage.render(offset, z + 2, consumer, options)
        fullNotchesImage?.render(offset, z + 3, consumer, options)

        return LAYERS
    }

    companion object {
        const val LAYERS = 4 // background, foreground * 2 (notches)
    }
}
