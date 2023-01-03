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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.`fun`

import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.FramebufferShader
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class FunEffectManager(
    private val context: RenderContext,
) {
    private val effects: MutableMap<ResourceLocation, FunEffect> = mutableMapOf()
    var effect: FunEffect? = null
        private set
    val shader: FramebufferShader?
        get() = effect?.shader
    private var effectUsed = 0


    init {
        context.inputHandler.registerKeyCallback(
            "minosoft:switch_fun_settings".toResourceLocation(),
            KeyBinding(
                KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4),
                KeyActions.PRESS to setOf(KeyCodes.KEY_J),
            )
        ) {
            effectUsed++
            if (effectUsed > DefaultFunEffects.size) {
                effectUsed = 0
            }
            if (effectUsed == 0) {
                effect = null
            } else {
                setEffect(DefaultFunEffects[effectUsed - 1])
            }
        }
    }

    fun setEffect(effect: FunEffectFactory<*>?) {
        if (effect == null) {
            this.effect = null
            return
        }
        this.effect = this.effects.getOrPut(effect.identifier) { effect.build(context) }
    }

    fun preDraw() {
        effect?.update()
        effect?.preDraw()
    }
}
