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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.container

import de.bixilon.minosoft.data.container.types.CraftingContainer
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import kotlin.reflect.KClass

class CraftingContainerScreen(guiRenderer: GUIRenderer, container: CraftingContainer) : LabeledContainerScreen<CraftingContainer>(guiRenderer, container, guiRenderer.atlas[ATLAS]?.get("container")) {


    companion object : ContainerGUIFactory<CraftingContainerScreen, CraftingContainer> {
        private val ATLAS = minecraft("container/crafting")
        override val clazz: KClass<CraftingContainer> = CraftingContainer::class

        override fun register(gui: GUIRenderer) {
            gui.atlas.load(ATLAS)
        }

        override fun build(gui: GUIRenderer, container: CraftingContainer): CraftingContainerScreen {
            return CraftingContainerScreen(gui, container)
        }
    }
}
