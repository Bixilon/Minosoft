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
package de.bixilon.minosoft.data.player

import de.bixilon.kutil.collections.CollectionUtil.synchronizedBiMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.collections.map.SynchronizedMap
import de.bixilon.kutil.collections.map.bi.SynchronizedBiMap
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.abilities.ItemCooldown
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.InventorySlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.gui.rendering.input.camera.MovementInput
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3i
import kotlin.collections.set

class LocalPlayerEntity(
    account: Account,
    connection: PlayConnection,
) : PlayerEntity(connection, connection.registries.entityTypeRegistry[RemotePlayerEntity.RESOURCE_LOCATION]!!, account.username) {
    val incompleteContainers: SynchronizedMap<Int, SynchronizedMap<Int, ItemStack>> = synchronizedMapOf()
    val inventory = PlayerInventory(connection)
    val containers: SynchronizedBiMap<Int, Container> = synchronizedBiMapOf(
        ProtocolDefinition.PLAYER_CONTAINER_ID to inventory,
    )

    val itemCooldown: MutableMap<Item, ItemCooldown> = synchronizedMapOf()

    val experienceCondition = PlayerExperienceCondition()
    val healthCondition = PlayerHealthCondition()
    val baseAbilities = Abilities()

    @Deprecated(message = "Will be replaced with some kind of teleport manager, ...")
    var isSpawnConfirmed = false
    var spawnPosition: Vec3i = Vec3i.EMPTY

    var movementInput = MovementInput.EMPTY


    val reachDistance: Double
        get() = (gamemode == Gamemodes.CREATIVE).decide(5.0, 4.5)

    override val equipment: LockMap<InventorySlots.EquipmentSlots, ItemStack>
        get() = inventory.equipment

    override val health: Double
        get() = healthCondition.hp.toDouble()

    override val mainArm: Arms
        get() = connection.profiles.connection.mainArm


    var selectedHotbarSlot: Int = 0
        set(value) {
            if (field == value) {
                return
            }
            field = value
            equipment.remove(InventorySlots.EquipmentSlots.MAIN_HAND)
            equipment[InventorySlots.EquipmentSlots.MAIN_HAND] = inventory.getHotbarSlot(value) ?: return
        }


    fun useItem(hand: Hands) {
        TODO()
    }
}
