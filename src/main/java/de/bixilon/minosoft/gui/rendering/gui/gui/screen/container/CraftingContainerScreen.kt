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
import de.bixilon.minosoft.data.container.types.CraftingContainer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.text.ContainerText
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import kotlin.reflect.KClass

class CraftingContainerScreen(guiRenderer: GUIRenderer, container: CraftingContainer) : BackgroundedContainerScreen<CraftingContainer>(guiRenderer, container, guiRenderer.atlasManager["minecraft:crafting_container".toResourceLocation()]) {
    private val title = ContainerText.of(guiRenderer, atlasElement?.areas?.get("crafting_text"), container.title)
    private val inventoryTitle = ContainerText.createInventoryTitle(guiRenderer, atlasElement?.areas?.get("inventory_text"))


    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)
        val centerOffset = offset + (size - containerBackground.size) / 2
        if (container.title != null) {
            title?.render(centerOffset, consumer, options)
        }
        inventoryTitle?.render(centerOffset, consumer, options)
    }

    companion object : ContainerGUIFactory<CraftingContainerScreen, CraftingContainer> {
        override val clazz: KClass<CraftingContainer> = CraftingContainer::class

        override fun build(guiRenderer: GUIRenderer, container: CraftingContainer): CraftingContainerScreen {
            return CraftingContainerScreen(guiRenderer, container)
        }
    }
}
