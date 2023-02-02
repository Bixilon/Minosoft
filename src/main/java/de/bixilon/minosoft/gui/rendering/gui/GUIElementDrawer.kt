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

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.gui.rendering.RenderUtil.runAsync
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.renderer.drawable.AsyncDrawable
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

interface GUIElementDrawer {
    val guiRenderer: GUIRenderer
    var lastTickTime: Long

    fun tickElements(elements: Collection<GUIElement>) {
        val time = millis()
        val latch = CountUpAndDownLatch(1)
        if (time - lastTickTime > ProtocolDefinition.TICK_TIME) {
            for (element in elements) {
                if (!element.enabled) {
                    continue
                }
                latch.inc()
                guiRenderer.context.runAsync {
                    element.tick()
                    if (element is Pollable) {
                        if (element.poll()) {
                            element.apply()
                        }
                    }
                    latch.dec()
                }
            }

            lastTickTime = time
        }
        latch.dec()
        latch.await()
    }

    fun prepareElements(elements: Collection<GUIElement>) {
        val latch = CountUpAndDownLatch(1)
        for (element in elements) {
            if (!element.enabled) {
                continue
            }
            if (element !is AsyncDrawable) {
                continue
            }
            if (element.skipDraw) {
                continue
            }
            element.drawAsync()

            if (element is LayoutedGUIElement<*>) {
                latch.inc()
                element.prepare()
                guiRenderer.context.runAsync { element.prepareAsync(); latch.dec() }
            }
        }
        latch.dec()
        latch.await()
    }

    fun drawElements(elements: Collection<GUIElement>) {
        val latch = CountUpAndDownLatch(1)
        for (element in elements) {
            if (!element.enabled) {
                continue
            }
            if (element !is Drawable) {
                continue
            }
            if (element.skipDraw) {
                continue
            }
            element.draw()
        }
        latch.dec()
        latch.await()


        for (element in elements) {
            if (!element.enabled) {
                continue
            }
            if (element !is LayoutedGUIElement<*>) {
                continue
            }
            if (element.skipDraw) {
                continue
            }
            element.postPrepare()
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
