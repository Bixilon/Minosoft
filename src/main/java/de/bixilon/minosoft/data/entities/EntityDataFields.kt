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
package de.bixilon.minosoft.data.entities

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.entities.animal.Axolotl
import de.bixilon.minosoft.data.entities.entities.decoration.armorstand.ArmorStandArmRotation
import de.bixilon.minosoft.data.entities.entities.npc.villager.data.VillagerData
import de.bixilon.minosoft.data.entities.entities.vehicle.boat.Boat
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.ChatComponent.Companion.of

enum class EntityDataFields(val defaultValue: Any? = null) {
    ENTITY_FLAGS(0.toByte()),
    ENTITY_AIR_SUPPLY(300),
    ENTITY_CUSTOM_NAME,
    ENTITY_CUSTOM_NAME_VISIBLE(false),
    ENTITY_SILENT(false),
    ENTITY_NO_GRAVITY(false),
    ENTITY_POSE(Poses.STANDING),
    ENTITY_TICKS_FROZEN(0),

    LIVING_ENTITY_FLAGS(0.toByte()),
    LIVING_ENTITY_HEALTH(Float.MIN_VALUE),
    LIVING_ENTITY_EFFECT_COLOR(0),
    LIVING_ENTITY_EFFECT_AMBIENCE(false),
    LIVING_ENTITY_ARROW_COUNT(0),
    LIVING_ENTITY_ABSORPTION_HEARTS(0),
    LIVING_ENTITY_BED_POSITION,

    MOB_FLAGS(0.toByte()),

    ZOMBIE_HANDS_HELD_UP(false),
    ZOMBIE_IS_BABY(false),
    ZOMBIE_SPECIAL_TYPE(0),
    ZOMBIE_DROWNING_CONVERSION(false),

    THROWABLE_ITEM_PROJECTILE_ITEM,
    THROWN_POTION_ITEM,

    FALLING_BLOCK_SPAWN_POSITION,

    AREA_EFFECT_CLOUD_IGNORE_RADIUS(false),
    AREA_EFFECT_CLOUD_RADIUS(0.5f),
    AREA_EFFECT_CLOUD_COLOR(0),
    AREA_EFFECT_CLOUD_WAITING(false),
    AREA_EFFECT_CLOUD_PARTICLE(ParticleData(ParticleType(ResourceLocation("effect"), mutableListOf()))),

    ABSTRACT_ARROW_FLAGS(0.toByte()),
    ABSTRACT_ARROW_PIERCE_LEVEL(0.toByte()),
    ABSTRACT_ARROW_OWNER_UUID,

    ARROW_EFFECT_COLOR(-1),

    FISHING_HOOK_HOOKED_ENTITY(0),
    FISHING_HOOK_CATCHABLE(false),

    THROWN_TRIDENT_LOYALTY_LEVEL(0),
    THROWN_TRIDENT_FOIL(false),

    BOAT_HURT(0),
    BOAT_HURT_DIRECTION(1),
    BOAT_DAMAGE_TAKEN(0.0f),
    BOAT_MATERIAL(Boat.BoatMaterials.OAK.ordinal),
    BOAT_PADDLE_LEFT(false),
    BOAT_PADDLE_RIGHT(false),
    BOAT_BUBBLE_TIME(0),

    END_CRYSTAL_BEAM_TARGET,
    END_CRYSTAL_SHOW_BOTTOM(true),

    FIREBALL_ITEM,

    WITHER_SKULL_DANGEROUS(false),

    FIREWORK_ROCKET_ENTITY_ITEM,
    FIREWORK_ROCKET_ENTITY_ATTACHED_ENTITY(0),
    FIREWORK_ROCKET_ENTITY_SHOT_AT_ANGLE(false),

    ITEM_FRAME_ITEM,
    ITEM_FRAME_ROTATION(0),

    ITEM_ITEM,

    PLAYER_ABSORPTION_HEARTS(0.0f),
    PLAYER_SCORE(0),
    PLAYER_SKIN_PARTS_FLAGS(0.toByte()),
    PLAYER_SKIN_MAIN_HAND(1.toByte()),
    PLAYER_LEFT_SHOULDER_DATA,
    PLAYER_RIGHT_SHOULDER_DATA,
    PLAYER_LAST_DEATH_POSITION,

    ARMOR_STAND_FLAGS(0.toByte()),
    ARMOR_STAND_HEAD_ROTATION(ArmorStandArmRotation(0.0f, 0.0f, 0.0f)),
    ARMOR_STAND_BODY_ROTATION(ArmorStandArmRotation(0.0f, 0.0f, 0.0f)),
    ARMOR_STAND_LEFT_ARM_ROTATION(ArmorStandArmRotation(-10.0f, 0.0f, -10.0f)),
    ARMOR_STAND_RIGHT_ARM_ROTATION(ArmorStandArmRotation(-15.0f, 0.0f, 10.0f)),
    ARMOR_STAND_LEFT_LAG_ROTATION(ArmorStandArmRotation(-1.0f, 0.0f, -1.0f)),
    ARMOR_STAND_RIGHT_LAG_ROTATION(ArmorStandArmRotation(1.0f, 0.0f, 1.0f)),

    BAT_FLAGS(0.toByte()),

    AGEABLE_IS_BABY(false),

    DOLPHIN_TREASURE_POSITION,
    DOLPHIN_HAS_FISH(false),
    DOLPHIN_MOISTNESS_LEVEL(2400),

    ABSTRACT_FISH_FROM_BUCKET(false),

    PUFFERFISH_PUFF_STATE(0),

    TROPICAL_FISH_VARIANT(0),

    ABSTRACT_HORSE_FLAGS(0.toByte()),
    ABSTRACT_HORSE_OWNER_UUID,

    HORSE_VARIANT(0),

    ABSTRACT_CHESTED_HORSE_HAS_CHEST(false),

    LLAMA_STRENGTH(0),
    LLAMA_CARPET_COLOR(-1),
    LLAMA_VARIANT(0),

    BEE_FLAGS(0.toByte()),
    BEE_REMAINING_ANGER_TIME(0),

    FOX_VARIANT(0),
    FOX_FLAGS(0.toByte()),
    FOX_TRUSTED_1,
    FOX_TRUSTED_2,

    OCELOT_IS_TRUSTING(false),

    PANDA_UNHAPPY_TIMER(0),
    PANDA_SNEEZE_TIMER(0),
    PANDA_EAT_TIMER(0),
    PANDA_MAIN_GENE(0.toByte()),
    PANDA_HIDDEN_GAME(0.toByte()),
    PANDA_FLAGS(0.toByte()),

    PIG_HAS_SADDLE(false),
    PIG_BOOST_TIME(0),

    RABBIT_VARIANT(0),

    TURTLE_HOME_POSITION,
    TURTLE_HAS_EGG(false),
    TURTLE_IS_LAYING_EGG(false),
    TURTLE_TRAVEL_POSITION,
    TURTLE_IS_GOING_HOME(false),
    TURTLE_IS_TRAVELING(false),

    POLAR_BEAR_STANDING(false),

    HOGLIN_IMMUNE_TO_ZOMBIFICATION(false),

    MOOSHROOM_VARIANT("red"),

    SHEEP_FLAGS(0.toByte()),

    STRIDER_TIME_TO_BOOST(0),
    STRIDER_IS_SUFFOCATING(false),
    STRIDER_HAS_SADDLE(false),

    TAMABLE_ENTITY_FLAGS(0.toByte()),
    TAMABLE_ENTITY_OWNER_UUID,

    CAT_VARIANT(1),
    CAT_IS_LYING(false),
    CAT_IS_RELAXED(false),
    CAT_GET_COLLAR_COLOR(14),  // RED

    WOLF_IS_BEGGING(false),
    WOLF_COLLAR_COLOR(14),
    WOLF_ANGER_TIME(0),
    WOLF_HEALTH(0.0f),

    PARROT_VARIANT(0),

    ABSTRACT_ILLAGER_FLAGS(0.toByte()),
    ABSTRACT_VILLAGER_UNHAPPY_TIMER(0),
    VILLAGER_VILLAGER_DATA(VillagerData(VillagerTypes.PLAINS, ResourceLocation("minecraft:none"), VillagerLevels.APPRENTICE)),

    IRON_GOLEM_FLAGS(0.toByte()),

    SNOW_GOLEM_FLAGS(0.toByte()),

    SHULKER_ATTACH_FACE(Directions.DOWN),
    SHULKER_ATTACHMENT_POSITION,
    SHULKER_PEEK(0.toByte()),
    SHULKER_COLOR(10.toByte()),

    ABSTRACT_PIGLIN_IMMUNE_TO_ZOMBIFICATION(false),

    PIGLIN_IS_BABY(false),
    PIGLIN_IS_CHARGING_CROSSBOW(false),
    PIGLIN_IS_DANCING(false),
    PIGLIN_IMMUNE_TO_ZOMBIFICATION(false),

    BLAZE_FLAGS(0.toByte()),

    CREEPER_STATE(-1),
    CREEPER_IS_CHARGED(false),
    CREEPER_IS_IGNITED(false),

    GUARDIAN_IS_MOVING(false),
    GUARDIAN_TARGET_ENTITY_ID(0),

    RAIDER_IS_CELEBRATING(false),

    SPELLCASTER_ILLAGER_SPELL(0),

    WITCH_IS_DRINKING_POTION(0),

    VEX_FLAGS(0.toByte()),

    SPIDER_FLAGS(0.toByte()),

    WITHER_BOSS_CENTER_HEAD_TARGET_ENTITY_ID(0),
    WITHER_BOSS_LEFT_HEAD_TARGET_ENTITY_ID(0),
    WITHER_BOSS_RIGHT_HEAD_TARGET_ENTITY_ID(0),
    WITHER_BOSS_INVULNERABLE_TIME(0),

    ZOGLIN_IS_BABY(false),

    ZOMBIE_VILLAGER_IS_CONVERTING(false),
    ZOMBIE_VILLAGER_DATA(VILLAGER_VILLAGER_DATA.defaultValue),

    ENDERMAN_CARRIED_BLOCK,
    ENDERMAN_IS_SCREAMING(false),
    ENDERMAN_IS_STARRING(false),

    ENDER_DRAGON_PHASE(10),

    GHAST_IS_ATTACKING(false),

    PHANTOM_SIZE(0),

    SLIME_SIZE(0),

    MINECART_HURT(0),
    MINECART_HURT_DIRECTION(1),
    MINECART_DAMAGE_TAKEN(0.0f),
    MINECART_BLOCK_ID(0),
    MINECART_BLOCK_Y_OFFSET(6),
    MINECART_SHOW_BLOCK(false),
    MINECART_FURNACE_HAS_FUEL(false),
    MINECART_COMMAND_BLOCK_COMMAND(""),
    MINECART_COMMAND_BLOCK_LAST_OUTPUT(of("")),

    PRIMED_TNT_FUSE_TIME(80),

    PILLAGER_IS_CHARGING_CROSSBOW(false),

    THROWN_EYE_OF_ENDER_ITEM,

    AXOLOTL_VARIANT(Axolotl.AxolotlVariants.LUCY.ordinal),

    AXOLOTL_PLAYING_DEAD(false),
    AXOLOTL_FROM_BUCKET(false),

    GLOW_SQUID_DARK_TICKS_REMAINING(0),

    SKELETON_STRAY_FREEZE_CONVERTING(false),
    SKELETON_SWING_ARMS(false),

    GOAT_IS_SCREAMING(false),


    FROG_TYPE(0),
    FROG_TARGET(null),

    WARDEN_ANGER(0),

    MOTIVE(null),


    // pretty old stuff here. 1.8 mostly (or even after, I don't know and care)
    LEGACY_SKELETON_TYPE(0.toByte()),
    LEGACY_ENDERMAN_CARRIED_BLOCK(0),
    LEGACY_ENDERMAN_CARRIED_BLOCK_DATA(0),
    LEGACY_WITCH_IS_AGGRESSIVE(false),
    LEGACY_GUARDIAN_FLAGS(0.toByte()),
    LEGACY_OCELOT_TYPE(0.toByte()),
    LEGACY_HORSE_OWNER_NAME(""),
    LEGACY_HORSE_SPECIAL_TYPE(0.toByte()),
    LEGACY_HORSE_ARMOR(0),
    LEGACY_VILLAGE_PROFESSION(0),
    LEGACY_END_CRYSTAL_HEALTH(5),
    LEGACY_LIVING_ENTITY_AI_DISABLED(0.toByte()),
    LEGACY_AGEABLE_ENTITY_AGE(0.toByte()),
    LEGACY_AGEABLE_OWNER_NAME(""),
    LEGACY_ZOMBIE_VILLAGER_TYPE(0),
    LEGACY_AREA_EFFECT_CLOUD_PARTICLE_ID(0),
    LEGACY_AREA_EFFECT_CLOUD_PARTICLE_PARAMETER_1(0),
    LEGACY_AREA_EFFECT_CLOUD_PARTICLE_PARAMETER_2(0),
    ;

    companion object : ValuesEnum<EntityDataFields> {
        override val VALUES: Array<EntityDataFields> = values()
        override val NAME_MAP: Map<String, EntityDataFields> = EnumUtil.getEnumValues(VALUES)
    }
}
