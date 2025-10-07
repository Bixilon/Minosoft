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

package de.bixilon.minosoft.terminal.arguments

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import de.bixilon.minosoft.assets.util.AssetsOptions
import de.bixilon.minosoft.config.profile.ProfileOptions
import de.bixilon.minosoft.terminal.RunConfiguration

class StorageArgument : OptionGroup(), AppliedArgument {
    val home by option("--home").file(mustExist = false, canBeFile = false, mustBeReadable = true).default(RunConfiguration.home.toFile())
    val assets by option("--assets").file(mustExist = false, canBeFile = false, mustBeReadable = true).default(AssetsOptions.path.toFile())
    val profiles by option("--profiles", "--config").file(mustExist = false, canBeFile = false, mustBeReadable = true).default(ProfileOptions.path.toFile())

    override fun apply() {
        RunConfiguration.home = home.toPath()
        AssetsOptions.path = assets.toPath()
        ProfileOptions.path = profiles.toPath()
    }
}
