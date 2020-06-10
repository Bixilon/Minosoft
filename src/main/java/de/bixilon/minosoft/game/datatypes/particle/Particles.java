/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.game.datatypes.particle;

import de.bixilon.minosoft.game.datatypes.Identifier;

public enum Particles {
    AMBIENT_ENTITY_EFFECT(new Identifier("ambient_entity_effect", "ambiententityeffect"), 0),
    ANGRY_VILLAGER(new Identifier("angry_villager", "angryvillager"), 1),
    BARRIER(new Identifier("barrier"), 2),
    BLOCK(new Identifier("block"), 3, BlockParticle.class),
    BUBBLE(new Identifier("barrier"), 4),
    CLOUD(new Identifier("cloud"), 5),
    CRIT(new Identifier("crit"), 6),
    DAMAGE_INDICATOR(new Identifier("damageindicator", "damage_indicator"), 7),
    DRAGON_BREATH(new Identifier("dragonbreath", "dragon_breath"), 8),
    DRIPPING_LAVA(new Identifier("drippinglava", "dripping_lava"), 9),
    FALLING_LAVA(new Identifier("fallinglava", "falling_lava"), 10),
    LANDING_LAVA(new Identifier("landinglava", "landing_lava"), 11),
    DRIPPING_WATER(new Identifier("drippingwater", "dripping_water"), 12),
    FALLING_WATER(new Identifier("fallingwater", "falling_water"), 13),
    DUST(new Identifier("dust"), 14, DustParticle.class),
    EFFECT(new Identifier("effect"), 15),
    ELDER_GUARDIAN(new Identifier("elderguardian", "elder_guardian"), 16),
    ENCHANTED_HIT(new Identifier("enchantedhit", "enchanted_hit"), 17),
    ENCHANT(new Identifier("enchant"), 18),
    END_ROD(new Identifier("endrod", "end_rod"), 19),
    ENTITY_EFFECT(new Identifier("entityeffect", "entity_effect"), 20),
    EXPLOSION_EMITTER(new Identifier("explosionemitter", "explosion_emitter"), 21),
    EXPLOSION(new Identifier("explosion"), 22),
    FALLING_DUST(new Identifier("fallingdust", "falling_dust"), 14, FallingDustParticle.class),
    FIREWORK(new Identifier("firework"), 24),
    FISHING(new Identifier("fishing"), 25),
    FLAME(new Identifier("flame"), 26),
    FLASH(new Identifier("flash"), 27),
    HAPPY_VILLAGER(new Identifier("happyvillager", "happy_villager"), 28),
    COMPOSTER(new Identifier("composter"), 29),
    HEART(new Identifier("heart"), 30),
    INSTANT_EFFECT(new Identifier("instanteffect", "instant_effect"), 31),
    ITEM(new Identifier("item"), 32, ItemParticle.class),
    ITEM_SLIME(new Identifier("itemslime", "item_slime"), 33),
    ITEM_SNOWBALL(new Identifier("itemsnowball", "item_snowball"), 34),
    LARGE_SMOKE(new Identifier("largesmoke", "large_smoke"), 35),
    LAVA(new Identifier("lava"), 36),
    MYCELIUM(new Identifier("mycelium"), 37),
    NOTE(new Identifier("note"), 38),
    POOF(new Identifier("poof"), 39),
    PORTAL(new Identifier("portal"), 40),
    RAIN(new Identifier("rain"), 41),
    SMOKE(new Identifier("smoke"), 42),
    SNEEZE(new Identifier("sneeze"), 43),
    SPIT(new Identifier("spit"), 44),
    SQUID_INK(new Identifier("squidink", "squid_ink"), 45),
    SWEEP_ATTACK(new Identifier("sweepattack", "sweep_attack"), 46),
    TOTEM_OF_UNDYING(new Identifier("totemofundying", "totem_of_undying"), 47),
    UNDERWATER(new Identifier("underwater"), 48),
    SPLASH(new Identifier("splash"), 49),
    WITCH(new Identifier("witch"), 50),
    BUBBLE_POP(new Identifier(null, "bubble_pop"), 51),
    CURRENT_DOWN(new Identifier(null, "current_down"), 52),
    BUBBLE_COLUMN_UP(new Identifier(null, "bubble_column_up"), 53),
    NAUTILUS(new Identifier(null, "nautilus"), 54),
    DOLPHIN(new Identifier(null, "dolphin"), 55),
    CAMPFIRE_COSY_SMOKE(new Identifier(null, "campfire_cosy_smoke"), 56),
    CAMPFIRE_SIGNAL_SMOKE(new Identifier(null, "campfire_signal_smoke"), 57),
    DRIPPING_HONEY(new Identifier(null, "dripping_honey"), 58),
    FALLING_HONEY(new Identifier(null, "falling_honey"), 59),
    LANDING_HONEY(new Identifier(null, "landing_honey"), 60),
    FALLING_NECTAR(new Identifier(null, "falling_nectar"), 61);

    final Identifier identifier;
    final int id;
    final Class<? extends Particle> clazz;

    Particles(Identifier identifier, int id, Class<? extends Particle> clazz) {
        this.identifier = identifier;
        this.id = id;
        this.clazz = clazz;
    }

    Particles(Identifier identifier, int id) {
        this.identifier = identifier;
        this.id = id;
        this.clazz = OtherParticles.class;
    }

    public static Particles byIdentifier(Identifier identifier) {
        for (Particles b : values()) {
            if (b.getIdentifier().equals(identifier)) {
                return b;
            }
        }
        return null;
    }

    public static Particles byType(int type) {
        for (Particles b : values()) {
            if (b.getId() == type) {
                return b;
            }
        }
        return null;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public int getId() {
        return id;
    }

    public Class<? extends Particle> getClazz() {
        return clazz;
    }
}
