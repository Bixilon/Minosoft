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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputManager
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.BindingsManager
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.KeyBindingFilterState
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.KeyBindingState
import de.bixilon.minosoft.test.IT.OBJENESIS
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import java.util.*


val keysPressed = InputManager::class.java.getDeclaredField("pressed").apply { isAccessible = true }
val bindingsPressed = BindingsManager::class.java.getDeclaredField("pressed").apply { isAccessible = true }
val name = minosoft("dummy")

fun input(): InputManager {
    val manager = OBJENESIS.newInstance(InputManager::class.java)
    val bindings = OBJENESIS.newInstance(BindingsManager::class.java)
    bindingsPressed[bindings] = mutableSetOf<ResourceLocation>()
    manager::bindings.forceSet(bindings)

    keysPressed[manager] = EnumSet.noneOf(KeyCodes::class.java)

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
            0L,
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
            0L,
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
            0L,
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
            0L,
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
            0L,
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
            0L,
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
            0L,
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
            0L,
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
            0L,
        )

        assertTrue(state.satisfied)
        assertTrue(state.forceNotify)
    }

    fun `simple release`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Change.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.CHANGE to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = false,
            0L,
        )

        assertTrue(state.satisfied)
        assertTrue(state.forceNotify)
    }

    fun `wrong key`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Change.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.CHANGE to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = false,
            0L,
        )

        assertFalse(state.satisfied)
        assertFalse(state.forceNotify)
    }

    fun `multiple keys`() {
        val state = KeyBindingFilterState(true)
        KeyActionFilter.Change.check(
            state, setOf(KeyCodes.KEY_0, KeyCodes.KEY_1, KeyCodes.KEY_2), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.CHANGE to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = true,
            0L,
        )

        assertTrue(state.satisfied)
        assertTrue(state.forceNotify)
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
            0L,
        )

        assertFalse(state.skip)
        assertTrue(state.satisfied)
    }

    fun `simple release`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Modifier.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.MODIFIER to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = false,
            0L,
        )

        assertFalse(state.skip)
        assertFalse(state.satisfied)
    }

    fun `wrong key`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Modifier.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.MODIFIER to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = false,
            0L,
        )

        assertTrue(state.skip)
    }

    fun `multiple keys, not all pressed`() {
        val state = KeyBindingFilterState(true)
        KeyActionFilter.Modifier.check(
            state, setOf(KeyCodes.KEY_0, KeyCodes.KEY_1, KeyCodes.KEY_2), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.MODIFIER to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_1,
            pressed = true,
            0L,
        )

        assertFalse(state.satisfied)
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
            0L,
        )

        assertTrue(state.satisfied)
        assertFalse(state.skip)
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
            0L,
        )

        assertTrue(state.result)
        assertFalse(state.skip)
        assertTrue(state.satisfied)
    }

    fun `release key`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Sticky.check(
            state, setOf(KeyCodes.KEY_0), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.STICKY to setOf(KeyCodes.KEY_0)))),
            KeyCodes.KEY_0,
            pressed = false,
            0L,
        )

        assertTrue(state.skip)
    }

    fun `wrong key`() {
        val state = KeyBindingFilterState(false)
        KeyActionFilter.Sticky.check(
            state, setOf(KeyCodes.KEY_1), input(), name,
            KeyBindingState(KeyBinding(mapOf(KeyActions.STICKY to setOf(KeyCodes.KEY_1)))),
            KeyCodes.KEY_0,
            pressed = true,
            0L,
        )

        assertTrue(state.skip)
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
            0L,
        )

        assertFalse(state.result)
        assertFalse(state.skip)
    }
}
