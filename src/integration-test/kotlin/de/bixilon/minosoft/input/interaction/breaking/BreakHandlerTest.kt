/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.input.interaction.breaking

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.StatusEffectInstance
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.building.WoolBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.StoneBlock
import de.bixilon.minosoft.data.registries.blocks.types.pvp.CobwebBlock
import de.bixilon.minosoft.data.registries.blocks.types.wood.Oak
import de.bixilon.minosoft.data.registries.effects.mining.MiningEffect
import de.bixilon.minosoft.data.registries.enchantment.armor.ArmorEnchantment
import de.bixilon.minosoft.data.registries.enchantment.tool.MiningEnchantment
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.tool.materials.*
import de.bixilon.minosoft.data.registries.item.items.tool.shears.ShearsItem
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.tick
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafePress
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafeRelease
import de.bixilon.minosoft.input.interaction.breaking.executor.TestExecutor
import de.bixilon.minosoft.input.interaction.breaking.survival.BlockBreakProductivity
import de.bixilon.minosoft.input.interaction.breaking.survival.BlockDigStatus
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.PlayerActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.SwingArmC2SP
import de.bixilon.minosoft.test.ITUtil.todo
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["interaction"])
class BreakHandlerTest {

    private fun breakBlock(
        block: ResourceLocation,
        tool: Identified? = null,
        gamemode: Gamemodes = Gamemodes.SURVIVAL,
        efficiency: Int = 0,
        haste: Int = 0,
        miningFatigue: Int = 0,
        aquaAffinity: Int = 0,
        inWater: Boolean = false,
        onGround: Boolean = true,
        distance: Double = 1.0,
        ticks: Int,
    ): BlockDigStatus? {
        val session = createSession(1)
        val player = createPlayer(session)
        player.physics().onGround = onGround
        session.player.physics.submersion::eye.forceSet(if (inWater) session.registries.fluid[WaterFluid] else null)
        val state = createTarget(session, block, distance)

        player.additional.gamemode = gamemode
        if (tool != null) {
            val item = session.registries.item[tool] ?: Broken()
            val stack = ItemStack(item)
            if (efficiency > 0) {
                stack.enchanting.enchantments[MiningEnchantment.Efficiency] = efficiency
            }
            player.items.inventory[EquipmentSlots.MAIN_HAND] = stack
        }
        if (haste > 0) player.effects += StatusEffectInstance(MiningEffect.Haste, haste, 100000)
        if (miningFatigue > 0) player.effects += StatusEffectInstance(MiningEffect.MiningFatigue, miningFatigue, 100000)
        if (aquaAffinity > 0) {
            player.items.inventory[EquipmentSlots.HEAD] = ItemStack(session.registries.item["minecraft:iron_helmet"]!!, 1).apply { enchanting.enchantments[ArmorEnchantment.AquaAffinity] = 1 }
        }


        val handler = BreakHandler(session.camera.interactions)
        handler::executor.forceSet(TestExecutor(handler))

        handler.unsafePress()
        var status: BlockDigStatus? = null
        if (ticks > 1) {
            for (tick in 0 until ticks - 2) { // -1 for count, -1 for post ticking
                handler.tick()
                assertEquals(session.world[BlockPosition(1, 2, 3)], state, "Block got mined in tick $tick, expected $ticks")
            }
            status = handler.digging.status
            handler.tick()
        }
        if (ticks < 0) {
            status = handler.digging.status
        }

        if (ticks >= 0) {
            val existing = session.world[BlockPosition(1, 2, 3)]
            assertNull(existing, "Block is still present, progress=${status?.progress}")
        }


        // TODO: packets

        return status
    }

    private fun BlockDigStatus.assert(productivity: BlockBreakProductivity? = null) {
        productivity?.let { assertEquals(this.productivity, productivity) }
    }

    fun nothingWool() {
        breakBlock(WoolBlock.Red.identifier, ticks = 25)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun pickaxeWool() {
        breakBlock(WoolBlock.Red.identifier, WoodenTool.WoodenPickaxe, ticks = 25)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun shearsWool() {
        breakBlock(WoolBlock.Red.identifier, ShearsItem, ticks = 6)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun efficiencyShearsWool() {
        breakBlock(WoolBlock.Red.identifier, ShearsItem, efficiency = 5, ticks = 0)
    }

    fun pickaxeGlass() {
        breakBlock(MinecraftBlocks.RED_STAINED_GLASS, WoodenTool.WoodenAxe, ticks = 10)!!.apply { todo(); assert(productivity = BlockBreakProductivity.EFFECTIVE) }
    }

    fun fistGlass() {
        breakBlock(MinecraftBlocks.RED_STAINED_GLASS, ticks = 10)!!.apply { todo(); assert(productivity = BlockBreakProductivity.EFFECTIVE) }
    }

    fun fistLadder() {
        breakBlock(MinecraftBlocks.LADDER, ticks = 14)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun woodenAxeLadder() {
        breakBlock(MinecraftBlocks.LADDER, WoodenTool.WoodenAxe, ticks = 7)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun diamondAxeLadder() {
        breakBlock(MinecraftBlocks.LADDER, DiamondTool.DiamondAxe, ticks = 3)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }


    fun nothingPlanks() {
        breakBlock(Oak.Planks.identifier, ticks = 62)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun pickaxePlanks() {
        breakBlock(Oak.Planks.identifier, WoodenTool.WoodenPickaxe, ticks = 62)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun woodenAxePlanks() {
        breakBlock(Oak.Planks.identifier, WoodenTool.WoodenAxe, ticks = 31)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun stoneAxePlanks() {
        breakBlock(Oak.Planks.identifier, StoneTool.StoneAxe, ticks = 16)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun ironAxePlanks() {
        breakBlock(Oak.Planks.identifier, IronTool.IronAxe, ticks = 11)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun diamondAxePlanks() {
        breakBlock(Oak.Planks.identifier, DiamondTool.DiamondAxe, ticks = 9)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun netheriteAxePlanks() {
        breakBlock(Oak.Planks.identifier, NetheriteTool.NetheriteAxe, ticks = 8)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun goldenAxePlanks() {
        breakBlock(Oak.Planks.identifier, GoldenTool.GoldenAxe, ticks = 6)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun nothingEndStone() {
        breakBlock(MinecraftBlocks.END_STONE, ticks = 302)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun axeEndStone() {
        breakBlock(MinecraftBlocks.END_STONE, WoodenTool.WoodenAxe, ticks = 302)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun woodenPickAxeEndStone() {
        breakBlock(MinecraftBlocks.END_STONE, WoodenTool.WoodenPickaxe, ticks = 46)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun goldenPickAxeEndStone() {
        breakBlock(MinecraftBlocks.END_STONE, GoldenTool.GoldenPickaxe, ticks = 9)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun nothingGoldOre() {
        breakBlock(MinecraftBlocks.GOLD_ORE, ticks = 302)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun woodenPickaxeGoldOre() {
        breakBlock(MinecraftBlocks.GOLD_ORE, WoodenTool.WoodenPickaxe, ticks = 152)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun stonePickaxeGoldOre() {
        breakBlock(MinecraftBlocks.GOLD_ORE, StoneTool.StonePickaxe, ticks = 77)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun ironPickaxeGoldOre() {
        breakBlock(MinecraftBlocks.GOLD_ORE, IronTool.IronPickaxe, ticks = 16)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun diamondPickaxeGoldOre() {
        breakBlock(MinecraftBlocks.GOLD_ORE, DiamondTool.DiamondPickaxe, ticks = 13)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun netheritePickaxeGoldOre() {
        breakBlock(MinecraftBlocks.GOLD_ORE, NetheriteTool.NetheritePickaxe, ticks = 11)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun goldenPickaxeGoldOre() {
        breakBlock(MinecraftBlocks.GOLD_ORE, GoldenTool.GoldenPickaxe, ticks = 26)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }


    fun nothingObsidian() {
        breakBlock(MinecraftBlocks.OBSIDIAN, ticks = 5002)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun ironPickaxeObsidian() {
        breakBlock(MinecraftBlocks.OBSIDIAN, IronTool.IronPickaxe, ticks = 835)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun diamondPickaxeObsidian() {
        breakBlock(MinecraftBlocks.OBSIDIAN, DiamondTool.DiamondPickaxe, ticks = 189)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun nothingBed() {
        breakBlock(MinecraftBlocks.RED_BED, ticks = 7)!!.apply { todo(); assert(productivity = BlockBreakProductivity.EFFECTIVE) }
    }

    fun nothingCobweb() {
        breakBlock(CobwebBlock.identifier, ticks = 402)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun swordCobweb() {
        breakBlock(CobwebBlock.identifier, WoodenTool.WoodenSword, ticks = 9)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun shearsCobweb() {
        breakBlock(CobwebBlock.identifier, ShearsItem, ticks = 9)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun swordBamboo() {
        todo()
        breakBlock(MinecraftBlocks.BAMBOO, WoodenTool.WoodenSword, ticks = 2)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun swordMelon() {
        todo()
        breakBlock(MinecraftBlocks.MELON, WoodenTool.WoodenSword, ticks = 21)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun nothingBedrock() {
        breakBlock(MinecraftBlocks.BEDROCK, ticks = -1)!!.assert(productivity = BlockBreakProductivity.USELESS)
    }

    fun creativeBedrock() {
        assertNull(breakBlock(MinecraftBlocks.BEDROCK, gamemode = Gamemodes.CREATIVE, ticks = 0))
    }

    fun nothingBarrier() {
        breakBlock(MinecraftBlocks.BARRIER, ticks = -1)!!.assert(productivity = BlockBreakProductivity.USELESS)
    }

    fun haste1() {
        breakBlock(StoneBlock.Block.identifier, WoodenTool.WoodenPickaxe, haste = 1, ticks = 18)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun haste1BadTool() {
        breakBlock(StoneBlock.Block.identifier, haste = 1, ticks = 109)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun haste2() {
        breakBlock(StoneBlock.Block.identifier, WoodenTool.WoodenPickaxe, haste = 2, ticks = 16)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun haste3() {
        breakBlock(StoneBlock.Block.identifier, WoodenTool.WoodenPickaxe, haste = 3, ticks = 14)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun efficiency1() {
        breakBlock(StoneBlock.Block.identifier, WoodenTool.WoodenPickaxe, efficiency = 1, ticks = 13)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun efficiency2() {
        breakBlock(StoneBlock.Block.identifier, GoldenTool.GoldenPickaxe, efficiency = 2, ticks = 4)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun efficiency3() {
        breakBlock(MinecraftBlocks.OBSIDIAN, GoldenTool.GoldenPickaxe, efficiency = 3, ticks = 229)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun efficiency3Obby() {
        breakBlock(MinecraftBlocks.OBSIDIAN, DiamondTool.DiamondPickaxe, efficiency = 3, ticks = 85)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun air() {
        breakBlock(StoneBlock.Block.identifier, onGround = false, ticks = 751)!!.assert(productivity = BlockBreakProductivity.INEFFECTIVE)
    }

    fun air2() {
        breakBlock(StoneBlock.Block.identifier, DiamondTool.DiamondPickaxe, onGround = false, ticks = 30)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun water() {
        todo()
        breakBlock(StoneBlock.Block.identifier, DiamondTool.DiamondPickaxe, inWater = true, ticks = Int.MAX_VALUE)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE) // TODO
    }

    fun waterAquaAffinity() {
        todo()
        breakBlock(StoneBlock.Block.identifier, DiamondTool.DiamondPickaxe, inWater = true, aquaAffinity = 1, ticks = Int.MAX_VALUE)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE) // TODO
    }

    fun waterAir() {
        todo()
        breakBlock(StoneBlock.Block.identifier, DiamondTool.DiamondPickaxe, inWater = true, onGround = false, ticks = Int.MAX_VALUE)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE) // TODO
    }


    fun miningFatigue1() {
        breakBlock(StoneBlock.Block.identifier, GoldenTool.GoldenPickaxe, miningFatigue = 1, ticks = 43)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun miningFatigue2() {
        breakBlock(StoneBlock.Block.identifier, GoldenTool.GoldenPickaxe, miningFatigue = 2, ticks = 1390)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun miningFatigue3() {
        breakBlock(StoneBlock.Block.identifier, GoldenTool.GoldenPickaxe, miningFatigue = 3, ticks = 4631)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun miningFatigue4() {
        breakBlock(StoneBlock.Block.identifier, GoldenTool.GoldenPickaxe, miningFatigue = 4, ticks = 4631)!!.assert(productivity = BlockBreakProductivity.EFFECTIVE)
    }

    fun sapling() {
        assertNull(breakBlock(MinecraftBlocks.OAK_SAPLING, ticks = 0))
    }

    fun stonk() { // hypixel skyblock
        assertNull(breakBlock(StoneBlock.Block.identifier, GoldenTool.GoldenPickaxe, efficiency = 6, haste = 2, ticks = 0))
    }

    fun packetCancel() {
        val session = createSession(1)
        val player = createPlayer(session)
        player.physics.onGround = true
        createTarget(session, MinecraftBlocks.COCOA, 1.0)

        val handler = BreakHandler(session.camera.interactions)
        handler::executor.forceSet(TestExecutor(handler))

        handler.unsafePress()

        session.assertPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.START_DIGGING, BlockPosition(1, 2, 3), Directions.UP, 0))
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        // vanilla sends 2: session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()

        handler.tick()
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()

        handler.unsafeRelease()
        session.assertPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.CANCELLED_DIGGING, BlockPosition(1, 2, 3), Directions.DOWN, 0))
        session.assertNoPacket()
    }

    fun packetSuccess() {
        val session = createSession(1)
        val player = createPlayer(session)
        player.physics.onGround = true
        createTarget(session, MinecraftBlocks.CANDLE, 1.0)

        val handler = BreakHandler(session.camera.interactions)
        handler::executor.forceSet(TestExecutor(handler))

        handler.unsafePress()

        session.assertPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.START_DIGGING, BlockPosition(1, 2, 3), Directions.UP, 0))
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        // vanilla sends 2: session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()

        handler.tick()
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()

        handler.tick()
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()

        handler.tick()
        session.assertPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.FINISHED_DIGGING, BlockPosition(1, 2, 3), Directions.UP, 0))
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
    }

    fun packetInstantBreak() {
        val session = createSession(1)
        val player = createPlayer(session)
        player.physics.onGround = true
        createTarget(session, MinecraftBlocks.OAK_SAPLING, 1.0)

        val handler = BreakHandler(session.camera.interactions)
        handler::executor.forceSet(TestExecutor(handler))

        handler.unsafePress()
        session.assertPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.START_DIGGING, BlockPosition(1, 2, 3), Directions.UP, sequence = 0))
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
    }

    fun packetCreative() {
        val session = createSession(1)
        val player = createPlayer(session)
        player.additional.gamemode = Gamemodes.CREATIVE
        createTarget(session, MinecraftBlocks.BEDROCK, 1.0)

        val handler = BreakHandler(session.camera.interactions)
        handler::executor.forceSet(TestExecutor(handler))

        handler.unsafePress()
        session.assertPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.START_DIGGING, BlockPosition(1, 2, 3), Directions.UP, sequence = 0))
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
    }

    // TODO: multiple instant break
    // TODO: creative: sword, trident, cooldown
    // TODO: adventure canPlaceAt

    // TODO: creative packet: when holding, every tick a swing arm is sent

    // TODO: instant break cooldown
    // TODO: tags
    // TODO: change: position, state, slot, item in hand
    // TODO: reach distance / get out of reach
    // TODO: change of effects (first x ticks onGround then in air)
    // TODO: acknowledgements
    // TODO: just unpress key
    // TODO: item using, ...
    // TODO: offhand swinging?

    companion object {

        fun createTarget(session: PlaySession, block: ResourceLocation, distance: Double): BlockState {
            val state = session.registries.block[block]!!.states.default
            session.world[BlockPosition(1, 2, 3)] = state

            val target = BlockTarget(Vec3d(1.0, 2.0, 3.0), distance, Directions.UP, state, null, BlockPosition(1, 2, 3), false)
            session.camera.target::target.forceSet(DataObserver(target))

            return state
        }
    }
}
