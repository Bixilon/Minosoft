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
import de.bixilon.minosoft.data.entities.objects.*;

public enum Objects {
    BOAT(1, Boat.class),
    ITEM_STACK(2, ItemStack.class),
    AREA_EFFECT_CLOUD(3, AreaEffectCloud.class),
    MINECART(10, Minecart.class),
    PRIMED_TNT(50, PrimedTNT.class),
    ENDER_CRYSTAL(51, EnderCrystal.class),
    ARROW(60, Arrow.class), // ToDo: Tipped Arrows
    SNOWBALL(61, Snowball.class),
    EGG(62, ThrownEgg.class),
    FIREBALL(63, Fireball.class),
    FIRE_CHARGE(64, FireCharge.class),
    ENDER_PEARL(65, ThrownEnderpearl.class),
    WITHER_SKULL(66, WitherSkull.class),
    SHULKER_BULLET(67, ShulkerBullet.class),
    LLAMA_SPIT(67, LlamaSpit.class),
    FALLING_BLOCK(70, FallingBlock.class),
    ITEM_FRAME(71, ItemFrame.class),
    EYE_OF_ENDER(72, EyeOfEnder.class),
    THROWN_POTION(73, ThrownPotion.class),
    FALLING_DRAGON_EGG(74, FallingDragonEgg.class),
    THROWN_EXP_BOTTLE(75, ThrownExperienceBottle.class),
    FIREWORK(76, Firework.class),
    LEASH_KNOT(77, LeashKnot.class),
    ARMOR_STAND(78, ArmorStand.class),
    EVOCATION_FANGS(78, EvocationFangs.class),
    FISHING_FLOAT(90, FishingFloat.class),
    SPECTRAL_ARROW(91, SpectralArrow.class),
    DRAGON_FIREBALL(93, DragonFireball.class),
    TRIDENT(94, Trident.class);

    final static HashBiMap<Integer, Objects> objects = HashBiMap.create();

    static {
        for (Objects object : values()) {
            objects.put(object.getId(), object);
        }
    }

    final int id;
    final Class<? extends EntityObject> clazz;

    Objects(int id, Class<? extends EntityObject> clazz) {
        this.id = id;
        this.clazz = clazz;
    }

    public static Objects byId(int id) {
        return objects.get(id);
    }

    public int getId() {
        return id;
    }

    public Class<? extends EntityObject> getClazz() {
        return clazz;
    }
}
