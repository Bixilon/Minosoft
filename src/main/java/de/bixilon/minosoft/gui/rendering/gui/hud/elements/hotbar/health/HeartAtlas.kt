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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar.health

import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas.Companion.get
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement

class HeartAtlas(private val atlas: Atlas?) {
    val normal = HartType("")
    val poisoned = HartType("poisoned")
    val withered = HartType("withered")
    val absorption = HartType("absorption")
    val frozen = HartType("frozen")


    val container = atlas["container"] // TODO: There are multiple containers

    inner class HartType(type: String) {
        val hearts = Array(2) { full ->
            Array(2) { hardcore ->
                Array(2) { blinking ->
                    val name = StringBuilder()
                    if (type.isNotEmpty()) {
                        name.append(type)
                        name.append('_')
                    }
                    name.append(if (full > 0) "full" else "half")
                    if (hardcore > 0) name.append("_hardcore")
                    if (blinking > 0) name.append("_blinking")

                    atlas?.get(name.toString())
                }
            }
        } // frozen_full_hardcore_blinking

        fun get(full: Boolean, hardcore: Boolean, blinking: Boolean): AtlasElement? = this.hearts[full.toInt()][hardcore.toInt()][blinking.toInt()]
    }


    companion object {
        val ATLAS = minecraft("hud/hotbar/hearts")
    }
}
