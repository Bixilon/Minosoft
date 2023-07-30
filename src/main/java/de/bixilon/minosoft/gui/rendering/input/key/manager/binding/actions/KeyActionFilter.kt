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

package de.bixilon.minosoft.gui.rendering.input.key.manager.binding.actions

import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputManager
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.KeyBindingFilterState
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.KeyBindingState

interface KeyActionFilter {

    fun check(filter: KeyBindingFilterState, codes: Set<KeyCodes>, input: InputManager, name: ResourceLocation, state: KeyBindingState, code: KeyCodes, pressed: Boolean, millis: Long)


    object Press : KeyActionFilter {

        override fun check(filter: KeyBindingFilterState, codes: Set<KeyCodes>, input: InputManager, name: ResourceLocation, state: KeyBindingState, code: KeyCodes, pressed: Boolean, millis: Long) {
            if (!pressed || code !in codes) {
                filter.satisfied = false
                return
            }

            filter.store = false
        }
    }

    object Release : KeyActionFilter {

        override fun check(filter: KeyBindingFilterState, codes: Set<KeyCodes>, input: InputManager, name: ResourceLocation, state: KeyBindingState, code: KeyCodes, pressed: Boolean, millis: Long) {
            if (pressed || code !in codes) {
                filter.satisfied = false
                return
            }

            filter.result = true
            filter.store = false
        }
    }

    object Change : KeyActionFilter {

        override fun check(filter: KeyBindingFilterState, codes: Set<KeyCodes>, input: InputManager, name: ResourceLocation, state: KeyBindingState, code: KeyCodes, pressed: Boolean, millis: Long) {
            if (code in codes) return

            filter.satisfied = false
        }
    }

    object Modifier : KeyActionFilter {

        override fun check(filter: KeyBindingFilterState, codes: Set<KeyCodes>, input: InputManager, name: ResourceLocation, state: KeyBindingState, code: KeyCodes, pressed: Boolean, millis: Long) {
            if (!pressed) {
                filter.satisfied = false
                return
            }
            if (code in codes) return
            if (input.areKeysDown(codes)) return

            filter.satisfied = false
        }
    }

    object Sticky : KeyActionFilter {

        override fun check(filter: KeyBindingFilterState, codes: Set<KeyCodes>, input: InputManager, name: ResourceLocation, state: KeyBindingState, code: KeyCodes, pressed: Boolean, millis: Long) {
            if (!pressed) {
                // sticky keys are invoked on press and not on release
                filter.satisfied = false
                return
            }
            if (code !in codes) {
                filter.satisfied = false
                return
            }
            val wasPressed = name in input.bindings

            filter.result = !wasPressed
        }
    }


    object DoublePress : KeyActionFilter {

        override fun check(filter: KeyBindingFilterState, codes: Set<KeyCodes>, input: InputManager, name: ResourceLocation, state: KeyBindingState, code: KeyCodes, pressed: Boolean, millis: Long) {
            if (!pressed) {
                filter.satisfied = false
                return
            }
            if (code !in codes) {
                filter.satisfied = false
                return
            }
            val previous = input.getLastPressed(code)
            if (previous < 0L) {
                filter.satisfied = false
                return
            }

            if (millis - previous > RenderConstants.DOUBLE_PRESS_KEY_PRESS_MAX_DELAY) {
                filter.satisfied = false
                return
            }
            if (millis - state.lastChange <= RenderConstants.DOUBLE_PRESS_DELAY_BETWEEN_PRESSED) {
                filter.satisfied = false
                return
            }
            filter.result = !input.bindings.isDown(name)
        }
    }


    companion object {

        fun KeyActions.filter(): KeyActionFilter {
            return when (this) {
                KeyActions.PRESS -> Press
                KeyActions.RELEASE -> Release
                KeyActions.CHANGE -> Change
                KeyActions.MODIFIER -> Modifier
                KeyActions.STICKY -> Sticky
                KeyActions.DOUBLE_PRESS -> DoublePress
            }
        }
    }
}
