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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.tab

import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas.Companion.get
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement

class TabListAtlas(val atlas: Atlas?) {
    private val ping = Array(6) { atlas["ping_$it"] }


    constructor(gui: GUIRenderer) : this(gui.atlas[ATLAS])

    fun getPing(ping: Int): AtlasElement? {
        return this.ping[when {
            ping < 0 -> 0
            ping < 150 -> 5
            ping < 300 -> 4
            ping < 600 -> 3
            ping < 1000 -> 2
            else -> 1
        }]
    }


    companion object {
        val ATLAS = minecraft("hud/tab")
    }
}
