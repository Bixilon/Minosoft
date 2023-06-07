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

package de.bixilon.minosoft.gui.rendering.font.types

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJsonObject
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.types.factory.FontTypeFactory
import de.bixilon.minosoft.gui.rendering.font.types.factory.FontTypes
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

object ReferenceFontType : FontTypeFactory<FontType> {
    override val identifier = minecraft("reference")

    override fun build(context: RenderContext, data: JsonObject): FontType? {
        val id = data["id"]?.toResourceLocation()?.prefix("font/") ?: throw IllegalArgumentException("id missing!")
        val factory = FontTypes[id]
        if (factory == null) {
            Log.log(LogMessageType.ASSETS, LogLevels.WARN) { "Unknown font type $id" }
            return null
        }

        return factory.build(context, context.connection.assetsManager[id].readJsonObject())
    }

}
