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
package de.bixilon.minosoft.data.registries.materials

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.codec.ResourceLocationCodec
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import java.util.*

data class Material(
    override val identifier: ResourceLocation,
    val color: RGBColor?,
    val pushReaction: PushReactions,
    val blockMotion: Boolean,
    val flammable: Boolean,
    val liquid: Boolean,
    val soft: Boolean,
    val solidBlocking: Boolean,
    val replaceable: Boolean,
    val solid: Boolean,
) : RegistryItem() {

    override fun toString(): String {
        return identifier.toString()
    }

    companion object : ResourceLocationCodec<Material> {
        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): Material {
            return Material(
                identifier = resourceLocation,
                color = TintManager.getJsonColor(data["color"]?.toInt() ?: 0),
                pushReaction = data["push_reaction"].nullCast<String>()?.let { PushReactions.valueOf(it.uppercase(Locale.getDefault())) } ?: PushReactions.NORMAL,
                blockMotion = data["blocks_motion"]?.toBoolean() ?: false,
                flammable = data["flammable"]?.toBoolean() ?: false,
                liquid = data["liquid"]?.toBoolean() ?: false,
                soft = data["is_soft"]?.toBoolean() ?: false,
                solidBlocking = data["solid_blocking"]?.toBoolean() ?: false,
                replaceable = data["replaceable"]?.toBoolean() ?: false,
                solid = data["solid"]?.toBoolean() ?: false,
            )
        }
    }
}
