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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.bossbar

import de.bixilon.minosoft.data.bossbar.BossbarColors
import de.bixilon.minosoft.data.bossbar.BossbarNotches
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement

class BossbarAtlas(atlas: Atlas) {
    val colors = Array(BossbarColors.VALUES.size) {
        arrayOf(
            atlas[BossbarColors[it].name.lowercase() + "_empty"],
            atlas[BossbarColors[it].name.lowercase() + "_full"]
        )
    }

    operator fun get(color: BossbarColors): Array<AtlasElement?> = colors[color.ordinal]

    val notches = Array(BossbarNotches.NOTCHES.size) {
        arrayOf(
            atlas[BossbarNotches.NOTCHES[it].name.lowercase() + "_empty"],
            atlas[BossbarNotches.NOTCHES[it].name.lowercase() + "_full"]
        )
    }

    operator fun get(notches: BossbarNotches): Array<AtlasElement?> = this.notches[notches.ordinal - 1]
}
