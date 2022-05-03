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

import de.bixilon.minosoft.data.container.types.processing.smelting.SmokerContainer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.ContainerGUIFactory
import kotlin.reflect.KClass

class SmokerContainerScreen(
    guiRenderer: GUIRenderer,
    container: SmokerContainer,
) : SmeltingContainerScreen<SmokerContainer>(guiRenderer, container, guiRenderer.atlasManager["minecraft:blast_furnace_container"], guiRenderer.atlasManager["minecraft:blast_furnace_container_fuel"], guiRenderer.atlasManager["minecraft:blast_furnace_container_process"]) {


    companion object : ContainerGUIFactory<SmokerContainerScreen, SmokerContainer> {
        override val clazz: KClass<SmokerContainer> = SmokerContainer::class

        override fun build(guiRenderer: GUIRenderer, container: SmokerContainer): SmokerContainerScreen {
            return SmokerContainerScreen(guiRenderer, container)
        }
    }
}
