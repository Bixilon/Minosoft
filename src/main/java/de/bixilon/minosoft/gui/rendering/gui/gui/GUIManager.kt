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

package de.bixilon.minosoft.gui.rendering.gui.gui

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.gui.GUIElement
import de.bixilon.minosoft.gui.rendering.gui.GUIElementDrawer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.Dragged
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.ContainerGUIManager
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.pause.PauseMenu
import de.bixilon.minosoft.gui.rendering.gui.hud.Initializable
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.input.DraggableHandler
import de.bixilon.minosoft.gui.rendering.input.InputHandler
import de.bixilon.minosoft.gui.rendering.renderer.Drawable
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i

class GUIManager(
    override val guiRenderer: GUIRenderer,
) : Initializable, InputHandler, GUIElementDrawer, DraggableHandler {
    private val elementCache: MutableMap<GUIBuilder<*>, GUIElement> = mutableMapOf()
    var elementOrder: MutableList<GUIElement> = mutableListOf()
    private val renderWindow = guiRenderer.renderWindow
    internal var paused = false
    override var lastTickTime: Long = -1L

    override fun init() {
        for (element in elementCache.values) {
            element.init()
        }
        registerDefaultElements()
    }

    private fun registerDefaultElements() {
        ContainerGUIManager.register(guiRenderer)
    }

    override fun postInit() {
        renderWindow.inputHandler.registerKeyCallback("minosoft:back".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyAction.RELEASE to setOf(KeyCodes.KEY_ESCAPE),
                ),
                ignoreConsumer = true,
            )) { popOrPause() }


        for (element in elementCache.values) {
            element.postInit()
            if (element is LayoutedGUIElement<*>) {
                element.initMesh()
            }
        }
    }

    fun onMatrixChange() {
        for (element in elementCache.values) {
            // ToDo: Just the current active one
            if (element is LayoutedGUIElement<*>) {
                element.element.forceSilentApply()
            }
            element.apply()
        }
    }

    fun draw() {
        val order = elementOrder.reversed()
        val time = TimeUtil.time
        val tick = time - lastTickTime > ProtocolDefinition.TICK_TIME
        if (tick) {
            lastTickTime = time
        }
        for ((index, element) in order.withIndex()) {
            if (!element.enabled) {
                continue
            }
            if (index != order.size - 1 && !element.activeWhenHidden) {
                continue
            }
            if (tick) {
                element.tick()
                if (element is Pollable) {
                    if (element.poll()) {
                        element.apply()
                    }
                }

                lastTickTime = time
            }

            if (element is Drawable && !element.skipDraw) {
                element.draw()
            }
            if (element is LayoutedGUIElement<*>) {
                element.prepare()
            }

            guiRenderer.setup()
            if (element !is LayoutedGUIElement<*> || !element.enabled || element.mesh.data.isEmpty) {
                continue
            }
            element.mesh.draw()
        }
    }

    fun pause(pause: Boolean = !paused) {
        if (pause == paused) {
            return
        }

        paused = pause
        if (pause) {
            if (elementOrder.isNotEmpty()) {
                return
            }
            open(PauseMenu)
        } else {
            clear()
        }
    }

    private fun runForEach(run: (element: GUIElement) -> Boolean): Boolean {
        for ((index, element) in elementOrder.toList().withIndex()) {
            if (index != 0 && !element.activeWhenHidden) {
                continue
            }
            if (run(element)) {
                return true
            }
        }
        return false
    }

    override fun onCharPress(char: Int): Boolean {
        return runForEach { it.onCharPress(char) }
    }

    override fun onMouseMove(position: Vec2i): Boolean {
        return runForEach { it.onMouseMove(position) }
    }

    override fun onKey(type: KeyChangeTypes, key: KeyCodes): Boolean {
        return runForEach { it.onKey(type, key) }
    }

    override fun onScroll(scrollOffset: Vec2d): Boolean {
        return runForEach { it.onScroll(scrollOffset) }
    }

    private fun runForEachDrag(run: (element: GUIElement) -> Element?): Element? {
        for ((index, element) in elementOrder.toList().withIndex()) {
            if (index != 0 && !element.activeWhenHidden) {
                continue
            }

            run(element)?.let { return it }
        }
        return null
    }

    override fun onDragMove(position: Vec2i, dragged: Dragged): Element? {
        return runForEachDrag { it.onDragMove(position, dragged) }
    }

    override fun onDragKey(type: KeyChangeTypes, key: KeyCodes, dragged: Dragged): Element? {
        return runForEachDrag { it.onDragKey(type, key, dragged) }
    }

    override fun onDragScroll(scrollOffset: Vec2d, dragged: Dragged): Element? {
        return runForEachDrag { it.onDragScroll(scrollOffset, dragged) }
    }

    override fun onDragChar(char: Int, dragged: Dragged): Element? {
        return runForEachDrag { it.onDragChar(char, dragged) }
    }

    fun open(builder: GUIBuilder<*>) {
        clear()
        push(builder)
    }

    fun popOrPause() {
        if (elementOrder.isEmpty()) {
            return pause()
        }
        pop()
    }

    fun push(builder: GUIBuilder<*>) {
        _push(this[builder])
    }

    private fun _push(element: GUIElement) {
        if (elementOrder.isEmpty()) {
            renderWindow.inputHandler.inputHandler = guiRenderer
        }
        for ((index, elementEntry) in elementOrder.toList().withIndex()) {
            if (index != 0 && !elementEntry.activeWhenHidden) {
                continue
            }
            elementEntry.onHide()
        }
        elementOrder.add(0, element)
        element.onOpen()
        onMouseMove(guiRenderer.currentCursorPosition)
    }

    @Deprecated("Only use for dynamic gui (e.g. dialogs, ...)")
    fun push(element: LayoutedElement) {
        val layouted = LayoutedGUIElement(element)
        layouted.init()
        layouted.postInit()
        _push(layouted)
    }

    fun pop(element: GUIElement) {
        TODO("Not yet implemented")
    }

    fun pop() {
        if (guiRenderer.dragged.element != null) {
            guiRenderer.dragged.element = null
            return
        }
        val previous = elementOrder.removeFirstOrNull() ?: return
        previous.onClose()
        if (elementOrder.isEmpty()) {
            renderWindow.inputHandler.inputHandler = null
            guiRenderer.popper.clear()
            guiRenderer.dragged.element = null
        }
        val now = elementOrder.firstOrNull() ?: return
        now.onOpen()
    }

    fun clear() {
        for (element in elementOrder) {
            element.onClose()
        }
        elementOrder.clear()
        guiRenderer.popper.clear()
        guiRenderer.dragged.element = null
        renderWindow.inputHandler.inputHandler = null
    }

    operator fun <T : GUIElement> get(builder: GUIBuilder<T>): T {
        return elementCache.getOrPut(builder) {
            if (builder is HUDBuilder<*>) {
                guiRenderer.hud[builder]?.let { return it.unsafeCast() }
            }
            val element = builder.build(guiRenderer)
            element.init()
            element.postInit()
            return@getOrPut element
        }.unsafeCast() // init mesh
    }
}
