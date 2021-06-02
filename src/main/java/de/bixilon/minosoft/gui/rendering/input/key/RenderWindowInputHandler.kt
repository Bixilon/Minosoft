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

package de.bixilon.minosoft.gui.rendering.input.key

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.hud.elements.input.KeyConsumer
import de.bixilon.minosoft.gui.rendering.input.LeftClickHandler
import de.bixilon.minosoft.gui.rendering.input.RightClickHandler
import de.bixilon.minosoft.gui.rendering.input.camera.Camera
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.glfw.GLFW.*

class RenderWindowInputHandler(
    val renderWindow: RenderWindow,
) {
    val connection: PlayConnection = renderWindow.connection
    val camera: Camera = Camera(connection, Minosoft.getConfig().config.game.camera.fov, renderWindow)

    private val keyBindingCallbacks: MutableMap<ResourceLocation, KeyBindingCallbackPair> = mutableMapOf()
    private val keysDown: MutableList<KeyCodes> = mutableListOf()
    private val keyBindingsDown: MutableList<ResourceLocation> = mutableListOf()
    private val keysLastDownTime: MutableMap<KeyCodes, Long> = mutableMapOf()

    private var skipNextCharPress = false

    val rightClickHandler = RightClickHandler(renderWindow)
    val leftClickHandler = LeftClickHandler(renderWindow)

    init {
        registerKeyCallback(KeyBindingsNames.DEBUG_MOUSE_CATCH) {
            val newCursorMode = if (it) {
                GLFW_CURSOR_DISABLED
            } else {
                GLFW_CURSOR_NORMAL
            }
            glfwSetInputMode(renderWindow.windowId, GLFW_CURSOR, newCursorMode)
            renderWindow.sendDebugMessage("Toggled mouse catch!")
        }
    }

    fun init() {
        rightClickHandler.init()
        leftClickHandler.init()
    }


    var currentKeyConsumer: KeyConsumer? = null

    fun mouseKeyInput(windowId: Long, button: Int, action: Int, modifierKey: Int) {
        keyInput(windowId, button, 0, action, modifierKey)
    }

    fun keyInput(windowId: Long, key: Int, char: Int, action: Int, modifierKey: Int) {
        if (windowId != renderWindow.windowId) {
            return
        }
        val keyCode = KeyCodes.KEY_CODE_GLFW_ID_MAP[key] ?: KeyCodes.KEY_UNKNOWN

        val keyDown = when (action) {
            GLFW_PRESS -> {
                currentKeyConsumer?.keyInput(keyCode)
                true
            }
            GLFW_RELEASE -> false
            GLFW_REPEAT -> {
                currentKeyConsumer?.keyInput(keyCode)
                return
            }
            else -> {
                Log.log(LogMessageType.RENDERING_GENERAL, LogLevels.WARN) { "Unknown glfw action $action" }
                return
            }
        }
        val currentTime = System.currentTimeMillis()

        if (keyDown) {
            keysDown += keyCode
        } else {
            keysDown -= keyCode
        }

        val previousKeyConsumer = currentKeyConsumer

        for ((resourceLocation, pair) in keyBindingCallbacks) {
            if (currentKeyConsumer != null && !pair.keyBinding.ignoreConsumer) {
                continue
            }
            var thisKeyBindingDown = keyDown
            var checksRun = 0
            var thisIsChange = true

            pair.keyBinding.action[KeyAction.PRESS]?.let {
                if (!keyDown) {
                    thisIsChange = false
                }
                if (!it.contains(keyCode)) {
                    thisIsChange = false
                }
                checksRun++
            }

            pair.keyBinding.action[KeyAction.RELEASE]?.let {
                if (keyDown) {
                    thisIsChange = false
                }
                if (!it.contains(keyCode)) {
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

            fun checkSticky(keys: MutableSet<KeyCodes>, invert: Boolean) {
                var alreadyActive = keyBindingsDown.contains(resourceLocation)
                if (invert) {
                    alreadyActive = !alreadyActive
                }
                checksRun++
                if (!keys.contains(keyCode)) {
                    thisIsChange = false
                    return
                }
                if (!keyDown) {
                    thisIsChange = false
                    return
                }
                thisKeyBindingDown = !alreadyActive
            }

            pair.keyBinding.action[KeyAction.STICKY]?.let {
                checkSticky(it, false)
            }

            pair.keyBinding.action[KeyAction.STICKY_INVERTED]?.let {
                checkSticky(it, true)
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

            // Log.debug("Changing $resourceLocation because of $keyCode -> $thisKeyBindingDown")
            pair.lastChange = System.currentTimeMillis()
            for (callback in pair.callback) {
                callback(thisKeyBindingDown)
            }

            if (thisKeyBindingDown) {
                keyBindingsDown += resourceLocation
            } else {
                keyBindingsDown -= resourceLocation
            }
        }
        if (keyDown) {
            keysLastDownTime[keyCode] = currentTime
        }

        if (previousKeyConsumer != currentKeyConsumer) {
            skipNextCharPress = true
        }
    }

    fun charInput(windowId: Long, char: Int) {
        if (windowId != renderWindow.windowId) {
            return
        }
        if (skipNextCharPress) {
            skipNextCharPress = false
            return
        }
        currentKeyConsumer?.charInput(char.toChar())
    }

    fun mouseMove(windowId: Long, xPos: Double, yPos: Double) {
        if (windowId != renderWindow.windowId) {
            return
        }
        camera.mouseCallback(xPos, yPos)
    }

    fun registerKeyCallback(resourceLocation: ResourceLocation, callback: ((keyDown: Boolean) -> Unit)) {
        val keyBinding = Minosoft.getConfig().config.game.controls.keyBindings.entries[resourceLocation] ?: return
        val callbackPair = keyBindingCallbacks.getOrPut(resourceLocation) { KeyBindingCallbackPair(keyBinding) }
        if (keyBinding.ignoreConsumer) {
            callbackPair.callback += callback
        } else {
            callbackPair.callback += add@{
                if (currentKeyConsumer != null) {
                    return@add
                }
                callback(it)
            }
        }
    }

    fun registerCheckCallback(vararg resourceLocations: ResourceLocation) {
        for (resourceLocation in resourceLocations) {
            val keyBinding = Minosoft.getConfig().config.game.controls.keyBindings.entries[resourceLocation] ?: return
            keyBindingCallbacks.getOrPut(resourceLocation) { KeyBindingCallbackPair(keyBinding) }
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
        camera.draw()
        leftClickHandler.draw(delta)
        rightClickHandler.draw(delta)
    }
}
