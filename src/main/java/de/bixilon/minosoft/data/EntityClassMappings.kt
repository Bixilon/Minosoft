/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.data.entities.EvokerFangs
import de.bixilon.minosoft.data.entities.entities.AreaEffectCloud
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.ExperienceOrb
import de.bixilon.minosoft.data.entities.entities.LightningBolt
import de.bixilon.minosoft.data.entities.entities.ambient.Bat
import de.bixilon.minosoft.data.entities.entities.animal.*
import de.bixilon.minosoft.data.entities.entities.animal.hoglin.Hoglin
import de.bixilon.minosoft.data.entities.entities.animal.horse.*
import de.bixilon.minosoft.data.entities.entities.animal.water.*
import de.bixilon.minosoft.data.entities.entities.boss.enderdragon.EndCrystal
import de.bixilon.minosoft.data.entities.entities.boss.enderdragon.EnderDragon
import de.bixilon.minosoft.data.entities.entities.boss.wither.WitherBoss
import de.bixilon.minosoft.data.entities.entities.decoration.ArmorStand
import de.bixilon.minosoft.data.entities.entities.decoration.ItemFrame
import de.bixilon.minosoft.data.entities.entities.decoration.LeashFenceKnotEntity
import de.bixilon.minosoft.data.entities.entities.decoration.Painting
import de.bixilon.minosoft.data.entities.entities.item.FallingBlock
import de.bixilon.minosoft.data.entities.entities.item.ItemEntity
import de.bixilon.minosoft.data.entities.entities.item.PrimedTNT
import de.bixilon.minosoft.data.entities.entities.monster.*
import de.bixilon.minosoft.data.entities.entities.monster.piglin.Piglin
import de.bixilon.minosoft.data.entities.entities.monster.piglin.PiglinBrute
import de.bixilon.minosoft.data.entities.entities.monster.raid.*
import de.bixilon.minosoft.data.entities.entities.npc.Villager
import de.bixilon.minosoft.data.entities.entities.npc.WanderingTrader
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.projectile.*
import de.bixilon.minosoft.data.entities.entities.vehicle.*
import de.bixilon.minosoft.data.mappings.ModIdentifier

object EntityClassMappings {
    private val ENTITY_CLASS_MAPPINGS: HashBiMap<Class<out Entity>, ModIdentifier> = HashBiMap.create(
        mapOf(
            AreaEffectCloud::class.java to ModIdentifier("area_effect_cloud"),
            ArmorStand::class.java to ModIdentifier("armor_stand"),
            Arrow::class.java to ModIdentifier("arrow"),
            Axolotl::class.java to ModIdentifier("axolotl"),
            Bat::class.java to ModIdentifier("bat"),
            Bee::class.java to ModIdentifier("bee"),
            Blaze::class.java to ModIdentifier("blaze"),
            Boat::class.java to ModIdentifier("boat"),
            Cat::class.java to ModIdentifier("cat"),
            CaveSpider::class.java to ModIdentifier("cave_spider"),
            Chicken::class.java to ModIdentifier("chicken"),
            Cod::class.java to ModIdentifier("cod"),
            Cow::class.java to ModIdentifier("cow"),
            Creeper::class.java to ModIdentifier("creeper"),
            Dolphin::class.java to ModIdentifier("dolphin"),
            Donkey::class.java to ModIdentifier("donkey"),
            DragonFireball::class.java to ModIdentifier("dragon_fireball"),
            Drowned::class.java to ModIdentifier("drowned"),
            ElderGuardian::class.java to ModIdentifier("elder_guardian"),
            EndCrystal::class.java to ModIdentifier("end_crystal"),
            EnderDragon::class.java to ModIdentifier("ender_dragon"),
            Enderman::class.java to ModIdentifier("enderman"),
            Endermite::class.java to ModIdentifier("endermite"),
            Evoker::class.java to ModIdentifier("evoker"),
            EvokerFangs::class.java to ModIdentifier("evoker_fangs"),
            ExperienceOrb::class.java to ModIdentifier("experience_orb"),
            ThrownEyeOfEnder::class.java to ModIdentifier("eye_of_ender"),
            FallingBlock::class.java to ModIdentifier("falling_block"),
            FireworkRocketEntity::class.java to ModIdentifier("firework_rocket"),
            Fox::class.java to ModIdentifier("fox"),
            Ghast::class.java to ModIdentifier("ghast"),
            Giant::class.java to ModIdentifier("giant"),
            Guardian::class.java to ModIdentifier("guardian"),
            Hoglin::class.java to ModIdentifier("hoglin"),
            Horse::class.java to ModIdentifier("horse"),
            Husk::class.java to ModIdentifier("husk"),
            Illusioner::class.java to ModIdentifier("illusioner"),
            IronGolem::class.java to ModIdentifier("iron_golem"),
            ItemEntity::class.java to ModIdentifier("item"),
            ItemFrame::class.java to ModIdentifier("item_frame"),
            LargeFireball::class.java to ModIdentifier("fireball"),
            LeashFenceKnotEntity::class.java to ModIdentifier("leash_knot"),
            LightningBolt::class.java to ModIdentifier("lightning_bolt"),
            Llama::class.java to ModIdentifier("llama"),
            LlamaSpit::class.java to ModIdentifier("llama_spit"),
            MagmaCube::class.java to ModIdentifier("magma_cube"),
            Minecart::class.java to ModIdentifier("minecart"),
            MinecartChest::class.java to ModIdentifier("chest_minecart"),
            MinecartCommandBlock::class.java to ModIdentifier("command_block_minecart"),
            MinecartFurnace::class.java to ModIdentifier("furnace_minecart"),
            MinecartHopper::class.java to ModIdentifier("hopper_minecart"),
            MinecartSpawner::class.java to ModIdentifier("spawner_minecart"),
            MinecartTNT::class.java to ModIdentifier("tnt_minecart"),
            Mule::class.java to ModIdentifier("mule"),
            Mooshroom::class.java to ModIdentifier("mooshroom"),
            Ocelot::class.java to ModIdentifier("ocelot"),
            Painting::class.java to ModIdentifier("painting"),
            Panda::class.java to ModIdentifier("panda"),
            Parrot::class.java to ModIdentifier("parrot"),
            Phantom::class.java to ModIdentifier("phantom"),
            Pig::class.java to ModIdentifier("pig"),
            Piglin::class.java to ModIdentifier("piglin"),
            PiglinBrute::class.java to ModIdentifier("piglin_brute"),
            Pillager::class.java to ModIdentifier("pillager"),
            PolarBear::class.java to ModIdentifier("polar_bear"),
            PrimedTNT::class.java to ModIdentifier("tnt"),
            PufferFish::class.java to ModIdentifier("pufferfish"),
            Rabbit::class.java to ModIdentifier("rabbit"),
            Ravenger::class.java to ModIdentifier("ravager"),
            Salmon::class.java to ModIdentifier("salmon"),
            Sheep::class.java to ModIdentifier("sheep"),
            Shulker::class.java to ModIdentifier("shulker"),
            ShulkerBullet::class.java to ModIdentifier("shulker_bullet"),
            Silverfish::class.java to ModIdentifier("silverfish"),
            Skeleton::class.java to ModIdentifier("skeleton"),
            SkeletonHorse::class.java to ModIdentifier("skeleton_horse"),
            Slime::class.java to ModIdentifier("slime"),
            SmallFireball::class.java to ModIdentifier("small_fireball"),
            SnowGolem::class.java to ModIdentifier("snow_golem"),
            ThrownSnowball::class.java to ModIdentifier("snowball"),
            SpectralArrow::class.java to ModIdentifier("spectral_arrow"),
            Spider::class.java to ModIdentifier("spider"),
            Squid::class.java to ModIdentifier("squid"),
            Stray::class.java to ModIdentifier("stray"),
            Strider::class.java to ModIdentifier("strider"),
            ThrownEgg::class.java to ModIdentifier("egg"),
            ThrownEnderPearl::class.java to ModIdentifier("ender_pearl"),
            ThrownExperienceBottle::class.java to ModIdentifier("experience_bottle"),
            ThrownPotion::class.java to ModIdentifier("potion"),
            ThrownTrident::class.java to ModIdentifier("trident"),
            TraderLlama::class.java to ModIdentifier("trader_llama"),
            TropicalFish::class.java to ModIdentifier("tropical_fish"),
            Turtle::class.java to ModIdentifier("turtle"),
            Vex::class.java to ModIdentifier("vex"),
            Villager::class.java to ModIdentifier("villager"),
            Vindicator::class.java to ModIdentifier("vindicator"),
            WanderingTrader::class.java to ModIdentifier("wandering_trader"),
            Witch::class.java to ModIdentifier("witch"),
            WitherBoss::class.java to ModIdentifier("wither"),
            WitherSkeleton::class.java to ModIdentifier("wither_skeleton"),
            WitherSkull::class.java to ModIdentifier("wither_skull"),
            Wolf::class.java to ModIdentifier("wolf"),
            Zoglin::class.java to ModIdentifier("zoglin"),
            Zombie::class.java to ModIdentifier("zombie"),
            ZombieHorse::class.java to ModIdentifier("zombie_horse"),
            ZombieVillager::class.java to ModIdentifier("zombie_villager"),
            ZombiePigman::class.java to ModIdentifier("zombie_pigman"),
            ZombifiedPiglin::class.java to ModIdentifier("zombified_piglin"),
            PlayerEntity::class.java to ModIdentifier("player"),
            FishingHook::class.java to ModIdentifier("fishing_bobber"),
            GlowSquid::class.java to ModIdentifier("glow_squid")
        )
    )

    fun getByIdentifier(mod: String, identifier: String): Class<out Entity>? {
        return ENTITY_CLASS_MAPPINGS.inverse()[ModIdentifier(mod, identifier)]
    }

    fun getByIdentifier(identifier: ModIdentifier): Class<out Entity>? {
        return ENTITY_CLASS_MAPPINGS.inverse()[identifier]
    }
}
