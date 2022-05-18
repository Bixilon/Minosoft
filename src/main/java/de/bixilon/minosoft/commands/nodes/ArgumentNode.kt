/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.commands.nodes

import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.stack.CommandExecutor
import de.bixilon.minosoft.commands.suggestion.types.SuggestionType

class ArgumentNode : ExecutableNode {
    private val parser: ArgumentParser<*>

    constructor(
        name: String,
        parser: ArgumentParser<*>,
        suggestion: SuggestionType<*>? = null,
        executable: Boolean = false,
        redirect: CommandNode? = null,
    ) : super(name, suggestion, false, null, executable, redirect) {
        this.parser = parser
    }


    constructor(name: String, parser: ArgumentParser<*>, onlyDirectExecution: Boolean = true, executor: CommandExecutor) : super(name, executable = true, onlyDirectExecution = onlyDirectExecution, executor = executor) {
        this.executor = executor
        this.parser = parser
    }
}
