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

package de.bixilon.minosoft.game.datatypes.inventory;

import de.bixilon.minosoft.game.datatypes.objectLoader.CustomMapping;
import de.bixilon.minosoft.game.datatypes.objectLoader.enchantments.Enchantment;
import de.bixilon.minosoft.game.datatypes.objectLoader.items.Item;
import de.bixilon.minosoft.game.datatypes.text.ChatComponent;
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.nbt.tag.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;

public class Slot {
    final Item item;
    final HashMap<Enchantment, Short> enchantments = new HashMap<>();
    int itemCount;
    short itemMetadata;
    int repairCost;
    int durability;
    ChatComponent customDisplayName;
    boolean unbreakable;
    String skullOwner;
    byte hideFlags;
    final ArrayList<ChatComponent> lore = new ArrayList<>();

    public Slot(CustomMapping mapping, Item item, int itemCount, CompoundTag nbt) {
        this.item = item;
        this.itemCount = itemCount;
        setNBT(mapping, nbt);
    }

    private void setNBT(CustomMapping mapping, CompoundTag nbt) {
        if (nbt == null) {
            return;
        }
        if (nbt.containsKey("RepairCost")) {
            this.repairCost = nbt.getIntTag("RepairCost").getValue();
        }
        if (nbt.containsKey("display")) {
            CompoundTag display = nbt.getCompoundTag("display");
            if (display.containsKey("Name")) {
                this.customDisplayName = ChatComponent.fromString(display.getStringTag("Name").getValue());
            }
            if (display.containsKey("Lore")) {
                for (StringTag lore : display.getListTag("Lore").<StringTag>getValue()) {
                    this.lore.add(ChatComponent.fromString(lore.getValue()));
                }
            }
        }
        if (nbt.containsKey("unbreakable")) {
            this.unbreakable = nbt.getByteTag("unbreakable").getValue() == 0x01;
        }
        if (nbt.containsKey("SkullOwner")) {
            //this.skullOwner = nbt.getStringTag("SkullOwner").getValue(); // ToDo
        }
        if (nbt.containsKey("HideFlags")) {
            this.hideFlags = (byte) nbt.getIntTag("HideFlags").getValue();
        }
        if (nbt.containsKey("Enchantments")) {
            for (CompoundTag enchantment : nbt.getListTag("Enchantments").<CompoundTag>getValue()) {
                String[] spilittedIdentifier = enchantment.getStringTag("id").getValue().split(":");
                enchantments.put(new Enchantment(spilittedIdentifier[0], spilittedIdentifier[1]), enchantment.getShortTag("lvl").getValue());
            }
        } else if (nbt.containsKey("ench")) {
            for (CompoundTag enchantment : nbt.getListTag("ench").<CompoundTag>getValue()) {
                enchantments.put(mapping.getEnchantmentById(enchantment.getShortTag("id").getValue()), enchantment.getShortTag("lvl").getValue());
            }
        }
    }

    public Slot(CustomMapping mapping, Item item, byte itemCount, short itemMetadata, CompoundTag nbt) {
        this.item = item;
        this.itemMetadata = itemMetadata;
        this.itemCount = itemCount;
        setNBT(mapping, nbt);
    }

    public Slot(Item item) {
        this.item = item;
    }

    public Slot(Item item, byte itemCount) {
        this.item = item;
        this.itemCount = itemCount;
    }

    public CompoundTag getNbt(CustomMapping mapping) {
        CompoundTag nbt = new CompoundTag();

        if (repairCost != 0) {
            nbt.writeTag("RepairCost", new IntTag(repairCost));
        }
        CompoundTag display = new CompoundTag();
        if (customDisplayName != null) {
            display.writeTag("Name", new StringTag(customDisplayName.getLegacyText()));
        }
        if (lore.size() > 0) {
            display.writeTag("Lore", new ListTag(TagTypes.STRING, lore.stream().map(ChatComponent::getLegacyText).map(StringTag::new).toArray(StringTag[]::new)));
        }
        if (display.size() > 0) {
            nbt.writeTag("display", display);
        }
        if (unbreakable) {
            nbt.writeTag("unbreakable", new ByteTag(true));
        }
        if (skullOwner != null) {
            //nbt.writeTag("SkullOwner", new StringTag(skullOwner)); // ToDo
        }
        if (hideFlags != 0) {
            nbt.writeTag("HideFlags", new IntTag(hideFlags));
        }
        if (enchantments.size() > 0) {
            if (mapping.getVersion().isFlattened()) {
                ListTag enchantmentList = new ListTag(TagTypes.COMPOUND, new ArrayList<>());
                enchantments.forEach((id, level) -> {
                    CompoundTag tag = new CompoundTag();
                    tag.writeTag("id", new StringTag(id.toString()));
                    tag.writeTag("lvl", new ShortTag(level));
                    enchantmentList.getValue().add(tag);
                });
                nbt.writeTag("Enchantments", enchantmentList);
            } else {
                ListTag enchantmentList = new ListTag(TagTypes.COMPOUND, new ArrayList<>());
                enchantments.forEach((id, level) -> {
                    CompoundTag tag = new CompoundTag();
                    tag.writeTag("id", new ShortTag((short) mapping.getIdByEnchantment(id)));
                    tag.writeTag("lvl", new ShortTag(level));
                    enchantmentList.getValue().add(tag);
                });
                nbt.writeTag("ench", enchantmentList);
            }
        }
        return nbt;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        Slot their = (Slot) obj;

        // ToDo: check nbt

        return their.getItem().equals(getItem()) && their.getItemCount() == getItemCount() && their.getItemMetadata() == getItemMetadata();
    }

    public Item getItem() {
        return item;
    }

    public int getItemCount() {
        return itemCount;
    }

    public short getItemMetadata() {
        return itemMetadata;
    }

    public void setItemMetadata(short itemMetadata) {
        this.itemMetadata = itemMetadata;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    public String getDisplayName() {
        ChatComponent customName = getCustomDisplayName();
        if (customName != null) {
            return customName.getANSIColoredMessage();
        }
        return (item == null ? "AIR" : item.toString()); // ToDo display name per Item (from language file)
    }

    @Nullable
    public ChatComponent getCustomDisplayName() {
        return customDisplayName;
    }

    public void setCustomDisplayName(ChatComponent customDisplayName) {
        this.customDisplayName = customDisplayName;
    }

    public int getRepairCost() {
        return repairCost;
    }

    public void setRepairCost(int repairCost) {
        this.repairCost = repairCost;
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public boolean shouldHideEnchantments() {
        return BitByte.isBitSet(hideFlags, 0);
    }

    public boolean shouldHideModifiers() {
        return BitByte.isBitSet(hideFlags, 1);
    }

    public boolean shouldHideUnbreakable() {
        return BitByte.isBitSet(hideFlags, 2);
    }

    public boolean shouldHideCanDestroy() {
        return BitByte.isBitSet(hideFlags, 3);
    }

    public boolean shouldHideCanPlaceOn() {
        return BitByte.isBitSet(hideFlags, 4);
    }

    /**
     * @return hides other information, including potion effects, shield pattern info, "StoredEnchantments", written book "generation" and "author", "Explosion", "Fireworks", and map tooltips
     */
    public boolean shouldHideOtherInformation() {
        return BitByte.isBitSet(hideFlags, 5);
    }

    public boolean shouldHideLeatherDyeColor() {
        return BitByte.isBitSet(hideFlags, 6);
    }

    public HashMap<Enchantment, Short> getEnchantments() {
        return enchantments;
    }

    public String getSkullOwner() {
        if (!item.getMod().equals("minecraft") || !item.getIdentifier().equals("skull")) {
            throw new IllegalArgumentException("Item is not a skull!");
        }
        return skullOwner;
    }

    public void setSkullOwner(String skullOwner) {
        this.skullOwner = skullOwner;
    }

    public ArrayList<ChatComponent> getLore() {
        return lore;
    }

    public void setShouldHideEnchantments(boolean hideEnchantments) {
        if (hideEnchantments) {
            hideFlags |= 1;
        } else
            hideFlags &= ~(1);
    }

    public void setShouldHideModifiers(boolean hideModifiers) {
        if (hideModifiers) {
            hideFlags |= 1 << 1;
        } else {
            hideFlags &= ~(1 << 1);
        }
    }

    public void setShouldHideUnbreakable(boolean hideUnbreakable) {
        if (hideUnbreakable) {
            hideFlags |= 1 << 2;
        } else {
            hideFlags &= ~(1 << 2);
        }
    }

    public void setShouldHideCanDestroy(boolean hideCanDestroy) {
        if (hideCanDestroy) {
            hideFlags |= 1 << 3;
        } else {
            hideFlags &= ~(1 << 3);
        }
    }

    public void setShouldHideCanPlaceOn(boolean hideCanPlaceOn) {
        if (hideCanPlaceOn) {
            hideFlags |= 1 << 4;
        } else {
            hideFlags &= ~(1 << 4);
        }
    }

    /**
     * @ hides other information, including potion effects, shield pattern info, "StoredEnchantments", written book "generation" and "author", "Explosion", "Fireworks", and map tooltips
     */
    public void setShouldHideOtherInformation(boolean hideOtherInformation) {
        if (hideOtherInformation) {
            hideFlags |= 1 << 5;
        } else {
            hideFlags &= ~(1 << 5);
        }
    }

    public void setShouldHideLeatherDyeColor(boolean hideLeatherDyeColor) {
        if (hideLeatherDyeColor) {
            hideFlags |= 1 << 6;
        } else {
            hideFlags &= ~(1 << 6);
        }
    }
}
