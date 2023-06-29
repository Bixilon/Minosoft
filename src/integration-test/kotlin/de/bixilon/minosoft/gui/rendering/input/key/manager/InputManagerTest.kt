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

import de.bixilon.kutil.collections.map.SynchronizedMap
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfile
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.input.InputHandler
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.BindingsManager
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.actions.bindingsPressed
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.actions.keysPressed
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.test.IT.OBJENESIS
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["input"])
class InputManagerTest {
    private val profile = BindingsManager::class.java.getDeclaredField("profile").apply { isAccessible = true }
    private val bindings = BindingsManager::class.java.getDeclaredField("bindings").apply { isAccessible = true }
    private val onKey = InputManager::class.java.getDeclaredMethod("onKey", KeyCodes::class.java, KeyChangeTypes::class.java).apply { isAccessible = true }

    private fun create(): InputManager {
        val manager = OBJENESIS.newInstance(InputManager::class.java)

        val bindings = OBJENESIS.newInstance(BindingsManager::class.java)
        bindingsPressed[bindings] = mutableSetOf<ResourceLocation>()
        profile[bindings] = ControlsProfile()
        this.bindings[bindings] = SynchronizedMap<Any, Any>()


        manager::bindings.forceSet(bindings)
        keysPressed[manager] = mutableSetOf<KeyCodes>()

        return manager
    }

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


    private fun InputManager.simulate(code: KeyCodes, change: KeyChangeTypes) {
        onKey.invoke(this, code, change)
    }

    private object Handler : InputHandler

    private val dummy = minosoft("dummy")
}
