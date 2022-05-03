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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.container

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasSlot
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.text.ContainerText
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

abstract class LabeledContainerScreen<C : Container>(
    guiRenderer: GUIRenderer,
    container: C,
    atlasElement: AtlasElement?,
    items: Int2ObjectOpenHashMap<AtlasSlot> = atlasElement?.slots ?: Int2ObjectOpenHashMap(),
) : BackgroundedContainerScreen<C>(guiRenderer, container, atlasElement, items) {
    protected val titleText = ContainerText.of(guiRenderer, atlasElement?.areas?.get("title"), container.title)
    protected val inventoryTitleText = ContainerText.createInventoryTitle(guiRenderer, atlasElement?.areas?.get("inventory_text"))

    override fun forceRenderContainerScreen(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRenderContainerScreen(offset, consumer, options)

        if (container.title != null) {
            titleText?.render(offset, consumer, options)
        }
        inventoryTitleText?.render(offset, consumer, options)
    }
}
