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

package de.bixilon.minosoft.game.datatypes.entities;

import de.bixilon.minosoft.game.datatypes.Identifier;
import de.bixilon.minosoft.game.datatypes.entities.objects.*;

public enum Objects implements EntityEnumInterface {
    BOAT(new Identifier("boat"), 1, Boat.class),
    ITEM_STACK(null, 2, ItemStack.class),
    MINECART(new Identifier("minecart"), 10, Minecart.class),
    PRIMED_TNT(new Identifier("tnt"), 50, PrimedTNT.class),
    ENDER_CRYSTAL(new Identifier("ender_crystal"), 51, EnderCrystal.class),
    ARROW(new Identifier("arrow"), 60, Arrow.class),
    SNOWBALL(new Identifier("snowball"), 61, Snowball.class),
    EGG(new Identifier("egg"), 62, Egg.class),
    FIRE_BALL(new Identifier("fire_ball"), 63, FireBall.class),
    FIRE_CHARGE(new Identifier("fire_charge"), 64, FireCharge.class),
    ENDER_PEARL(new Identifier("ender_pearl"), 65, Enderpearl.class),
    WITHER_SKULL(new Identifier("wither_skull"), 66, WitherSkull.class),
    FALLING_BLOCK(new Identifier("falling_block"), 70, FallingBlock.class),
    ITEM_FRAME(new Identifier("item_frame"), 71, ItemFrame.class),
    EYE_OF_ENDER(new Identifier("eye_of_ender"), 72, EyeOfEnder.class),
    THROWN_POTION(new Identifier("thrown_potion"), 73, ThrownPotion.class),
    FALLING_DRAGON_EGG(new Identifier("falling_dragon_eg"), 74, FallingDragonEgg.class),
    THROWN_EXP_BOTTLE(null, 75, ThrownExpBottle.class),
    FIREWORK(new Identifier("firework"), 76, Firework.class),
    LEASH_KNOT(new Identifier("firework"), 77, LeashKnot.class),
    FISHING_FLOAT(null, 90, FishingFloat.class);
    //ToDo: identifier

    final Identifier identifier;
    final int type;
    final Class<? extends EntityObject> clazz;

    Objects(Identifier identifier, int type, Class<? extends EntityObject> clazz) {
        this.identifier = identifier;
        this.type = type;
        this.clazz = clazz;
    }

    public static Objects byIdentifier(Identifier identifier) {
        for (Objects b : values()) {
            if (b.getIdentifier().equals(identifier)) {
                return b;
            }
        }
        return null;
    }

    public static Objects byType(int type) {
        for (Objects b : values()) {
            if (b.getType() == type) {
                return b;
            }
        }
        return null;
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public Class<? extends EntityObject> getClazz() {
        return clazz;
    }
}
