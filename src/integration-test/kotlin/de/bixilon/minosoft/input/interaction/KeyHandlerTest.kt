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

package de.bixilon.minosoft.input.interaction

import de.bixilon.kutil.environment.Environment
import de.bixilon.kutil.time.TimeUtil.sleep
import de.bixilon.minosoft.input.interaction.KeyHandlerUtil.awaitTicks
import org.testng.AssertJUnit.assertEquals
import org.testng.annotations.Test
import kotlin.time.Duration.Companion.milliseconds

@Test(groups = ["interaction"])
class KeyHandlerTest {

    fun `single press`() {
        val handler = TestKeyHandler()
        handler.press()
        assertEquals(handler.actions.toList(), listOf(
            TestKeyHandler.Actions.PRESS,
        ))
        handler.release()
    }

    fun `tick once`() {
        if (Environment.isInCI()) return // TODO: Broken there???
        val handler = TestKeyHandler()
        handler.press()
        handler.awaitTicks(1)
        sleep(5.milliseconds) // no clue
        assertEquals(handler.actions.toList(), listOf(
            TestKeyHandler.Actions.PRESS,
            TestKeyHandler.Actions.TICK,
        ))
        handler.release()
    }

    fun `tick twice`() {
        if (Environment.isInCI()) return // TODO: Broken there???
        val handler = TestKeyHandler()
        handler.press()
        handler.awaitTicks(2)
        sleep(5.milliseconds) // no clue
        handler.release()
        assertEquals(handler.actions.toList(), listOf(
            TestKeyHandler.Actions.PRESS,
            TestKeyHandler.Actions.TICK,
            TestKeyHandler.Actions.TICK,
            TestKeyHandler.Actions.RELEASE,
        ))
    }

    fun `press and release`() {
        val handler = TestKeyHandler()
        handler.press()
        handler.release()
        assertEquals(handler.actions.toList(), listOf(
            TestKeyHandler.Actions.PRESS,
            TestKeyHandler.Actions.RELEASE,
        ))
    }


    class TestKeyHandler : KeyHandler() {
        val actions: MutableList<Actions> = mutableListOf()

        enum class Actions {
            PRESS, TICK, RELEASE,
        }

        override fun onPress() {
            this.actions += Actions.PRESS
        }

        override fun onRelease() {
            this.actions += Actions.RELEASE
        }

        override fun onTick() {
            this.actions += Actions.TICK
        }
    }
}
