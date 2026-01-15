/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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
import de.bixilon.minosoft.gui.rendering.input.InputHandler
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputManager
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.actions.KeyActionFilter.Companion.filter
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

class BindingsManager(
    val input: InputManager,
) {
    private val session = input.context.session
    private val profile = session.profiles.controls

    private val bindings: SynchronizedMap<ResourceLocation, KeyBindingState> = synchronizedMapOf()
    private val pressed: MutableSet<ResourceLocation> = mutableSetOf()


    init {
        profile::bindings.observeMap(this) {
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

    private fun onKey(name: ResourceLocation, state: KeyBindingState, pressed: Boolean, code: KeyCodes, time: ValueTimeMark) {
        val filterState = KeyBindingFilterState(pressed)

        val binding = state.binding

        if (binding.action.isEmpty()) return

        for ((action, keys) in binding.action) {
            val filter = action.filter()
            filter.check(filterState, keys, input, name, state, code, pressed, time)
        }
        if (!filterState.satisfied) return

        val previous = name in this
        if (previous == filterState.result) return

        for (callback in state.callback) {
            callback(filterState.result)
        }

        state.lastChange = time

        if (!filterState.store) return

        if (filterState.result) {
            this.pressed += name
        } else {
            this.pressed -= name
        }
    }

    fun onKey(code: KeyCodes, pressed: Boolean, handler: InputHandler?, time: ValueTimeMark) {
        // Find all satisfied bindings that use this key and track their key counts
        // This part of the code is to make sure that combination keys take priority over single keys AND combination keys that include other combination keys.
        // It can have problems in a schenario if a third combination that has more keys but includes another combination exists. But who in their right name bother with that... 
        // This should technically include all possible use cases.
        val satisfiedBindings = mutableMapOf<ResourceLocation, Int>() // name -> total key count
        var maxKeyCount = 0
        
        if (pressed) {
            // Check which bindings are satisfied
            for ((name, state) in bindings) {
                if (handler != null && !state.binding.ignoreConsumer) continue
                
                val binding = state.binding
                val modifierKeys = binding.action[KeyActions.MODIFIER] ?: emptySet()
                // Check if all modifier keys are currently pressed (if any)
                if (modifierKeys.isNotEmpty() && !input.areKeysDown(modifierKeys)) continue
                
                // Check if this binding uses the current key (in any action that's not MODIFIER)
                val usesCurrentKey = binding.action.entries.any { (action, keys) ->
                    action != KeyActions.MODIFIER && code in keys
                }
                
                if (usesCurrentKey) {
                    // Count total keys in this binding (modifiers + action keys)
                    val actionKeys = binding.action.entries
                        .filter { it.key != KeyActions.MODIFIER }
                        .flatMap { it.value }
                        .toSet()
                    val totalKeyCount = modifierKeys.size + actionKeys.size
                    
                    satisfiedBindings[name] = totalKeyCount
                    if (totalKeyCount > maxKeyCount) {
                        maxKeyCount = totalKeyCount
                    }
                }
            }
        }
        
        for ((name, state) in bindings) {
            if (handler != null && !state.binding.ignoreConsumer) {
                continue
            }
            
            // If there are satisfied bindings, only trigger those with the maximum key count
            if (pressed && satisfiedBindings.isNotEmpty()) {
                val keyCount = satisfiedBindings[name]
                
                if (keyCount != null) {
                    // This binding is satisfied, but skip if it's not the most specific
                    if (keyCount < maxKeyCount) {
                        continue
                    }
                } else {
                    // This binding is not in satisfiedBindings, check if it uses the current key
                    val binding = state.binding
                    val usesCurrentKey = binding.action.entries.any { (action, keys) ->
                        action != KeyActions.MODIFIER && code in keys
                    }
                    
                    if (usesCurrentKey) {
                        // Skip this binding because more specific combinations take priority
                        continue
                    }
                }
            }
            
            onKey(name, state, pressed, code, time)
        }
    }

    fun register(name: ResourceLocation, default: KeyBinding, pressed: Boolean = false, callback: KeyBindingCallback) {
        val keyBinding = profile.bindings.getOrPut(name) { default }
        val callbackPair = bindings.synchronizedGetOrPut(name) { KeyBindingState(keyBinding, default, pressed) }
        callbackPair.callback += callback

        if (keyBinding.action.containsKey(KeyActions.STICKY) && pressed) {
            this.pressed += name
        }
    }

    fun registerCheck(vararg checks: Pair<ResourceLocation, KeyBinding>) {
        for ((name, binding) in checks) {
            bindings.synchronizedGetOrPut(name) { KeyBindingState(profile.bindings.getOrPut(name) { binding }, binding) }
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
