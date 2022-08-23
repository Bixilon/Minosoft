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

import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.gui.GUIElement
import de.bixilon.minosoft.gui.rendering.gui.GUIElementDrawer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.Dragged
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.CreditsScreen
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.SignEditorScreen
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.ContainerGUIManager
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.pause.PauseMenu
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.pause.RespawnMenu
import de.bixilon.minosoft.gui.rendering.gui.hud.Initializable
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.input.DraggableHandler
import de.bixilon.minosoft.gui.rendering.input.InputHandler
import de.bixilon.minosoft.gui.rendering.renderer.Drawable
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class GUIManager(
    override val guiRenderer: GUIRenderer,
) : Initializable, InputHandler, GUIElementDrawer, DraggableHandler {
    private val elementCache: MutableMap<GUIBuilder<*>, GUIElement> = mutableMapOf()
    private var orderLock = SimpleLock()
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
        SignEditorScreen.register(guiRenderer)
        RespawnMenu.register(guiRenderer)
        CreditsScreen.register(guiRenderer)
    }

    override fun postInit() {
        renderWindow.inputHandler.registerKeyCallback(
            "minosoft:back".toResourceLocation(),
            KeyBinding(
                KeyActions.RELEASE to setOf(KeyCodes.KEY_ESCAPE),
                ignoreConsumer = true,
            )
        ) { popOrPause() }


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
        orderLock.acquire()
        for (element in elementOrder) {
            if (element is LayoutedGUIElement<*>) {
                element.element.forceSilentApply()
            }
            element.apply()
        }
        orderLock.release()
    }

    fun draw() {
        orderLock.acquire()
        val order = elementOrder.reversed()
        orderLock.release()
        val time = TimeUtil.millis
        val tick = time - lastTickTime > ProtocolDefinition.TICK_TIME
        if (tick) {
            lastTickTime = time
        }
        val latch = CountUpAndDownLatch(1)
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
                latch.inc()
                DefaultThreadPool += { element.prepareAsync(); latch.dec() }
            }
        }
        latch.dec()
        latch.await()

        for ((index, element) in order.withIndex()) {
            if (!element.enabled) {
                continue
            }
            if (index != order.size - 1 && !element.activeWhenHidden) {
                continue
            }
            if (element is LayoutedGUIElement<*>) {
                element.postPrepare()
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
        orderLock.acquire()
        val copy = elementOrder.toList()
        orderLock.release()
        for ((index, element) in copy.withIndex()) {
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
        orderLock.acquire()
        val copy = elementOrder.toList()
        orderLock.release()
        for ((index, element) in copy.withIndex()) {
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
        orderLock.acquire()
        val copy = elementOrder.toList()
        orderLock.release()
        for ((index, elementEntry) in copy.withIndex()) {
            if (index != 0 && !elementEntry.activeWhenHidden) {
                continue
            }
            elementEntry.onHide()
        }
        orderLock.lock()
        elementOrder.add(0, element)
        orderLock.unlock()
        element.onOpen()
        onMouseMove(guiRenderer.currentMousePosition)
    }

    fun push(element: LayoutedElement) {
        val layouted = LayoutedGUIElement(element)
        layouted.init()
        layouted.postInit()
        _push(layouted)
    }

    fun pop(element: GUIElement) {
        if (elementOrder.isEmpty() || !element.canPop) {
            return
        }

        orderLock.acquire()
        val index = elementOrder.indexOf(element)
        orderLock.release()
        if (index < 0) {
            return
        }
        element.onClose()
        orderLock.lock()
        elementOrder.removeAt(index)
        orderLock.unlock()
        if (index == 0) {
            elementOrder.firstOrNull()?.onOpen()
        }

        if (elementOrder.isEmpty()) {
            renderWindow.inputHandler.inputHandler = null
            guiRenderer.popper.clear()
            guiRenderer.dragged.element = null
        }
    }

    fun pop() {
        if (guiRenderer.dragged.element != null) {
            guiRenderer.dragged.element = null
            return
        }
        if (elementOrder.firstOrNull()?.canPop == false) {
            return
        }
        orderLock.lock()
        val previous = elementOrder.removeFirstOrNull()
        orderLock.unlock()
        if (previous == null) {
            return
        }
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
        val remaining: MutableList<GUIElement> = mutableListOf()
        orderLock.acquire()
        for (element in elementOrder) {
            if (!element.canPop) {
                remaining += element
                continue
            }
            element.onClose()
        }
        orderLock.release()
        orderLock.lock()
        elementOrder.clear()
        elementOrder += remaining
        orderLock.unlock()
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
