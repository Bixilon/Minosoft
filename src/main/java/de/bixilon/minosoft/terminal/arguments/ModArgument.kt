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
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import de.bixilon.kutil.uri.URIUtil.toURI
import de.bixilon.minosoft.modding.loader.ModOptions
import de.bixilon.minosoft.modding.loader.mod.source.ModSource
import de.bixilon.minosoft.modding.loader.phase.DefaultModPhases

class ModArgument : OptionGroup(), AppliedArgument {
    val disable by option("--no-mods").flag(default = ModOptions.disabled)
    val ignoreMods by option("--ignore-mod").multiple(required = false).unique()
    val ignorePhases by option("--ignore-mod-phase").choice(DefaultModPhases.PRE.name, DefaultModPhases.BOOT.name, DefaultModPhases.POST.name).multiple(required = false).unique()
    val sources by option("--mod-source").splitPair("=").convert { pair -> Pair(pair.first, ModSource.of(pair.second.toURI())) }.multiple(required = false).unique()

    override fun apply() {
        ModOptions.disabled = disable
        ModOptions.ignoreMods = ignoreMods
        ModOptions.ignorePhases = ignorePhases

        for ((phase, source) in sources) {
            ModOptions.additional.getOrPut(phase) { mutableSetOf() } += source
        }
    }
}
