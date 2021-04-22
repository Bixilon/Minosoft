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
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.input.camera.Camera
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.hud.elements.input.KeyConsumer
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.logging.Log
import org.lwjgl.glfw.GLFW.*

class RenderWindowInputHandler(
    val renderWindow: RenderWindow,
) {
    val connection: PlayConnection = renderWindow.connection
    val camera: Camera = Camera(connection, Minosoft.getConfig().config.game.camera.fov, renderWindow)

    private val keyBindingCallbacks: MutableMap<ResourceLocation, KeyBindingCallbackPair> = mutableMapOf()
    private val keysDown: MutableList<KeyCodes> = mutableListOf()
    private val keyCombinationsDown: MutableList<ResourceLocation> = mutableListOf()

    private var skipNextCharPress = false

    private var _currentInputConsumer: KeyConsumer? = null


    init {
        registerKeyCallback(KeyBindingsNames.DEBUG_MOUSE_CATCH) {
            if (it) {
                glfwSetInputMode(renderWindow.windowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
            } else {
                glfwSetInputMode(renderWindow.windowId, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
            }
            renderWindow.sendDebugMessage("Toggled mouse catch!")
        }
    }


    var currentKeyConsumer: KeyConsumer?
        get() = _currentInputConsumer
        set(value) {
            _currentInputConsumer = value
        }

    fun invoke(windowId: Long, key: Int, char: Int, action: Int, modifierKey: Int) {
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
                Log.game("Unknown glfw action $action")
                return
            }
        }

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
            var combinationDown = keyDown
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
                var alreadyActive = keyCombinationsDown.contains(resourceLocation)
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
                combinationDown = !alreadyActive
            }

            pair.keyBinding.action[KeyAction.STICKY]?.let {
                checkSticky(it, false)
            }

            pair.keyBinding.action[KeyAction.STICKY_INVERTED]?.let {
                checkSticky(it, true)
            }

            if (!thisIsChange || checksRun == 0) {
                continue
            }

            // Log.debug("Changing $resourceLocation because of $keyCode -> $combinationDown")
            for (callback in pair.callback) {
                callback.invoke(combinationDown)
            }

            if (combinationDown) {
                keyCombinationsDown += resourceLocation
            } else {
                keyCombinationsDown -= resourceLocation
            }
        }
        if (previousKeyConsumer != currentKeyConsumer) {
            skipNextCharPress = true
        }
    }

    fun invoke(windowId: Long, char: Int) {
        if (windowId != renderWindow.windowId) {
            return
        }
        if (skipNextCharPress) {
            skipNextCharPress = false
            return
        }
        currentKeyConsumer?.charInput(char.toChar())
    }

    fun invoke(windowId: Long, xPos: Double, yPos: Double) {
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
                callback.invoke(it)
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
        return keyCombinationsDown.contains(resourceLocation)
    }

    fun unregisterKeyBinding(it: ResourceLocation) {
        keyBindingCallbacks.remove(it)
    }
}
