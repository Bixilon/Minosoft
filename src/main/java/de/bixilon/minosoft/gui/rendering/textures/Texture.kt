/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

@Deprecated(message = "")
object Texture {
    fun getResourceTextureIdentifier(namespace: String = ProtocolDefinition.DEFAULT_NAMESPACE, textureName: String): ResourceLocation {
        var namespace = namespace
        var texturePath = textureName

        if (texturePath.contains(":")) {
            val split = texturePath.split(":")
            namespace = split[0]
            texturePath = split[1]
        }

        texturePath = texturePath.removePrefix("/")

        if (!texturePath.startsWith("textures/")) {
            texturePath = "textures/$texturePath"
        }
        if (!texturePath.endsWith(".png")) {
            texturePath = "$texturePath.png"
        }
        return ResourceLocation(namespace, texturePath)
    }
}
