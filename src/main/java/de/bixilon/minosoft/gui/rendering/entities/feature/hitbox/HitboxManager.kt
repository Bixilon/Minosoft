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

package de.bixilon.minosoft.gui.rendering.entities.feature.hitbox

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.feature.register.FeatureRegister
import de.bixilon.minosoft.util.KUtil.format

class HitboxManager(private val renderer: EntitiesRenderer) : FeatureRegister {
    val profile = renderer.profile.hitbox
    val shader = renderer.context.shaders.genericColorShader

    var enabled = profile.enabled


    override fun init() {
        profile::enabled.observe(this) { this.enabled = it }
        registerKeybinding()
    }

    private fun registerKeybinding() {
        renderer.context.input.bindings.register(TOGGLE, KeyBinding(
            KeyActions.MODIFIER to setOf(KeyCodes.KEY_F3),
            KeyActions.STICKY to setOf(KeyCodes.KEY_B),
        ), pressed = enabled) {
            profile.enabled = it
            renderer.connection.util.sendDebugMessage("Entity hit boxes: ${it.format()}")
            enabled = it
        }
    }

    companion object {
        val TOGGLE = minosoft("toggle_hitboxes")
    }
}
