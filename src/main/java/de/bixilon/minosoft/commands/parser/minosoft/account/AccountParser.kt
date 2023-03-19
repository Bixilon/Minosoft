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

package de.bixilon.minosoft.commands.parser.minosoft.account

import de.bixilon.minosoft.commands.errors.ExpectedArgumentError
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.parser.minosoft.account.identifier.AccountId
import de.bixilon.minosoft.commands.parser.minosoft.account.selector.AccountTargetProperties
import de.bixilon.minosoft.commands.parser.selector.AbstractTarget
import de.bixilon.minosoft.commands.parser.selector.SelectorParser
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object AccountParser : SelectorParser<Account>(), ArgumentParserFactory<AccountParser> {
    override val identifier: ResourceLocation = "minosoft:account".toResourceLocation()
    override val properties get() = AccountTargetProperties

    override fun parseId(reader: CommandReader): AbstractTarget<Account> {
        val result = reader.readResult { reader.readString() }
        if (result.result == null) {
            throw ExpectedArgumentError(reader)
        }
        return AccountId(result.result)
    }

    override fun read(buffer: PlayInByteBuffer): AccountParser = this
}
