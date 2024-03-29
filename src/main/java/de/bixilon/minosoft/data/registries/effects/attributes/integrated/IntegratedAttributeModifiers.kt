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

package de.bixilon.minosoft.data.registries.effects.attributes.integrated

import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.data.registries.effects.attributes.AttributeOperations
import de.bixilon.minosoft.data.registries.effects.attributes.MinecraftAttributes


object IntegratedAttributeModifiers {
    val SPRINT_SPEED_BOOST = IntegratedAttribute(MinecraftAttributes.MOVEMENT_SPEED, "Sprinting speed boost", "662A6B8D-DA3E-4C1C-8813-96EA6097278D", 0.3f.toDouble(), AttributeOperations.MULTIPLY_TOTAL)
    val POWDER_SNOW_SLOW = "1eaf83ff-7207-4596-b37a-d7a07b3ec4ce".toUUID()
}
