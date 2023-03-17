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

package de.bixilon.minosoft.input.interaction.short

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.camera.target.targets.EntityTarget
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.animal.Pig
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.data.registries.items.CoalTest0
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.input.interaction.InteractionTestUtil
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.assertUseItem
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafePress
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertOnlyPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityEmptyInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityInteractPositionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.PositionRotationC2SP
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.testng.annotations.Test

@Test(groups = ["interaction"], dependsOnGroups = ["item", "block"])
class EntityUseIT {
    private val pig = EntityType(Pig.identifier, Namespaces.minecraft(""), 1.0f, 1.0f, true, false, mapOf(), Pig, null)


    fun testAirOnPig() {
        val connection = InteractionTestUtil.createConnection()
        val entity = Pig(connection, this.pig, EntityData(connection, Int2ObjectOpenHashMap()), Vec3d.EMPTY, EntityRotation.EMPTY)
        connection.world.entities.add(10, null, entity)
        connection.camera.target::target.forceSet(DataObserver(EntityTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, entity)))
        val use = connection.camera.interactions.use

        use.unsafePress()

        connection.assertPacket(EntityInteractPositionC2SP(connection, entity, Vec3.EMPTY, Hands.MAIN, false))
        connection.assertPacket(EntityEmptyInteractC2SP(connection, entity, Hands.MAIN, false))
        connection.assertPacket(EntityInteractPositionC2SP(connection, entity, Vec3.EMPTY, Hands.OFF, false))
        connection.assertOnlyPacket(EntityEmptyInteractC2SP(connection, entity, Hands.OFF, false))
    }

    fun testCoalOnPig() {
        val connection = InteractionTestUtil.createConnection()
        val entity = Pig(connection, this.pig, EntityData(connection, Int2ObjectOpenHashMap()), Vec3d.EMPTY, EntityRotation.EMPTY)
        connection.world.entities.add(10, null, entity)
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(CoalTest0.item)
        connection.camera.target::target.forceSet(DataObserver(EntityTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, entity)))
        val use = connection.camera.interactions.use

        use.unsafePress()

        connection.assertPacket(EntityInteractPositionC2SP(connection, entity, Vec3.EMPTY, Hands.MAIN, false))
        connection.assertPacket(EntityEmptyInteractC2SP(connection, entity, Hands.MAIN, false))
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertPacket(EntityInteractPositionC2SP(connection, entity, Vec3.EMPTY, Hands.OFF, false))
        connection.assertOnlyPacket(EntityEmptyInteractC2SP(connection, entity, Hands.OFF, false))
    }

    fun testCoalOnPig2() {
        val connection = InteractionTestUtil.createConnection()
        val entity = Pig(connection, this.pig, EntityData(connection, Int2ObjectOpenHashMap()), Vec3d.EMPTY, EntityRotation.EMPTY)
        connection.world.entities.add(10, null, entity)
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(CoalTest0.item)
        connection.camera.target::target.forceSet(DataObserver(EntityTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, entity)))
        val use = connection.camera.interactions.use

        use.unsafePress()

        connection.assertPacket(EntityInteractPositionC2SP(connection, entity, Vec3.EMPTY, Hands.MAIN, false))
        connection.assertPacket(EntityEmptyInteractC2SP(connection, entity, Hands.MAIN, false))
        connection.assertPacket(EntityInteractPositionC2SP(connection, entity, Vec3.EMPTY, Hands.OFF, false))
        connection.assertPacket(EntityEmptyInteractC2SP(connection, entity, Hands.OFF, false))
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertOnlyPacket(UseItemC2SP(Hands.OFF))
    }

    fun testCoalOnPig3() {
        val connection = InteractionTestUtil.createConnection()
        val entity = Pig(connection, this.pig, EntityData(connection, Int2ObjectOpenHashMap()), Vec3d.EMPTY, EntityRotation.EMPTY)
        connection.world.entities.add(10, null, entity)
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(CoalTest0.item)
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(CoalTest0.item)
        connection.camera.target::target.forceSet(DataObserver(EntityTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, entity)))
        val use = connection.camera.interactions.use

        use.unsafePress()

        connection.assertPacket(EntityInteractPositionC2SP(connection, entity, Vec3.EMPTY, Hands.MAIN, false))
        connection.assertPacket(EntityEmptyInteractC2SP(connection, entity, Hands.MAIN, false))
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertPacket(EntityInteractPositionC2SP(connection, entity, Vec3.EMPTY, Hands.OFF, false))
        connection.assertPacket(EntityEmptyInteractC2SP(connection, entity, Hands.OFF, false))
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertOnlyPacket(UseItemC2SP(Hands.OFF, 2))
    }

    // TODO: egg on pig
}
