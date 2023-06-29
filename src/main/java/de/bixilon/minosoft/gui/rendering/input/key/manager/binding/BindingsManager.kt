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

package de.bixilon.minosoft.gui.rendering.input.key.manager.binding

import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.map.SynchronizedMap
import de.bixilon.kutil.observer.map.MapObserver.Companion.observeMap
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputManager
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.actions.KeyActionFilter.Companion.filter

class BindingsManager(
    val input: InputManager,
) {
    private val connection = input.context.connection
    private val profile = connection.profiles.controls

    private val bindings: SynchronizedMap<ResourceLocation, KeyBindingState> = synchronizedMapOf()
    private val pressed: MutableSet<ResourceLocation> = mutableSetOf()


    init {
        profile::keyBindings.observeMap(this) {
            for ((key, value) in it.adds) {
                val binding = bindings[key] ?: continue
                binding.binding = value
            }
            for ((key, value) in it.removes) {
                val binding = bindings[key] ?: continue
                binding.binding = binding.default
            }
        }
    }

    fun clear() {
        for ((name, pair) in bindings) {
            val down = name in pressed
            if (!down || pair.pressed) {
                continue
            }

            // ToDo
            if (pair.binding.action[KeyActions.DOUBLE_PRESS] != null) {
                continue
            }
            if (pair.binding.action[KeyActions.STICKY] != null) {
                continue
            }


            for (callback in pair.callback) {
                callback(false)
            }
            pressed -= name
        }
    }

    private fun onKey(name: ResourceLocation, state: KeyBindingState, pressed: Boolean, code: KeyCodes, millis: Long) {
        val filterState = KeyBindingFilterState(pressed)

        val binding = state.binding

        if (binding.action.isEmpty()) return

        for ((action, keys) in binding.action) {
            val filter = action.filter()
            filter.check(filterState, keys, input, name, state, code, pressed, millis)
        }
        if (!filterState.satisfied) return

        val previous = name in this
        if (previous == filterState.result) return

        for (callback in state.callback) {
            callback(filterState.result)
        }

        state.lastChange = millis

        if (!filterState.store) return

        if (filterState.result) {
            this.pressed += name
        } else {
            this.pressed -= name
        }
    }

    fun onKey(code: KeyCodes, pressed: Boolean, millis: Long) {
        val handler = input.handler.handler

        for ((name, state) in bindings) {
            if (handler != null && !state.binding.ignoreConsumer) {
                continue
            }
            onKey(name, state, pressed, code, millis)
        }
    }

    fun register(name: ResourceLocation, default: KeyBinding, pressed: Boolean = false, callback: KeyBindingCallback) {
        val keyBinding = profile.keyBindings.getOrPut(name) { default }
        val callbackPair = bindings.synchronizedGetOrPut(name) { KeyBindingState(keyBinding, default, pressed) }
        callbackPair.callback += callback

        if (keyBinding.action.containsKey(KeyActions.STICKY) && pressed) {
            this.pressed += name
        }
    }

    fun registerCheck(vararg checks: Pair<ResourceLocation, KeyBinding>) {
        for ((name, binding) in checks) {
            bindings.synchronizedGetOrPut(name) { KeyBindingState(profile.keyBindings.getOrPut(name) { binding }, binding) }
        }
    }

    fun isDown(name: ResourceLocation): Boolean {
        return name in pressed
    }

    operator fun contains(name: ResourceLocation) = isDown(name)

    fun unregister(name: ResourceLocation) {
        bindings.remove(name)
    }

    operator fun minusAssign(name: ResourceLocation) = unregister(name)
}
