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

package de.bixilon.minosoft.gui.rendering.input.key.manager

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.events.input.CharInputEvent
import de.bixilon.minosoft.gui.rendering.events.input.KeyInputEvent
import de.bixilon.minosoft.gui.rendering.events.input.MouseMoveEvent
import de.bixilon.minosoft.gui.rendering.events.input.MouseScrollEvent
import de.bixilon.minosoft.gui.rendering.gui.input.ModifierKeys
import de.bixilon.minosoft.gui.rendering.input.CameraInput
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionManagerKeys
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.BindingsManager
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2dUtil.EMPTY
import de.bixilon.minosoft.modding.EventPriorities
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.set
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import java.util.*

class InputManager(
    val context: RenderContext,
) {
    val connection: PlayConnection = context.connection
    val cameraInput = CameraInput(context, context.camera.matrixHandler)
    val bindings = BindingsManager(this)
    val handler = InputHandlerManager(this)
    val interaction = InteractionManagerKeys(this, connection.camera.interactions)


    private val pressed: EnumSet<KeyCodes> = KeyCodes.set()
    private val times: Object2LongMap<KeyCodes> = Object2LongOpenHashMap<KeyCodes>().apply { defaultReturnValue(-1L) }

    var mousePosition: Vec2d = Vec2d.EMPTY
        private set


    fun init() {
        interaction.register()

        connection.events.listen<CharInputEvent> { onChar(it.char) }
        connection.events.listen<KeyInputEvent> { onKey(it.code, it.change) }
        connection.events.listen<MouseScrollEvent>(priority = EventPriorities.LOW) { scroll(it.offset, it) }
        connection.events.listen<MouseMoveEvent> { onMouse(it.delta, it.position) }

        cameraInput.init()
    }

    fun clear() {
        pressed.clear()
        times.clear()
        bindings.clear()
    }

    private fun onMouse(delta: Vec2d, position: Vec2d) {
        this.mousePosition = position
        if (handler.onMouse(Vec2(position))) return
        cameraInput.updateMouse(delta)
    }

    private fun onKey(code: KeyCodes, change: KeyChangeTypes) {
        this.handler.onKey(code, change)

        val pressed = when (change) {
            KeyChangeTypes.PRESS -> true
            KeyChangeTypes.RELEASE -> false
            KeyChangeTypes.REPEAT -> return
        }

        val millis = millis()


        if (pressed) {
            this.pressed += code
        } else {
            this.pressed -= code
        }

        val handler = this.handler.handler
        bindings.onKey(code, pressed, millis)

        if (pressed) {
            times[code] = millis
        }

        this.handler.checkSkip(code, pressed, handler)
    }

    private fun onChar(char: Int) {
        handler.onChar(char)
    }

    private fun scroll(scrollOffset: Vec2d, event: MouseScrollEvent) {
        if (handler.onScroll(Vec2(scrollOffset))) return
        event.cancelled = true
    }

    fun areKeysDown(vararg keys: KeyCodes): Boolean {
        for (key in keys) {
            if (key !in pressed) {
                return false
            }
        }
        return true
    }

    fun areKeysDown(keys: Collection<KeyCodes>): Boolean {
        for (key in keys) {
            if (key !in pressed) {
                return false
            }
        }
        return true
    }

    fun isKeyDown(vararg keys: KeyCodes): Boolean {
        for (key in keys) {
            if (key in pressed) {
                return true
            }
        }
        return false
    }

    fun isKeyDown(modifier: ModifierKeys): Boolean {
        return isKeyDown(*modifier.codes)
    }

    fun draw(delta: Double) {
        cameraInput.updateInput(delta)
        interaction.draw()
    }

    fun getLastPressed(key: KeyCodes): Long {
        return this.times.getLong(key)
    }
}
