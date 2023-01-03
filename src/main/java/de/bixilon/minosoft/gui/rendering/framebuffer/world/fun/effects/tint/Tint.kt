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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.`fun`.effects.tint

import de.bixilon.kutil.random.RandomUtil.nextInt
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.world.`fun`.FunEffect
import de.bixilon.minosoft.gui.rendering.framebuffer.world.`fun`.FunEffectFactory
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.util.*

class Tint(override val context: RenderContext) : FunEffect {
    override val identifier: ResourceLocation get() = Tint.identifier
    override val shader = createShader(fragment = "minosoft:framebuffer/world/fun/tint.fsh".toResourceLocation()) { TintShader(it) }
    private var updateUniform = true
    var color = Random().let { RGBColor(it.nextInt(20, 255), it.nextInt(20, 255), it.nextInt(20, 255), 0xFF) }
        set(value) {
            field = value
            updateUniform = true
        }

    override fun update() {
        if (updateUniform) {
            shader.tintColor = color
            updateUniform = false
        }
    }


    companion object : FunEffectFactory<Tint> {
        override val identifier: ResourceLocation = "minosoft:tint".toResourceLocation()

        override fun build(context: RenderContext): Tint {
            return Tint(context)
        }
    }
}
