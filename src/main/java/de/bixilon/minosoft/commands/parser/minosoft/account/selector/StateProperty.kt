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

package de.bixilon.minosoft.commands.parser.minosoft.account.selector

import de.bixilon.minosoft.commands.errors.ExpectedArgumentError
import de.bixilon.minosoft.commands.parser.minosoft.enums.EnumParser
import de.bixilon.minosoft.commands.parser.selector.TargetProperty
import de.bixilon.minosoft.commands.parser.selector.TargetPropertyFactory
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.AccountStates

class StateProperty(
    val state: AccountStates,
    val negated: Boolean,
) : TargetProperty<Account> {

    override fun passes(value: Account): Boolean {
        val state = value.state
        if (negated) {
            return state != this.state
        }
        return state == this.state
    }

    companion object : TargetPropertyFactory<Account> {
        private val parser = EnumParser(AccountStates)
        override val name: String = "state"

        override fun read(reader: CommandReader): StateProperty {
            val (word, negated) = reader.readNegateable { parser.parse(reader) } ?: throw ExpectedArgumentError(reader)
            return StateProperty(word, negated)
        }
    }
}
