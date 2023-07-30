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

package de.bixilon.minosoft.gui.rendering.gui.gui.popper

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.input.InputHandler
import de.bixilon.minosoft.gui.rendering.renderer.drawable.AsyncDrawable
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.Initializable

class PopperManager(
    private val guiRenderer: GUIRenderer,
) : Initializable, InputHandler, AsyncDrawable, Drawable {
    private val poppers: MutableList<PopperGUIElement> = mutableListOf()
    private var lastTickTime: Long = -1L


    fun onScreenChange() {
        for (element in poppers) {
            element.layout.forceSilentApply()
        }
    }

    override fun drawAsync() {
        val toRemove: MutableSet<PopperGUIElement> = mutableSetOf()
        val time = millis()
        val tick = time - lastTickTime > ProtocolDefinition.TICK_TIME
        if (tick) {
            lastTickTime = time
        }

        val latch = SimpleLatch(1)
        for (popper in poppers) {
            if (popper.layout.dead) {
                toRemove += popper
                popper.onClose()
                continue
            }
            if (tick) {
                popper.tick()
            }

            if (!popper.skipDraw) {
                popper.drawAsync()
            }
            latch.inc()
            popper.prepare()
            DefaultThreadPool += { popper.prepareAsync(); latch.dec() }
        }
        latch.dec()
        latch.await()

        poppers -= toRemove
    }

    override fun draw() {
        for (popper in poppers) {
            if (!popper.skipDraw) {
                popper.draw()
            }
            popper.postPrepare()

            guiRenderer.setup()
            if (!popper.enabled || popper.mesh.data.isEmpty) {
                continue
            }
            popper.mesh.draw()
        }
    }

    override fun onCharPress(char: Int): Boolean {
        for ((index, element) in poppers.toList().withIndex()) {
            if (index != 0 && !element.activeWhenHidden) {
                continue
            }
            if (element.onCharPress(char)) {
                return true
            }
        }
        return false
    }

    override fun onMouseMove(position: Vec2): Boolean {
        for ((index, element) in poppers.toList().withIndex()) {
            if (index != 0 && !element.activeWhenHidden) {
                continue
            }
            if (element.onMouseMove(position)) {
                return true
            }
        }
        return false
    }

    override fun onKey(code: KeyCodes, change: KeyChangeTypes): Boolean {
        for ((index, element) in poppers.toList().withIndex()) {
            if (index != 0 && !element.activeWhenHidden) {
                continue
            }
            if (element.onKey(code, change)) {
                return true
            }
        }
        return false
    }

    override fun onScroll(scrollOffset: Vec2): Boolean {
        for ((index, element) in poppers.toList().withIndex()) {
            if (index != 0 && !element.activeWhenHidden) {
                continue
            }
            if (element.onScroll(scrollOffset)) {
                return true
            }
        }
        return false
    }

    fun add(popper: Popper) {
        val guiElement = PopperGUIElement(popper)
        poppers += guiElement
        guiElement.onOpen()
    }

    operator fun plusAssign(popper: Popper) {
        add(popper)
    }

    fun remove(popper: Popper?) {
        for (popperEntry in poppers) {
            if (popperEntry.layout == popper) {
                poppers.remove(popperEntry)
                popperEntry.onClose()
                break
            }
        }
    }

    operator fun minusAssign(popper: Popper?) = remove(popper)

    fun clear() {
        for (popper in poppers) {
            popper.onClose()
        }
        poppers.clear()
    }
}
