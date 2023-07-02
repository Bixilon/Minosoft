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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.processing.smelting

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.minosoft.data.container.types.processing.smelting.SmeltingContainer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.processing.ProcessingContainerScreen
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

abstract class SmeltingContainerScreen<C : SmeltingContainer>(
    guiRenderer: GUIRenderer,
    container: C,
    atlasElement: AtlasElement?,
    protected val fuelAtlasElement: AtlasElement?,
    protected val processAtlasElement: AtlasElement?,
) : ProcessingContainerScreen<C>(guiRenderer, container, atlasElement), Pollable {
    private val fuelArea = atlasElement?.areas?.get("fuel")
    private val processArea = atlasElement?.areas?.get("process")
    private var fuel = 0.0f
    private var process = 0.0f

    override fun forceRenderContainerScreen(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRenderContainerScreen(offset, consumer, options)

        if (fuelArea != null) {
            val fuelImage = AtlasImageElement(guiRenderer, fuelAtlasElement, size = fuelArea.size)
            val fuel = fuel
            fuelImage.prefMaxSize.y = (fuelImage.size.y * fuel)
            fuelImage.uvStart = Vec2(fuelAtlasElement?.uvStart?.x ?: 0.0f, interpolateLinear(1.0f - fuel, fuelAtlasElement?.uvStart?.y ?: 0.0f, fuelAtlasElement?.uvEnd?.y ?: 0.0f))
            fuelImage.render(offset + fuelArea.start + Vec2i(0, fuelArea.size.y - fuelImage.size.y), consumer, options)
        }
        if (processArea != null) {
            val process = process
            val processImage = AtlasImageElement(guiRenderer, processAtlasElement, size = processArea.size)
            processImage.prefMaxSize.x = (processImage.size.x * process)
            processImage.uvEnd = Vec2(interpolateLinear(process, processAtlasElement?.uvStart?.x ?: 0.0f, processAtlasElement?.uvEnd?.x ?: 0.0f), processAtlasElement?.uvEnd?.y ?: 0.0f)
            processImage.render(offset + processArea.start, consumer, options)
        }
    }

    override fun poll(): Boolean {
        val fuel = container.fuel.toFloat() / maxOf(1, container.maxFuel)
        val process = container.processTime.toFloat() / maxOf(1, container.maxProcessTime)
        if (fuel != this.fuel || this.process != process) {
            this.fuel = fuel
            this.process = process
            return true
        }
        return false
    }

    override fun forceSilentApply() {
        cache.invalidate()
    }
}
