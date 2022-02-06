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

package de.bixilon.minosoft.gui.rendering.gui

import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.renderer.Drawable
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

interface GUIElementDrawer {
    val guiRenderer: GUIRenderer
    var lastTickTime: Long

    fun drawElements(elements: Collection<GUIElement>) {
        val time = TimeUtil.time
        if (time - lastTickTime > ProtocolDefinition.TICK_TIME) {
            for (element in elements) {
                if (!element.enabled) {
                    continue
                }
                element.tick()
                if (element is Pollable) {
                    if (element.poll()) {
                        element.apply()
                    }
                }
            }

            lastTickTime = time
        }

        for (element in elements) {
            if (!element.enabled) {
                continue
            }
            if (element is Drawable && !element.skipDraw) {
                element.draw()
            }
            if (element is LayoutedGUIElement<*>) {
                element.prepare()
            }
        }

        guiRenderer.setup()
        for (element in elements) {
            if (element !is LayoutedGUIElement<*> || !element.enabled || element.skipDraw || element.mesh.data.isEmpty) {
                continue
            }
            element.mesh.draw()
        }
    }
}
