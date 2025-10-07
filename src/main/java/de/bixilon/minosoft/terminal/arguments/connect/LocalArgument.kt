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

package de.bixilon.minosoft.terminal.arguments.connect

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.enums.ValuesEnum.Companion.names
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.local.LocalConnection
import de.bixilon.minosoft.local.generator.ChunkGenerator
import de.bixilon.minosoft.local.generator.DebugGenerator
import de.bixilon.minosoft.local.generator.VoidGenerator
import de.bixilon.minosoft.local.generator.flat.FlatGenerator
import de.bixilon.minosoft.local.storage.DebugStorage
import de.bixilon.minosoft.local.storage.MemoryStorage
import de.bixilon.minosoft.local.storage.WorldStorage
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class LocalArgument : OptionGroup(), AutoConnectFactory {
    val generator by option("--world-generator").enum<WorldGenerators>().default(WorldGenerators.DEFAULT)
    val storage by option("--world-storage").enum<WorldStorages>().defaultLazy { if (generator == WorldGenerators.DEBUG) WorldStorages.DEBUG else WorldStorages.MEMORY }


    override fun create(version: Version, account: Account): PlaySession {
        val version = if (version == Versions.AUTOMATIC) Versions["1.20.4"]!! else version
        Log.log(LogMessageType.AUTO_CONNECT, LogLevels.INFO) { "Starting local world (generator=${generator.name.lowercase()}, storage=${storage.name.lowercase()}) with version $version using account $account..." }
        return PlaySession(
            connection = LocalConnection(::DebugGenerator, ::DebugStorage),
            account = account,
            version = version,
        )
    }

    enum class WorldGenerators(val factory: (PlaySession) -> ChunkGenerator) {
        DEBUG(::DebugGenerator),
        FLAT(FlatGenerator.Companion::default),
        VOID({ VoidGenerator })
        ;

        companion object : ValuesEnum<WorldGenerators> {
            override val VALUES = values()
            override val NAME_MAP = names()

            val DEFAULT = FLAT
        }
    }

    enum class WorldStorages(val factory: (PlaySession) -> WorldStorage) {
        DEBUG(::DebugStorage),
        MEMORY(::MemoryStorage),
        ;

        companion object : ValuesEnum<WorldStorages> {
            override val VALUES = values()
            override val NAME_MAP = names()
        }
    }
}
