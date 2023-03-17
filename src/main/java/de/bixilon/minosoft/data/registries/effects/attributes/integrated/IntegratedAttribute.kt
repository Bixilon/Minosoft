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

package de.bixilon.minosoft.data.registries.effects.attributes.integrated

import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.data.registries.effects.attributes.AttributeOperations
import de.bixilon.minosoft.data.registries.effects.attributes.AttributeType
import de.bixilon.minosoft.data.registries.effects.attributes.container.AttributeModifier
import java.util.*

class IntegratedAttribute(
    val attribute: AttributeType,
    name: String,
    uuid: UUID,
    amount: Double,
    operation: AttributeOperations,
) {
    val modifier = AttributeModifier(name, uuid, amount, operation)

    constructor(type: AttributeType, name: String, uuid: String, amount: Double, operation: AttributeOperations) : this(type, name, uuid.toUUID(), amount, operation)
}
