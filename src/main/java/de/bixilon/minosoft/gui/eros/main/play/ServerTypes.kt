/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.eros.main.play

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.registry.Translatable
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.enum.ValuesEnum
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

enum class ServerTypes(val icon: Ikon) : Translatable {
    CUSTOM(FontAwesomeSolid.SERVER),
    LAN(FontAwesomeSolid.NETWORK_WIRED),
    ;

    override val translationKey: ResourceLocation = "minosoft:server_type.${name.lowercase()}".asResourceLocation()


    companion object : ValuesEnum<ServerTypes> {
        override val VALUES: Array<ServerTypes> = values()
        override val NAME_MAP: Map<String, ServerTypes> = KUtil.getEnumValues(VALUES)
    }
}
