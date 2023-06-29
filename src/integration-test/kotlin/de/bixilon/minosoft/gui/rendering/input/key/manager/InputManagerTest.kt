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

package de.bixilon.minosoft.gui.rendering.input.key.manager

import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.input.InputHandler
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputTestUtil.create
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputTestUtil.simulate
import de.bixilon.minosoft.gui.rendering.system.window.CursorModes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import org.testng.Assert
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["input"])
class InputManagerTest {

    fun `press key`() {
        val input = create()
        assertFalse(input.isKeyDown(KeyCodes.KEY_1))
        input.simulate(KeyCodes.KEY_1, KeyChangeTypes.PRESS)
        assertTrue(input.isKeyDown(KeyCodes.KEY_1))
    }

    fun `release key again`() {
        val input = create()
        input.simulate(KeyCodes.KEY_1, KeyChangeTypes.PRESS)
        input.simulate(KeyCodes.KEY_1, KeyChangeTypes.RELEASE)
        assertFalse(input.isKeyDown(KeyCodes.KEY_1))
    }

    fun `press multiple keys`() {
        val input = create()
        input.simulate(KeyCodes.KEY_1, KeyChangeTypes.PRESS)
        input.simulate(KeyCodes.KEY_2, KeyChangeTypes.PRESS)
        assertTrue(input.isKeyDown(KeyCodes.KEY_1))
        assertTrue(input.isKeyDown(KeyCodes.KEY_2))
    }

    fun `areKeysDown but none pressed`() {
        val input = create()
        assertFalse(input.areKeysDown(KeyCodes.KEY_1, KeyCodes.KEY_2))
    }

    fun `areKeysDown but only partly pressed`() {
        val input = create()
        input.simulate(KeyCodes.KEY_1, KeyChangeTypes.PRESS)
        assertFalse(input.areKeysDown(KeyCodes.KEY_1, KeyCodes.KEY_2))
    }

    fun `areKeysDown and all pressed`() {
        val input = create()
        input.simulate(KeyCodes.KEY_1, KeyChangeTypes.PRESS)
        input.simulate(KeyCodes.KEY_2, KeyChangeTypes.PRESS)
        assertTrue(input.areKeysDown(KeyCodes.KEY_1, KeyCodes.KEY_2))
    }

    fun `getLastPressed but not pressed at all`() {
        val input = create()
        assertTrue(input.getLastPressed(KeyCodes.KEY_1) < 0)
    }

    fun `getLastPressed just now`() {
        val input = create()
        val time = millis()
        input.simulate(KeyCodes.KEY_1, KeyChangeTypes.PRESS)
        assertTrue(input.getLastPressed(KeyCodes.KEY_1) - time < 10)
    }

    fun `check char invoke`() {
        val input = create()

        var pressed: Int? = null
        input.handler.handler = object : InputHandler {
            override fun onCharPress(char: Int): Boolean {
                pressed = char
                return true
            }
        }
        input.simulate(123)
        Assert.assertEquals(pressed, 123)
    }

    fun `don't skip next char if just pushed and popped`() {
        val input = create()

        var pressed: Int? = null
        val handler1 = object : InputHandler {
            override fun onCharPress(char: Int): Boolean {
                pressed = char
                return true
            }
        }
        val handler2 = object : InputHandler {
            override fun onCharPress(char: Int): Boolean {
                pressed = char + 5000
                return true
            }
        }
        input.handler.handler = handler1
        input.handler.handler = null
        input.handler.handler = handler2
        input.simulate(123)
        Assert.assertEquals(pressed, 5123)
    }

    fun `don't skip next char if just pushed and popped and opened via key binding`() {
        val input = create()

        var pressed: Int? = null
        val handler1 = object : InputHandler {
            override fun onCharPress(char: Int): Boolean {
                pressed = char
                return true
            }
        }
        val handler2 = object : InputHandler {
            override fun onCharPress(char: Int): Boolean {
                pressed = char + 5000
                return true
            }
        }
        input.bindings.register(InputTestUtil.dummy, KeyBinding(mapOf(KeyActions.PRESS to setOf(KeyCodes.KEY_1)))) { input.handler.handler = handler1 }
        input.handler.handler = handler1
        input.handler.handler = null
        input.handler.handler = handler2
        input.simulate(123)
        Assert.assertEquals(pressed, 5123)
    }

    fun `check correct set of cursor mode`() {
        val input = create()

        input.context.window.cursorMode = CursorModes.DISABLED
        input.handler.handler = InputTestUtil.Handler
        Assert.assertEquals(input.context.window.cursorMode, CursorModes.NORMAL)
        input.handler.handler = null
        Assert.assertEquals(input.context.window.cursorMode, CursorModes.DISABLED)
    }
}
