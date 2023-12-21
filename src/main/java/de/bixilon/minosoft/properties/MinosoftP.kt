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

package de.bixilon.minosoft.properties

import de.bixilon.minosoft.config.DebugOptions
import de.bixilon.minosoft.properties.general.GeneralP
import de.bixilon.minosoft.properties.git.GitP

data class MinosoftP(
    val general: GeneralP,
    val git: GitP?,
) {

    fun canUpdate(): Boolean {
        if (DebugOptions.FORCE_CHECK_UPDATES) return true
        val properties = MinosoftProperties
        if (properties.git != null && (properties.git.dirty || properties.git.branch != "master")) {
            // clearly self built, not checking for updates
            return false
        }
        if (!properties.general.updates) {
            // version is from 3rd party stores or self built
            return false
        }
        return true
    }
}
