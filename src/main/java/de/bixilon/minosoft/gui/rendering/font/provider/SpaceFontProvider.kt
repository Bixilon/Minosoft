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

package de.bixilon.minosoft.gui.rendering.font.provider
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.CharData
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class SpaceFontProvider(
    context: RenderContext,
    data: Map<String, Any>,
) : FontProvider {
    private val chars: Int2ObjectOpenHashMap<CharData> = Int2ObjectOpenHashMap()

    init {
        data["advances"]?.toJsonObject()?.let {
            for ((charData, spacing) in it) {
                val char = charData.codePoints().iterator().nextInt()
                chars[char] = CharData(context, null, spacing.toInt(), spacing.toInt(), Vec2.EMPTY, Vec2.EMPTY)
            }
        }
    }

    override fun postInit(latch: CountUpAndDownLatch) = Unit

    override fun get(char: Int): CharData? {
        return chars[char]
    }

    companion object : FontProviderFactory<SpaceFontProvider> {
        override val identifier: ResourceLocation = "minecraft:space".toResourceLocation()

        override fun build(context: RenderContext, data: Map<String, Any>): SpaceFontProvider {
            return SpaceFontProvider(context, data)
        }
    }
}
