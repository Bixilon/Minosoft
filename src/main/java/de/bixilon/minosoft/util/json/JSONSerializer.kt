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

package de.bixilon.minosoft.util.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.bixilon.minosoft.config.config.Config
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties

object JSONSerializer {
    private val MOSHI = Moshi.Builder()
        .add(RGBColorSerializer)
        .add(Vec2Serializer)
        .add(AccountSerializer)
        .add(ServerSerializer)
        .add(ResourceLocationSerializer)
        .add(KotlinJsonAdapterFactory())
        .build()!!

    val ANY_ADAPTER = MOSHI.adapter(Any::class.java)!!
    val CONFIG_ADAPTER = MOSHI.adapter(Config::class.java)!!
    val MAP_ADAPTER: JsonAdapter<MutableMap<String, Any>> = MOSHI.adapter(Types.newParameterizedType(MutableMap::class.java, String::class.java, Any::class.java))

    val IMAGE_PROPERTIES_ADAPTER = MOSHI.adapter(ImageProperties::class.java)!!
}