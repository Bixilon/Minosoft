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

package de.bixilon.minosoft.data.entities;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.entities.entities.AreaEffectCloud;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.entities.entities.boss.enderdragon.EndCrystal;
import de.bixilon.minosoft.data.entities.entities.decoration.ArmorStand;
import de.bixilon.minosoft.data.entities.entities.decoration.ItemFrame;
import de.bixilon.minosoft.data.entities.entities.decoration.LeashFenceKnotEntity;
import de.bixilon.minosoft.data.entities.entities.item.FallingBlock;
import de.bixilon.minosoft.data.entities.entities.item.ItemEntity;
import de.bixilon.minosoft.data.entities.entities.item.PrimedTNT;
import de.bixilon.minosoft.data.entities.entities.projectile.*;
import de.bixilon.minosoft.data.entities.entities.vehicle.Boat;
import de.bixilon.minosoft.data.entities.entities.vehicle.Minecart;

public enum Objects {
    BOAT(1, Boat.class),
    ITEM_STACK(2, ItemEntity.class),
    AREA_EFFECT_CLOUD(3, AreaEffectCloud.class),
    MINECART(10, Minecart.class),
    PRIMED_TNT(50, PrimedTNT.class),
    ENDER_CRYSTAL(51, EndCrystal.class),
    ARROW(60, Arrow.class), // ToDo: Tipped Arrows
    SNOWBALL(61, ThrownSnowball.class),
    EGG(62, ThrownEgg.class),
    FIREBALL(63, LargeFireball.class),
    FIRE_CHARGE(64, SmallFireball.class),
    ENDER_PEARL(65, ThrownEnderPearl.class),
    WITHER_SKULL(66, WitherSkull.class),
    SHULKER_BULLET(67, ShulkerBullet.class),
    LLAMA_SPIT(67, LlamaSpit.class),
    FALLING_BLOCK(70, FallingBlock.class),
    ITEM_FRAME(71, ItemFrame.class),
    EYE_OF_ENDER(72, ThrownEyeOfEnder.class),
    THROWN_POTION(73, ThrownPotion.class),
    // FALLING_DRAGON_EGG(74, FallingDragonEgg.class),
    THROWN_EXP_BOTTLE(75, ThrownExperienceBottle.class),
    FIREWORK(76, FireworkRocketEntity.class),
    LEASH_KNOT(77, LeashFenceKnotEntity.class),
    ARMOR_STAND(78, ArmorStand.class),
    EVOKER_FANGS(78, EvokerFangs.class),
    FISHING_HOOK(90, FishingHook.class),
    SPECTRAL_ARROW(91, SpectralArrow.class),
    DRAGON_FIREBALL(93, DragonFireball.class),
    TRIDENT(94, ThrownTrident.class);

    private static final HashBiMap<Integer, Objects> ID_OBJECT_MAP = HashBiMap.create();

    static {
        for (Objects object : values()) {
            ID_OBJECT_MAP.put(object.getId(), object);
        }
    }

    private final int id;
    private final Class<? extends Entity> clazz;

    Objects(int id, Class<? extends Entity> clazz) {
        this.id = id;
        this.clazz = clazz;
    }

    public static Objects byId(int id) {
        return ID_OBJECT_MAP.get(id);
    }

    public int getId() {
        return this.id;
    }

    public Class<? extends Entity> getClazz() {
        return this.clazz;
    }
}
