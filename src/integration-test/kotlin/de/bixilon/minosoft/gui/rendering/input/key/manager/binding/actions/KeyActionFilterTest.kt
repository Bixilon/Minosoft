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

package de.bixilon.minosoft.gui.rendering.input.key.manager.binding.actions

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputManager
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.BindingsManager
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.KeyBindingFilterState
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.KeyBindingState
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource.Monotonic.ValueTimeMark


val TIMES = InputManager::class.java.getFieldOrNull("times")!!
val keysPressed = InputManager::class.java.getFieldOrNull("pressed")!!
val bindingsPressed = BindingsManager::class.java.getFieldOrNull("pressed")!!
val name = minosoft("dummy")

val InputManager.times: EnumMap<KeyCodes, ValueTimeMark> get() = TIMES.get(this).unsafeCast()


fun input(): InputManager {
    val manager = InputManager::class.java.allocate()
    val bindings = BindingsManager::class.java.allocate()
    bindingsPressed[bindings] = mutableSetOf<ResourceLocation>()
    manager::bindings.forceSet(bindings)
    TIMES[manager] = EnumMap<KeyCodes, ValueTimeMark>(KeyCodes::class.java)

    keysPressed[manager] = KeyCodes.set()

    return manager
}

@Test(groups = ["input"])
class Press {

    fun `simple press`() {
        val state = KeyBindingFilterState(true)
        KeyActionFilter.Press.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = true,
            now(),
        )

        assertTrue(state.satisfied)
    }

    fun `wrong key`() {
        val state = KeyBindingFilterState(true)
        KeyActionFilter.Press.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = true,
            now(),
        )

        assertFalse(state.satisfied)
    }

    fun `multiple keys`() {
        val state = KeyBindingFilterState(true)
        KeyActionFilter.Press.check(
            state, setOf(KeyCodes.KEY_0, KeyCodes.KEY_1, KeyCodes.KEY_2), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = true,
            now(),
        )

        assertTrue(state.result)
    }

    fun `not pressed`() {
        val state = KeyBindingFilterState(true)
        KeyActionFilter.Press.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = false,
            now(),
        )

        assertFalse(state.satisfied)
    }
}

@Test(groups = ["input"])
class Release {

    fun `simple release`() {
        val state = KeyBindingFilterState(true)
        KeyActionFilter.Release.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.RELEASE to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = false,
            now(),
        )

        assertTrue(state.result)
        assertTrue(state.satisfied)
    }

    fun `wrong key`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Release.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.RELEASE to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = false,
            now(),
        )

        assertFalse(state.satisfied)
    }

    fun `multiple keys`() {
        val state = KeyBindingFilterState(true)
        KeyActionFilter.Release.check(
            state, setOf(KeyCodes.KEY_0, KeyCodes.KEY_1, KeyCodes.KEY_2), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.RELEASE to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = false,
            now(),
        )

        assertTrue(state.satisfied)
    }

    fun pressed() {
        val state = KeyBindingFilterState(true)
        KeyActionFilter.Release.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.RELEASE to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = true,
            now(),
        )

        assertFalse(state.satisfied)
    }
}

@Test(groups = ["input"])
class Change {

    fun `simple press`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Change.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.CHANGE to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = true,
            now(),
        )

        assertTrue(state.satisfied)
    }

    fun `simple release`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Change.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.CHANGE to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = false,
            now(),
        )

        assertTrue(state.satisfied)
    }

    fun `wrong key`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Change.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.CHANGE to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = false,
            now(),
        )

        assertFalse(state.satisfied)
    }

    fun `multiple keys`() {
        val state = KeyBindingFilterState(true)
        KeyActionFilter.Change.check(
            state, setOf(KeyCodes.KEY_0, KeyCodes.KEY_1, KeyCodes.KEY_2), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.CHANGE to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = true,
            now(),
        )

        assertTrue(state.satisfied)
    }
}

@Test(groups = ["input"])
class Modifier {

    fun `simple press`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Modifier.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.MODIFIER to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = true,
            now(),
        )

        assertTrue(state.satisfied)
    }

    fun `simple release`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Modifier.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.MODIFIER to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = false,
            now(),
        )

        assertFalse(state.satisfied)
    }

    fun `wrong key`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Modifier.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.MODIFIER to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = false,
            now(),
        )

        assertFalse(state.satisfied)
    }

    fun `multiple keys, one pressed`() {
        val state = KeyBindingFilterState(true)
        KeyActionFilter.Modifier.check(
            state, setOf(KeyCodes.KEY_0, KeyCodes.KEY_1, KeyCodes.KEY_2), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.MODIFIER to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = true,
            now(),
        )

        assertTrue(state.satisfied)
    }

    fun `multiple keys, all pressed`() {
        val state = KeyBindingFilterState(true)
        val input = input()
        val pressed = keysPressed.get(input).unsafeCast<MutableSet<KeyCodes>>()
        pressed += KeyCodes.KEY_0
        pressed += KeyCodes.KEY_1
        pressed += KeyCodes.KEY_2

        KeyActionFilter.Modifier.check(
            state, setOf(KeyCodes.KEY_0, KeyCodes.KEY_1, KeyCodes.KEY_2), input, name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.MODIFIER to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = true,
            now(),
        )

        assertTrue(state.satisfied)
    }
}

@Test(groups = ["input"])
class Sticky {

    fun `press key`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Sticky.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.STICKY to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = true,
            now(),
        )

        assertTrue(state.result)
        assertTrue(state.satisfied)
    }

    fun `release key`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Sticky.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.STICKY to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = false,
            now(),
        )

        assertFalse(state.satisfied)
    }

    fun `wrong key`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Sticky.check(
            state, setOf(KeyCodes.KEY_1), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.STICKY to setOf(KeyCodes.KEY_1)))),
            KeyCodes.KEY_0,
            pressed = true,
            now(),
        )

        assertFalse(state.satisfied)
    }

    fun `unpress`() {
        val state = KeyBindingFilterState(true)
        val input = input()
        val binding = KeyBinding(mapOf(KeyActions.STICKY to setOf(KeyCodes.KEY_0)))

        val pressed = bindingsPressed[input.bindings].unsafeCast<MutableSet<ResourceLocation>>()
        pressed += name

        KeyActionFilter.Sticky.check(
            state, setOf(KeyCodes.KEY_0), input, name,
            KeyBindingState(binding),
            KeyCodes.KEY_0,
            pressed = true,
            now(),
        )

        assertFalse(state.result)
        assertTrue(state.satisfied)
    }
}


@Test(groups = ["input"])
class DoublePress {

    fun `press single`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.DoublePress.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.DOUBLE_PRESS to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = true,
            now(),
        )

        assertFalse(state.satisfied)
    }

    fun `double press`() {
        val state = KeyBindingFilterState(false)
        val input = input()
        val start = now()
        input.times[KeyCodes.KEY_0] = start

        KeyActionFilter.DoublePress.check(
            state, setOf(KeyCodes.KEY_0), input, name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.DOUBLE_PRESS to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = true,
            start + 10.milliseconds,
        )

        assertTrue(state.satisfied)
        assertTrue(state.result)
        assertTrue(state.store)
    }

    fun `double press second time`() {
        val state = KeyBindingFilterState(false)
        val input = input()
        val start = now()

        val pressed = bindingsPressed[input.bindings].unsafeCast<MutableSet<ResourceLocation>>()
        pressed += name

        input.times[KeyCodes.KEY_0] = start

        KeyActionFilter.DoublePress.check(
            state, setOf(KeyCodes.KEY_0), input, name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.DOUBLE_PRESS to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = true,
            start + 10.milliseconds,
        )

        assertTrue(state.satisfied)
        assertFalse(state.result)
        assertTrue(state.store)
    }

    fun `too long ago press`() {
        val state = KeyBindingFilterState(false)
        val input = input()
        val start = now()

        input.times.put(KeyCodes.KEY_0, start)

        KeyActionFilter.DoublePress.check(
            state, setOf(KeyCodes.KEY_0), input, name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.DOUBLE_PRESS to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = true,
            start + 1.seconds,
        )

        assertFalse(state.satisfied)
    }

    fun `delay between`() {
        val filter = KeyBindingFilterState(false)
        val input = input()
        val start = now()

        input.times[KeyCodes.KEY_0] = start
        val state = KeyBindingState(KeyBinding(mapOf(KeyActions.DOUBLE_PRESS to setOf(KeyCodes.KEY_0))))
        state.lastChange = start + 10.milliseconds


        KeyActionFilter.DoublePress.check(
            filter, setOf(KeyCodes.KEY_0), input, name,
            state,
            KeyCodes.KEY_0,
            pressed = true,
            start + 100.milliseconds,
        )

        assertFalse(filter.satisfied)
    }
}
