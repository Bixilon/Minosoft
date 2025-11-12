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
import kotlin.system.exitProcess

object IT {

    init {
        try {
            MinosoftSIT.setup()
        } catch (error: Throwable) {
            error.printStackTrace()
            exitProcess(1)
        }
    }


    var VERSION = Versions["1.19.3"]!!
    var REGISTRIES = try {
        ITUtil.loadRegistries(VERSION)
    } catch (error: Throwable) {
        error.printStackTrace()
        exitProcess(1)
    }


    var VERSION_LEGACY = Versions["1.12.2"]!!
    var REGISTRIES_LEGACY = ITUtil.loadRegistries(VERSION_LEGACY)


    var FALLBACK_TAGS = FallbackTags.map(REGISTRIES)


    @Deprecated("TestBlockStates.OPAQUE1")
    val BLOCK_1 = REGISTRIES.block[StoneBlock.Block]!!.states.default

    @Deprecated("TestBlockStates.OPAQUE2")
    val BLOCK_2 = REGISTRIES.block[Cobblestone.Block]!!.states.default

    @Deprecated("TestBlockStates.OPAQUE3")
    val BLOCK_3 = REGISTRIES.block[Andesite.Block]!!.states.default


    val NULL_CONNECTION = PlaySession::class.java.allocate()
}
