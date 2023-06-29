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

import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputTestUtil.create
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputTestUtil.dummy
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputTestUtil.simulate
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["input"])
class BindingManagerTest {

    fun `just register key binding`() {
        val input = create()
        input.bindings.register(dummy, KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_1)))) {}
    }

    fun `simple pressing`() {
        val input = create()
        var invoked = false
        input.bindings.register(dummy, KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_1)))) { invoked = true }

        input.simulate(KeyCodes.KEY_1, KeyChangeTypes.PRESS)
        assertTrue(invoked)
    }

    fun `register press but do release`() {
        val input = create()
        var invoked = false
        input.simulate(KeyCodes.KEY_1, KeyChangeTypes.PRESS)

        input.bindings.register(dummy, KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_1)))) { invoked = true }

        input.simulate(KeyCodes.KEY_1, KeyChangeTypes.RELEASE)
        assertFalse(invoked)
    }

    fun `register multiple key bindings`() {
        val input = create()
        var correct = 0
        var wrong = 0

        input.bindings.register(dummy.prefix("0"), KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_1)))) { correct++ }
        input.bindings.register(dummy.prefix("1"), KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_2)))) { wrong++ }
        input.bindings.register(dummy.prefix("2"), KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_3)))) { wrong++ }
        input.bindings.register(dummy.prefix("3"), KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_4)))) { wrong++ }

        input.simulate(KeyCodes.KEY_1, KeyChangeTypes.PRESS)

        assertEquals(correct, 1)
        assertEquals(wrong, 0)
        input.simulate(KeyCodes.KEY_2, KeyChangeTypes.PRESS)
        assertEquals(correct, 1)
        assertEquals(wrong, 1)
    }

    fun `register multiple callbacks`() {
        val input = create()
        var a = 0
        var b = 0

        input.bindings.register(dummy, KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_1)))) { a++ }
        input.bindings.register(dummy, KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_2)))) { b++ }

        input.simulate(KeyCodes.KEY_1, KeyChangeTypes.PRESS)

        assertEquals(a, 1)
        assertEquals(b, 1)
    }

    fun `check binding`() {
        val input = create()

        input.bindings.registerCheck(dummy to KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_1))))
        assertFalse(input.bindings.isDown(dummy))
        input.simulate(KeyCodes.KEY_1, KeyChangeTypes.PRESS)
        assertTrue(input.bindings.isDown(dummy))
    }

    // TODO: input handler test
}
