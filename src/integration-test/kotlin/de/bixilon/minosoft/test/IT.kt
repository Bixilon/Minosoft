/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.test

import de.bixilon.minosoft.MinosoftSIT
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.Andesite
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.Cobblestone
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.StoneBlock
import de.bixilon.minosoft.data.registries.fallback.tags.FallbackTags
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.test.ITUtil.allocate

object IT {

    init {
        MinosoftSIT.setup()
    }


    var VERSION = Versions["1.19.3"]!!
    var REGISTRIES = ITUtil.loadRegistries(VERSION)


    var VERSION_LEGACY = Versions["1.12.2"]!!
    var REGISTRIES_LEGACY = ITUtil.loadRegistries(VERSION_LEGACY)


    var FALLBACK_TAGS = FallbackTags.map(REGISTRIES)


    val BLOCK_1 = REGISTRIES.block[StoneBlock.Block]!!.states.default
    val BLOCK_2 = REGISTRIES.block[Cobblestone.Block]!!.states.default
    val BLOCK_3 = REGISTRIES.block[Andesite.Block]!!.states.default


    val NULL_CONNECTION = PlaySession::class.java.allocate()
}
