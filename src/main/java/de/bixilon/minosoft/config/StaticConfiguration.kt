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
package de.bixilon.minosoft.config

object StaticConfiguration {
    const val DEBUG_MODE = true // if true, additional checks will be made to validate data, ... Decreases performance
    const val REPLACE_SYSTEM_OUT_STREAMS = true // Replace System.out and System.err with the custom Log system ones


    const val IGNORE_SERVER_LIGHT = true

    const val VALIDATE_RESOURCE_LOCATION = true

    const val REGISTRY_ITEM_COMPARE_CLASS = true
}
