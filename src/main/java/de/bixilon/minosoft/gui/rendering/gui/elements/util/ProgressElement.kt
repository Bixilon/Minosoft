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

package de.bixilon.minosoft.gui.rendering.gui.elements.util

import de.bixilon.minosoft.gui.rendering.gui.AbstractGUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

open class ProgressElement(
    guiRenderer: AbstractGUIRenderer,
    val emptyAtlasElement: AtlasElement,
    val fullAtlasElement: AtlasElement,
    progress: Float = 0.0f,
) : Element(guiRenderer) {
    var progress = progress
        set(value) {
            if (field == value) {
                return
            }
            field = value
            forceSilentApply()
            // ToDo: Animate
        }
    protected val emptyImage = AtlasImageElement(guiRenderer, emptyAtlasElement)
    protected lateinit var progressImage: ImageElement


    constructor(guiRenderer: AbstractGUIRenderer, atlasElements: Array<AtlasElement>, progress: Float = 0.0f) : this(guiRenderer, atlasElements[0], atlasElements[1], progress)

    init {
        _size = emptyAtlasElement.size
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        emptyImage.render(offset, z, consumer, options)
        progressImage.render(offset, z + 1, consumer, options)

        return LAYERS
    }

    override fun forceSilentApply() {
        progressImage = ImageElement(guiRenderer, fullAtlasElement.texture, uvStart = fullAtlasElement.uvStart, uvEnd = Vec2(VecUtil.lerp(progress, fullAtlasElement.uvStart.x, fullAtlasElement.uvEnd.x), fullAtlasElement.uvEnd.y), size = Vec2i((fullAtlasElement.size.x * progress).toInt(), emptyAtlasElement.size.y))

        cacheUpToDate = false
    }

    companion object {
        const val LAYERS = 2 // background, foreground
    }
}
