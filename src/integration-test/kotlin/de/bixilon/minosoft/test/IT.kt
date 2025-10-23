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

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.tags.TagManager
import org.objenesis.ObjenesisStd

object IT {
    val OBJENESIS = ObjenesisStd()
    const val TEST_VERSION_NAME = "1.19.3"
    var VERSION: Version = unsafeNull()
    var REGISTRIES: Registries = unsafeNull()
    var FALLBACK_TAGS: TagManager = unsafeNull()
    val NULL_CONNECTION = OBJENESIS.newInstance(PlaySession::class.java)


    var VERSION_LEGACY: Version = unsafeNull()
    var REGISTRIES_LEGACY: Registries = unsafeNull()


    val BLOCK_1: BlockState = unsafeNull()
    val BLOCK_2: BlockState = unsafeNull()
    val BLOCK_3: BlockState = unsafeNull()
}
