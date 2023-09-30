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

package de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties

import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.position.center.XCenterProperty
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.position.center.YCenterProperty
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.position.center.ZCenterProperty
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.position.distance.DistanceProperty
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.rotation.PitchRotation
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.rotation.YawRotation
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.sort.SortProperty

// See https://minecraft.wiki/w/Target_selectors
object EntityTargetProperties {
    val properties: MutableMap<String, EntityTargetPropertyFactory<*>> = mutableMapOf()


    init {
        register(XCenterProperty)
        register(YCenterProperty)
        register(ZCenterProperty)

        register(SortProperty)
        register(PitchRotation)
        register(YawRotation)
        register(DistanceProperty)
        register(GamemodeProperty)
        register(NameProperty)
        register(TypeProperty)
        register(LimitProperty)
        register(LevelProperty)

        // ToDo
        /*
        var volumeX: Double?,
        var volumeY: Double?,
        var volumeZ: Double?,
        var scores: Any?,
        var tag: Any?,
        var team: Any?,
        var nbt: Any? = null,
        var advancements: Any? = null,
        var predicate: Any? = null,
         */
    }

    fun register(factory: EntityTargetPropertyFactory<*>) {
        properties[factory.name] = factory
    }

    operator fun get(key: String): EntityTargetPropertyFactory<*>? {
        return properties[key]
    }
}
