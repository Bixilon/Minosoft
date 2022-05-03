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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.registries.factory.clazz.mapping.DefaultClassMappingFactory
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.generic.GenericContainerScreen
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.processing.smelting.FurnaceContainerScreen

object ContainerGUIFactories : DefaultClassMappingFactory<ContainerGUIFactory<*, *>>(
    GenericContainerScreen,
    CraftingContainerScreen,

    FurnaceContainerScreen,
) {

    fun build(guiRenderer: GUIRenderer, container: Container): ContainerScreen<*>? {
        val factory = this.forObject(container)?.unsafeCast<ContainerGUIFactory<ContainerScreen<Container>, Container>>() ?: return null
        return factory.build(guiRenderer, container)
    }
}
