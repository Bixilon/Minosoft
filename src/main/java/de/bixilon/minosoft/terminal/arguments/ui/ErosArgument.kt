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

package de.bixilon.minosoft.terminal.arguments.ui

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.deprecated
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import de.bixilon.minosoft.gui.eros.ErosOptions
import de.bixilon.minosoft.terminal.arguments.AppliedArgument

class ErosArgument : OptionGroup(), AppliedArgument {
    private val _disable by option("--disable_eros").flag(default = ErosOptions.disabled).deprecated("--disable_eros is deprecated, use --no-eros instead")
    val disable by option("--no-eros").flag(default = ErosOptions.disabled)

    override fun apply() {
        ErosOptions.disabled = _disable || disable
    }
}
