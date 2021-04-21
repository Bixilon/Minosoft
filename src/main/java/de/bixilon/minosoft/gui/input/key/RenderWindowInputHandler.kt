/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.input.key

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.input.camera.Camera
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.hud.elements.input.KeyConsumer
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import org.lwjgl.glfw.GLFW

class RenderWindowInputHandler(
    val renderWindow: RenderWindow,
) {
    val connection: PlayConnection = renderWindow.connection

    private val keyBindingCallbacks: MutableMap<ResourceLocation, Pair<KeyBinding, MutableSet<((keyCode: KeyCodes, keyEvent: KeyAction) -> Unit)>>> = mutableMapOf()
    private val keysDown: MutableSet<KeyCodes> = mutableSetOf()
    private val keyBindingDown: MutableSet<KeyBinding> = mutableSetOf()
    val camera: Camera = Camera(connection, Minosoft.getConfig().config.game.camera.fov, renderWindow)

    private var skipNextCharPress = false

    private var _currentInputConsumer: KeyConsumer? = null
    var mouseCatch = !StaticConfiguration.DEBUG_MODE


    init {
        registerKeyCallback(KeyBindingsNames.DEBUG_MOUSE_CATCH) { _: KeyCodes, _: KeyAction ->
            mouseCatch = !mouseCatch
            if (mouseCatch) {
                GLFW.glfwSetInputMode(renderWindow.windowId, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED)
            } else {
                GLFW.glfwSetInputMode(renderWindow.windowId, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL)
            }
            renderWindow.sendDebugMessage("Toggled mouse catch!")
        }
    }

    var currentKeyConsumer: KeyConsumer?
        get() = _currentInputConsumer
        set(value) {
            _currentInputConsumer = value
            for ((_, binding) in keyBindingCallbacks) {
                if (!keyBindingDown.contains(binding.first)) {
                    continue
                }
                if (!binding.first.action.containsKey(KeyAction.TOGGLE) && !binding.first.action.containsKey(KeyAction.CHANGE)) {
                    continue
                }

                for (keyCallback in binding.second) {
                    keyCallback.invoke(KeyCodes.KEY_UNKNOWN, KeyAction.RELEASE)
                }
            }
            // ToDo: move to mouse consumer
            if (value == null) {
                if (mouseCatch) {
                    renderWindow.renderQueue.add { GLFW.glfwSetInputMode(renderWindow.windowId, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED) }
                }
            } else {
                renderWindow.renderQueue.add { GLFW.glfwSetInputMode(renderWindow.windowId, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL) }
            }
            keyBindingDown.clear()
        }

    fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        val keyCode = KeyCodes.KEY_CODE_GLFW_ID_MAP[key] ?: KeyCodes.KEY_UNKNOWN
        val keyAction = when (action) {
            GLFW.GLFW_PRESS -> KeyAction.PRESS
            GLFW.GLFW_RELEASE -> KeyAction.RELEASE
            // ToDo: Double, Hold
            else -> return
        }
        if (keyAction == KeyAction.PRESS) {
            keysDown.add(keyCode)
        } else if (keyAction == KeyAction.RELEASE) {
            keysDown.remove(keyCode)
        }

        if (keyAction == KeyAction.PRESS) {
            // ToDo: Repeatable keys, long holding, etc
            currentKeyConsumer?.keyInput(keyCode)
        }

        val previousKeyConsumer = currentKeyConsumer
        for ((_, keyCallbackPair) in keyBindingCallbacks) {
            run {
                val keyBinding = keyCallbackPair.first
                val keyCallbacks = keyCallbackPair.second

                var anyCheckRun = false

                keyBinding.action[KeyAction.MODIFIER]?.let {
                    val previousKeysDown = if (keyAction == KeyAction.RELEASE) {
                        val previousKeysDown = keysDown.toMutableList()
                        previousKeysDown.add(keyCode)
                        previousKeysDown
                    } else {
                        keysDown
                    }
                    if (!previousKeysDown.containsAll(it)) {
                        return@run
                    }
                    anyCheckRun = true
                }
                keyBinding.action[KeyAction.CHANGE]?.let {
                    if (!it.contains(keyCode)) {
                        return@run
                    }
                    anyCheckRun = true
                }

                // release or press
                if (keyBinding.action[KeyAction.CHANGE] == null) {
                    keyBinding.action[keyAction].let {
                        if (it == null) {
                            return@run
                        }
                        if (!it.contains(keyCode)) {
                            return@run
                        }
                        anyCheckRun = true
                    }
                }

                if (!anyCheckRun) {
                    return@run
                }

                if (keyAction == KeyAction.PRESS) {
                    keyBindingDown.add(keyBinding)
                } else if (keyAction == KeyAction.RELEASE) {
                    keyBindingDown.remove(keyBinding)
                }
                for (keyCallback in keyCallbacks) {
                    keyCallback.invoke(keyCode, keyAction)
                    if (previousKeyConsumer != currentKeyConsumer) {
                        skipNextCharPress = true
                    }
                }
            }
        }
    }

    fun invoke(window: Long, char: Int) {
        if (skipNextCharPress) {
            skipNextCharPress = false
            return
        }
        currentKeyConsumer?.charInput(char.toChar())
    }

    fun invoke(window: Long, xPos: Double, yPos: Double) {
        camera.mouseCallback(xPos, yPos)
    }

    fun registerKeyCallback(resourceLocation: ResourceLocation, ignoreConsumer: Boolean = false, callback: ((keyCode: KeyCodes, keyEvent: KeyAction) -> Unit)) {
        var resourceLocationCallbacks = keyBindingCallbacks[resourceLocation]?.second
        if (resourceLocationCallbacks == null) {
            resourceLocationCallbacks = mutableSetOf()
            val keyBinding = Minosoft.getConfig().config.game.controls.keyBindings.entries[resourceLocation] ?: return
            keyBindingCallbacks[resourceLocation] = Pair(keyBinding, resourceLocationCallbacks)
        }
        resourceLocationCallbacks.add { keyCode, keyEvent ->
            if (!ignoreConsumer) {
                if (currentKeyConsumer != null) {
                    return@add
                }
            }
            callback.invoke(keyCode, keyEvent)
        }
    }

    fun unregisterKeyBinding(it: ResourceLocation) {
        keyBindingCallbacks.remove(it)
    }
}
