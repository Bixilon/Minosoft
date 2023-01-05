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

package de.bixilon.minosoft.gui.rendering.input.key

import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.map.SynchronizedMap
import de.bixilon.kutil.observer.map.MapObserver.Companion.observeMap
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.events.input.MouseMoveEvent
import de.bixilon.minosoft.gui.rendering.events.input.MouseScrollEvent
import de.bixilon.minosoft.gui.rendering.events.input.RawCharInputEvent
import de.bixilon.minosoft.gui.rendering.events.input.RawKeyInputEvent
import de.bixilon.minosoft.gui.rendering.gui.input.ModifierKeys
import de.bixilon.minosoft.gui.rendering.input.CameraInput
import de.bixilon.minosoft.gui.rendering.input.InputHandler
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionManager
import de.bixilon.minosoft.gui.rendering.system.window.CursorModes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2dUtil.EMPTY
import de.bixilon.minosoft.modding.EventPriorities
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class RenderWindowInputHandler(
    val context: RenderContext,
) {
    val connection: PlayConnection = context.connection
    val cameraInput = CameraInput(context, context.camera.matrixHandler)
    private val profile = connection.profiles.controls

    private val keyBindingCallbacks: SynchronizedMap<ResourceLocation, KeyBindingCallbackPair> = synchronizedMapOf()
    private val keysDown: MutableList<KeyCodes> = mutableListOf()
    private val keyBindingsDown: MutableList<ResourceLocation> = mutableListOf()
    private val keysLastDownTime: MutableMap<KeyCodes, Long> = mutableMapOf()


    var currentMousePosition: Vec2d = Vec2d.EMPTY
        private set


    val interactionManager = InteractionManager(context)
    var inputHandler: InputHandler? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value

            deactivateAll()

            context.window.cursorMode = if (value == null) {
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
                KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4),
                KeyActions.PRESS to setOf(KeyCodes.KEY_M),
                ignoreConsumer = true,
            ), defaultPressed = StaticConfiguration.DEBUG_MODE) {
            val nextMode = when (context.window.cursorMode) {
                CursorModes.DISABLED -> CursorModes.NORMAL
                CursorModes.NORMAL -> CursorModes.DISABLED
                CursorModes.HIDDEN -> CursorModes.NORMAL
            }
            context.window.cursorMode = nextMode
            connection.util.sendDebugMessage("Cursor mode: ${nextMode.format()}")
        }
    }

    fun init() {
        interactionManager.init()

        connection.events.listen<RawCharInputEvent> { charInput(it.char) }
        connection.events.listen<RawKeyInputEvent> { keyInput(it.keyCode, it.keyChangeType) }
        connection.events.listen<MouseScrollEvent>(priority = EventPriorities.LOW) { scroll(it.offset, it) }

        connection.events.listen<MouseMoveEvent> {
            val inputHandler = inputHandler
            currentMousePosition = it.position
            if (inputHandler != null) {
                if (skipMouseMove) {
                    skipMouseMove = false
                    return@listen
                }
                inputHandler.onMouseMove(Vec2i(it.position))
                return@listen
            }

            cameraInput.updateMouse(it.delta)
        }

        profile::keyBindings.observeMap(this) {
            for ((key, value) in it.adds) {
                val binding = keyBindingCallbacks[key] ?: continue
                binding.keyBinding = value
            }
            for ((key, value) in it.removes) {
                val binding = keyBindingCallbacks[key] ?: continue
                binding.keyBinding = binding.default
            }
        }
        cameraInput.init()
    }

    private fun deactivateAll() {
        keysDown.clear()
        keysLastDownTime.clear()

        for ((name, pair) in keyBindingCallbacks) {
            val down = name in keyBindingsDown
            if (!down || pair.defaultPressed) {
                continue
            }

            // ToDo
            if (pair.keyBinding.action[KeyActions.DOUBLE_PRESS] != null) {
                continue
            }
            if (pair.keyBinding.action[KeyActions.STICKY] != null) {
                continue
            }


            for (callback in pair.callback) {
                callback(false)
            }
            keyBindingsDown -= name
        }
    }

    private fun keyInput(keyCode: KeyCodes, keyChangeType: KeyChangeTypes) {
        val inputHandler = inputHandler
        inputHandler?.onKey(keyChangeType, keyCode)

        val keyDown = when (keyChangeType) {
            KeyChangeTypes.PRESS -> true
            KeyChangeTypes.RELEASE -> false
            KeyChangeTypes.REPEAT -> return
        }

        val currentTime = millis()

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

            pair.keyBinding.action[KeyActions.PRESS]?.let {
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

            pair.keyBinding.action[KeyActions.RELEASE]?.let {
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

            pair.keyBinding.action[KeyActions.CHANGE]?.let {
                if (!it.contains(keyCode)) {
                    thisIsChange = false
                }
                checksRun++
            }

            pair.keyBinding.action[KeyActions.MODIFIER]?.let {
                if (!keysDown.containsAll(it)) {
                    thisIsChange = false
                }
                checksRun++
            }

            pair.keyBinding.action[KeyActions.STICKY]?.let {
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

            pair.keyBinding.action[KeyActions.DOUBLE_PRESS]?.let {
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

            pair.lastChange = millis()
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
            skipCharPress = true
        }
        if (keyDown) {
            keysLastDownTime[keyCode] = currentTime
        }


        if (inputHandler != this.inputHandler && this.inputHandler != null) {
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

    private fun scroll(scrollOffset: Vec2d, event: MouseScrollEvent? = null) {
        val inputHandler = inputHandler
        if (inputHandler != null) {
            inputHandler.onScroll(scrollOffset)
            event?.cancelled = true
        }
    }

    fun registerKeyCallback(resourceLocation: ResourceLocation, defaultKeyBinding: KeyBinding, defaultPressed: Boolean = false, callback: ((keyDown: Boolean) -> Unit)) {
        val keyBinding = profile.keyBindings.getOrPut(resourceLocation) { defaultKeyBinding }
        val callbackPair = keyBindingCallbacks.synchronizedGetOrPut(resourceLocation) { KeyBindingCallbackPair(keyBinding, defaultKeyBinding, defaultPressed) }
        callbackPair.callback += callback

        if (keyBinding.action.containsKey(KeyActions.STICKY) && defaultPressed) {
            keyBindingsDown += resourceLocation
        }
    }

    fun registerCheckCallback(vararg checks: Pair<ResourceLocation, KeyBinding>) {
        for ((resourceLocation, defaultKeyBinding) in checks) {
            keyBindingCallbacks.synchronizedGetOrPut(resourceLocation) { KeyBindingCallbackPair(profile.keyBindings.getOrPut(resourceLocation) { defaultKeyBinding }, defaultKeyBinding) }
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

    fun isKeyDown(modifier: ModifierKeys): Boolean {
        return context.inputHandler.isKeyDown(*when (modifier) {
            ModifierKeys.CONTROL -> arrayOf(KeyCodes.KEY_LEFT_CONTROL, KeyCodes.KEY_RIGHT_CONTROL)
            ModifierKeys.ALT -> arrayOf(KeyCodes.KEY_LEFT_ALT, KeyCodes.KEY_RIGHT_ALT)
            ModifierKeys.SHIFT -> arrayOf(KeyCodes.KEY_LEFT_SHIFT, KeyCodes.KEY_RIGHT_SHIFT)
            ModifierKeys.SUPER -> arrayOf(KeyCodes.KEY_LEFT_SUPER, KeyCodes.KEY_RIGHT_SUPER)
        })
    }

    fun draw(delta: Double) {
        cameraInput.updateInput(delta)
        interactionManager.draw(delta)
    }
}
