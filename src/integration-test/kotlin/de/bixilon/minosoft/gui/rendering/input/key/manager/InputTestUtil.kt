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
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.kutil.unsafe.UnsafeUtil.setUnsafeAccessible
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfile
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.input.InputHandler
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.BindingsManager
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.actions.bindingsPressed
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.actions.keysPressed
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.system.window.dummy.DummyWindow
import de.bixilon.minosoft.test.IT
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap

object InputTestUtil {
    private val profile = BindingsManager::class.java.getFieldOrNull("profile")!!
    private val bindings = BindingsManager::class.java.getFieldOrNull("bindings")!!
    private val onKey = InputManager::class.java.getDeclaredMethod("onKey", KeyCodes::class.java, KeyChangeTypes::class.java).apply { setUnsafeAccessible() }
    private val onChar = InputManager::class.java.getDeclaredMethod("onChar", Int::class.java).apply { setUnsafeAccessible() }
    private val times = InputManager::class.java.getFieldOrNull("times")!!


    fun create(): InputManager {
        val manager = IT.OBJENESIS.newInstance(InputManager::class.java)
        val context = IT.OBJENESIS.newInstance(RenderContext::class.java)
        context::window.forceSet(DummyWindow())
        manager::context.forceSet(context)

        val bindings = IT.OBJENESIS.newInstance(BindingsManager::class.java)
        bindings::input.forceSet(manager)
        bindingsPressed[bindings] = mutableSetOf<ResourceLocation>()
        profile[bindings] = ControlsProfile()
        this.bindings[bindings] = SynchronizedMap<Any, Any>()

        val handler = InputHandlerManager(manager)


        manager::bindings.forceSet(bindings)
        manager::handler.forceSet(handler)
        this.times.forceSet(manager, Object2LongOpenHashMap<KeyCodes>().apply { defaultReturnValue(-1L) })
        keysPressed[manager] = KeyCodes.set()

        return manager
    }


    fun InputManager.simulate(code: KeyCodes, change: KeyChangeTypes) {
        onKey.invoke(this, code, change)
    }

    fun InputManager.simulate(char: Int) {
        onChar.invoke(this, char)
    }

    object Handler : InputHandler

    val dummy = Namespaces.minosoft("dummy")
}
