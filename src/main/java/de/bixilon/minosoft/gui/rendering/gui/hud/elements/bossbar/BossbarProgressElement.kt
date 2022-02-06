/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.util.ProgressElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import glm_.vec2.Vec2i

open class BossbarProgressElement(
    guiRenderer: GUIRenderer,
    emptyProgressAtlasElement: AtlasElement,
    fullProgressAtlasElement: AtlasElement,
    emptyNotchesElement: AtlasElement?,
    fullNotchesElement: AtlasElement?,
    progress: Float = 0.0f,
) : ProgressElement(guiRenderer, emptyProgressAtlasElement, fullProgressAtlasElement, progress) {
    private val emptyNotchesImage = emptyNotchesElement?.let { AtlasImageElement(guiRenderer, it, emptyProgressAtlasElement.size) }
    private val fullNotchesImage = fullNotchesElement?.let { AtlasImageElement(guiRenderer, it, emptyProgressAtlasElement.size) }

    constructor(guiRenderer: GUIRenderer, progressElements: Array<AtlasElement>, notchesElements: Array<AtlasElement>?, progress: Float = 0.0f) : this(guiRenderer, progressElements[0], progressElements[1], notchesElements?.get(0), notchesElements?.get(1), progress)

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        emptyImage.render(offset, consumer, options)
        emptyNotchesImage?.render(offset, consumer, options)
        progressImage.render(offset, consumer, options)
        fullNotchesImage?.render(offset, consumer, options)
    }
}
