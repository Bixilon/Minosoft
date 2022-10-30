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

package de.bixilon.minosoft.util.crash.section

import de.bixilon.minosoft.modding.loader.ModLoader
import de.bixilon.minosoft.modding.loader.mod.MinosoftMod

class ModCrashSection(
    connections: Array<MinosoftMod> = ModLoader.mods.mods.values.toTypedArray(),
) : ArrayCrashSection<MinosoftMod>("Mods", connections) {

    override fun format(entry: MinosoftMod, builder: StringBuilder, intent: String) {
        builder.appendProperty(intent, "Path", entry.path)
        builder.appendProperty(intent, "Phase", entry.phase)
        builder.appendProperty(intent, "Name", entry.manifest?.name)
        builder.appendProperty(intent, "Version", entry.manifest?.version)
        builder.appendProperty(intent, "Author(s)", entry.manifest?.authors)
        builder.appendProperty(intent, "Website", entry.manifest?.website)
        builder.appendProperty(intent, "Main", entry.manifest?.main)
    }
}
