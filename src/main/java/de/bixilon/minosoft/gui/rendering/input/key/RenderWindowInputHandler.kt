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

package de.bixilon.minosoft.gui.rendering.input.key

import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.config.profile.delegate.watcher.entry.MapProfileDelegateWatcher.Companion.profileWatchMap
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.input.CameraInput
import de.bixilon.minosoft.gui.rendering.input.InputHandler
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionManager
import de.bixilon.minosoft.gui.rendering.modding.events.input.MouseMoveEvent
import de.bixilon.minosoft.gui.rendering.modding.events.input.MouseScrollEvent
import de.bixilon.minosoft.gui.rendering.modding.events.input.RawCharInputEvent
import de.bixilon.minosoft.gui.rendering.modding.events.input.RawKeyInputEvent
import de.bixilon.minosoft.gui.rendering.system.window.CursorModes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2dUtil.EMPTY
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i

class RenderWindowInputHandler(
    val renderWindow: RenderWindow,
) {
    val connection: PlayConnection = renderWindow.connection
    val cameraInput = CameraInput(renderWindow, renderWindow.camera.matrixHandler)
    private val profile = connection.profiles.controls

    private val keyBindingCallbacks: MutableMap<ResourceLocation, KeyBindingCallbackPair> = mutableMapOf()
    private val keysDown: MutableList<KeyCodes> = mutableListOf()
    private val keyBindingsDown: MutableList<ResourceLocation> = mutableListOf()
    private val keysLastDownTime: MutableMap<KeyCodes, Long> = mutableMapOf()


    var currentMousePosition: Vec2d = Vec2d.EMPTY
        private set


    val interactionManager = InteractionManager(renderWindow)
    var inputHandler: InputHandler? = null
        set(value) {
            field = value

            deactivateAll()

            renderWindow.window.cursorMode = if (value == null) {
                CursorModes.DISABLED
            } else {
                CursorModes.NORMAL
            }
        }
    private var skipCharPress = false
    private var skipMouseMove = false

    init {
        registerKeyCallback("minosoft:debug_change_cursor_mode".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyAction.MODIFIER to setOf(KeyCodes.KEY_F4),
                    KeyAction.PRESS to setOf(KeyCodes.KEY_M),
                ),
                ignoreConsumer = true,
            ), defaultPressed = StaticConfiguration.DEBUG_MODE) {
            val nextMode = when (renderWindow.window.cursorMode) {
                CursorModes.DISABLED -> CursorModes.NORMAL
                CursorModes.NORMAL -> CursorModes.DISABLED
                CursorModes.HIDDEN -> CursorModes.NORMAL
            }
            renderWindow.window.cursorMode = nextMode
            connection.util.sendDebugMessage("Cursor mode: ${nextMode.format()}")
        }
    }

    fun init() {
        interactionManager.init()

        connection.registerEvent(CallbackEventInvoker.of<RawCharInputEvent> { charInput(it.char) })
        connection.registerEvent(CallbackEventInvoker.of<RawKeyInputEvent> { keyInput(it.keyCode, it.keyChangeType) })
        connection.registerEvent(CallbackEventInvoker.of<MouseScrollEvent> { scroll(it.offset) })

        connection.registerEvent(CallbackEventInvoker.of<MouseMoveEvent> {
            val inputHandler = inputHandler
            currentMousePosition = it.position
            if (inputHandler != null) {
                if (skipMouseMove) {
                    skipMouseMove = false
                    return@of
                }
                inputHandler.onMouseMove(Vec2i(it.position))
                return@of
            }

            cameraInput.mouseCallback(it.delta)
        })

        profile::keyBindings.profileWatchMap(this, profile = profile) {
            val keyBinding = keyBindingCallbacks[it.key] ?: return@profileWatchMap
            if (it.wasRemoved() && it.wasAdded()) {
                keyBinding.keyBinding = it.valueAdded
            } else if (it.wasRemoved()) {
                keyBinding.keyBinding = keyBinding.default
            } else {
                keyBinding.keyBinding = it.valueAdded
            }
        }
        cameraInput.init()
    }

    private fun deactivateAll() {
        keysDown.clear()
        keysLastDownTime.clear()
        val toRemove: MutableSet<ResourceLocation> = mutableSetOf()
        for ((name, pair) in keyBindingCallbacks) {
            val down = keyBindingsDown.contains(name)
            if (!down || pair.defaultPressed) {
                continue
            }

            // ToDo
            if (pair.keyBinding.action[KeyAction.DOUBLE_PRESS] != null) {
                continue
            }
            if (pair.keyBinding.action[KeyAction.STICKY] != null) {
                continue
            }


            for (callback in pair.callback) {
                callback(false)
            }
            toRemove += name
        }
        keyBindingsDown -= toRemove
    }

    private fun keyInput(keyCode: KeyCodes, keyChangeType: KeyChangeTypes) {
        val inputHandler = inputHandler
        inputHandler?.onKeyPress(keyChangeType, keyCode)

        val keyDown = when (keyChangeType) {
            KeyChangeTypes.PRESS -> true
            KeyChangeTypes.RELEASE -> false
            KeyChangeTypes.REPEAT -> return
        }

        val currentTime = TimeUtil.time

        if (keyDown) {
            keysDown += keyCode
        } else {
            keysDown -= keyCode
        }

        for ((resourceLocation, pair) in keyBindingCallbacks) {
            if (inputHandler != null && !pair.keyBinding.ignoreConsumer) {
                continue
            }
            var thisKeyBindingDown = keyDown
            var checksRun = 0
            var thisIsChange = true
            var saveDown = true

            pair.keyBinding.action[KeyAction.PRESS]?.let {
                if (!keyDown) {
                    thisIsChange = false
                }
                if (it.contains(keyCode)) {
                    saveDown = false
                } else {
                    thisIsChange = false
                }
                checksRun++
            }

            pair.keyBinding.action[KeyAction.RELEASE]?.let {
                if (keyDown) {
                    thisIsChange = false
                }
                if (it.contains(keyCode)) {
                    saveDown = false
                } else {
                    thisIsChange = false
                }
                checksRun++
            }

            pair.keyBinding.action[KeyAction.CHANGE]?.let {
                if (!it.contains(keyCode)) {
                    thisIsChange = false
                }
                checksRun++
            }

            pair.keyBinding.action[KeyAction.MODIFIER]?.let {
                if (!keysDown.containsAll(it)) {
                    thisIsChange = false
                }
                checksRun++
            }

            pair.keyBinding.action[KeyAction.STICKY]?.let {
                checksRun++
                if (!it.contains(keyCode)) {
                    thisIsChange = false
                    return@let
                }
                if (!keyDown) {
                    thisIsChange = false
                    return@let
                }
                thisKeyBindingDown = !keyBindingsDown.contains(resourceLocation)
            }

            pair.keyBinding.action[KeyAction.DOUBLE_PRESS]?.let {
                checksRun++
                if (!keyDown) {
                    thisIsChange = false
                    return@let
                }
                if (!it.contains(keyCode)) {
                    thisIsChange = false
                    return@let
                }
                val lastDownTime = keysLastDownTime[keyCode]
                if (lastDownTime == null) {
                    thisIsChange = false
                    return@let
                }
                if (currentTime - lastDownTime > RenderConstants.DOUBLE_PRESS_KEY_PRESS_MAX_DELAY) {
                    thisIsChange = false
                    return@let
                }
                if (currentTime - pair.lastChange <= RenderConstants.DOUBLE_PRESS_DELAY_BETWEEN_PRESSED) {
                    thisIsChange = false
                    return@let
                }
                thisKeyBindingDown = !isKeyBindingDown(resourceLocation)
            }

            if (!thisIsChange || checksRun == 0) {
                continue
            }

            pair.lastChange = TimeUtil.time
            for (callback in pair.callback) {
                callback(thisKeyBindingDown)
            }

            if (saveDown) {
                if (thisKeyBindingDown) {
                    keyBindingsDown += resourceLocation
                } else {
                    keyBindingsDown -= resourceLocation
                }
            }
        }
        if (keyDown) {
            keysLastDownTime[keyCode] = currentTime
        }


        if (inputHandler != this.inputHandler) {
            skipCharPress = true
            skipMouseMove = true
        }
    }

    private fun charInput(char: Int) {
        val inputHandler = inputHandler ?: return
        if (skipCharPress) {
            skipCharPress = false
            return
        }
        inputHandler.onCharPress(char)
        return
    }

    private fun scroll(scrollOffset: Vec2d) {
        inputHandler?.onScroll(scrollOffset)
    }

    fun registerKeyCallback(resourceLocation: ResourceLocation, defaultKeyBinding: KeyBinding, defaultPressed: Boolean = false, callback: ((keyDown: Boolean) -> Unit)) {
        val keyBinding = profile.keyBindings.getOrPut(resourceLocation) { defaultKeyBinding }
        val callbackPair = keyBindingCallbacks.getOrPut(resourceLocation) { KeyBindingCallbackPair(keyBinding, defaultKeyBinding, defaultPressed) }
        callbackPair.callback += callback

        if (keyBinding.action.containsKey(KeyAction.STICKY) && defaultPressed) {
            keyBindingsDown += resourceLocation
        }
    }

    fun registerCheckCallback(vararg checks: Pair<ResourceLocation, KeyBinding>) {
        for ((resourceLocation, defaultKeyBinding) in checks) {
            keyBindingCallbacks.getOrPut(resourceLocation) { KeyBindingCallbackPair(profile.keyBindings.getOrPut(resourceLocation) { defaultKeyBinding }, defaultKeyBinding) }
        }
    }

    fun isKeyBindingDown(resourceLocation: ResourceLocation): Boolean {
        return keyBindingsDown.contains(resourceLocation)
    }

    fun unregisterKeyBinding(it: ResourceLocation) {
        keyBindingCallbacks.remove(it)
    }

    fun isKeyDown(vararg keys: KeyCodes): Boolean {
        for (key in keys) {
            if (keysDown.contains(key)) {
                return true
            }
        }
        return false
    }

    fun draw(delta: Double) {
        cameraInput.update()
        interactionManager.draw(delta)
    }
}
