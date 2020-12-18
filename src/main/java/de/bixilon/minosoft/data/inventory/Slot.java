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

package de.bixilon.minosoft.data.inventory;

import de.bixilon.minosoft.data.locale.minecraft.MinecraftLocaleManager;
import de.bixilon.minosoft.data.mappings.Enchantment;
import de.bixilon.minosoft.data.mappings.Item;
import de.bixilon.minosoft.data.mappings.versions.VersionMapping;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.nbt.tag.*;
import org.checkerframework.common.value.qual.IntRange;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;

public class Slot {
    final Item item;
    final HashMap<Enchantment, Integer> enchantments = new HashMap<>();
    final ArrayList<ChatComponent> lore = new ArrayList<>();
    int itemCount;
    short itemMetadata;
    int repairCost;
    int durability;
    ChatComponent customDisplayName;
    boolean unbreakable;
    String skullOwner;
    byte hideFlags;

    public Slot(VersionMapping mapping, Item item, int itemCount, CompoundTag nbt) {
        this.item = item;
        this.itemCount = itemCount;
        setNBT(mapping, nbt);
    }

    public Slot(VersionMapping mapping, Item item, byte itemCount, short itemMetadata, CompoundTag nbt) {
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

    private void setNBT(VersionMapping mapping, CompoundTag nbt) {
        if (nbt == null) {
            return;
        }
        if (nbt.containsKey("RepairCost")) {
            this.repairCost = nbt.getIntTag("RepairCost").getValue();
        }
        if (nbt.containsKey("display")) {
            CompoundTag display = nbt.getCompoundTag("display");
            if (display.containsKey("Name")) {
                this.customDisplayName = ChatComponent.valueOf(display.getStringTag("Name").getValue());
            }
            if (display.containsKey("Lore")) {
                for (StringTag lore : display.getListTag("Lore").<StringTag>getValue()) {
                    this.lore.add(ChatComponent.valueOf(lore.getValue()));
                }
            }
        }
        if (nbt.containsKey("unbreakable")) {
            this.unbreakable = nbt.getByteTag("unbreakable").getValue() == 0x01;
        }
        if (nbt.containsKey("SkullOwner")) {
            // this.skullOwner = nbt.getStringTag("SkullOwner").getValue(); // ToDo
        }
        if (nbt.containsKey("HideFlags")) {
            this.hideFlags = (byte) nbt.getIntTag("HideFlags").getValue();
        }
        if (nbt.containsKey("Enchantments")) {
            for (CompoundTag enchantment : nbt.getListTag("Enchantments").<CompoundTag>getValue()) {
                String[] spilittedIdentifier = enchantment.getStringTag("id").getValue().split(":");
                this.enchantments.put(new Enchantment(spilittedIdentifier[0], spilittedIdentifier[1]), enchantment.getNumberTag("lvl").getAsInt());
            }
        } else if (nbt.containsKey("ench")) {
            for (CompoundTag enchantment : nbt.getListTag("ench").<CompoundTag>getValue()) {
                this.enchantments.put(mapping.getEnchantmentById(enchantment.getNumberTag("id").getAsInt()), enchantment.getNumberTag("lvl").getAsInt());
            }
        }
    }

    public CompoundTag getNbt(VersionMapping mapping) {
        CompoundTag nbt = new CompoundTag();

        if (this.repairCost != 0) {
            nbt.writeTag("RepairCost", new IntTag(this.repairCost));
        }
        CompoundTag display = new CompoundTag();
        if (this.customDisplayName != null) {
            display.writeTag("Name", new StringTag(this.customDisplayName.getLegacyText()));
        }
        if (!this.lore.isEmpty()) {
            display.writeTag("Lore", new ListTag(TagTypes.STRING, this.lore.stream().map(ChatComponent::getLegacyText).map(StringTag::new).toArray(StringTag[]::new)));
        }
        if (display.size() > 0) {
            nbt.writeTag("display", display);
        }
        if (this.unbreakable) {
            nbt.writeTag("unbreakable", new ByteTag(true));
        }
        if (this.skullOwner != null) {
            // nbt.writeTag("SkullOwner", new StringTag(skullOwner)); // ToDo
        }
        if (this.hideFlags != 0) {
            nbt.writeTag("HideFlags", new IntTag(this.hideFlags));
        }

        if (!this.enchantments.isEmpty()) {
            if (mapping.getVersion().isFlattened()) {
                ListTag enchantmentList = new ListTag(TagTypes.COMPOUND, new ArrayList<>());
                this.enchantments.forEach((id, level) -> {
                    CompoundTag tag = new CompoundTag();
                    tag.writeTag("id", new StringTag(id.toString()));
                    tag.writeTag("lvl", new ShortTag(level.shortValue()));
                    enchantmentList.getValue().add(tag);
                });
                nbt.writeTag("Enchantments", enchantmentList);
            } else {
                ListTag enchantmentList = new ListTag(TagTypes.COMPOUND, new ArrayList<>());
                this.enchantments.forEach((id, level) -> {
                    CompoundTag tag = new CompoundTag();
                    tag.writeTag("id", new ShortTag((short) (int) mapping.getIdByEnchantment(id)));
                    tag.writeTag("lvl", new ShortTag(level.shortValue()));
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
        return this.item;
    }

    @IntRange(from = 0, to = ProtocolDefinition.ITEM_STACK_MAX_SIZE)
    public int getItemCount() {
        return this.itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public short getItemMetadata() {
        return this.itemMetadata;
    }

    public void setItemMetadata(short itemMetadata) {
        this.itemMetadata = itemMetadata;
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
        return (this.item == null ? "AIR" : getLanguageName());
    }

    public String getLanguageName() {
        // ToDo: What if an item identifier changed between versions? oOo
        String[] keys = {String.format("item.%s.%s", this.item.getMod(), this.item.getIdentifier()), String.format("block.%s.%s", this.item.getMod(), this.item.getIdentifier())};
        for (String key : keys) {
            if (MinecraftLocaleManager.getLanguage().canTranslate(key)) {
                return MinecraftLocaleManager.translate(key);
            }
        }
        return this.item.getFullIdentifier();
    }

    @Nullable
    public ChatComponent getCustomDisplayName() {
        return this.customDisplayName;
    }

    public void setCustomDisplayName(ChatComponent customDisplayName) {
        this.customDisplayName = customDisplayName;
    }

    public int getRepairCost() {
        return this.repairCost;
    }

    public void setRepairCost(int repairCost) {
        this.repairCost = repairCost;
    }

    public int getDurability() {
        return this.durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public boolean isUnbreakable() {
        return this.unbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public boolean shouldHideEnchantments() {
        return BitByte.isBitSet(this.hideFlags, 0);
    }

    public boolean shouldHideModifiers() {
        return BitByte.isBitSet(this.hideFlags, 1);
    }

    public boolean shouldHideUnbreakable() {
        return BitByte.isBitSet(this.hideFlags, 2);
    }

    public boolean shouldHideCanDestroy() {
        return BitByte.isBitSet(this.hideFlags, 3);
    }

    public boolean shouldHideCanPlaceOn() {
        return BitByte.isBitSet(this.hideFlags, 4);
    }

    /**
     * @return hides other information, including potion effects, shield pattern info, "StoredEnchantments", written book "generation" and "author", "Explosion", "Fireworks", and map tooltips
     */
    public boolean shouldHideOtherInformation() {
        return BitByte.isBitSet(this.hideFlags, 5);
    }

    public boolean shouldHideLeatherDyeColor() {
        return BitByte.isBitSet(this.hideFlags, 6);
    }

    public HashMap<Enchantment, Integer> getEnchantments() {
        return this.enchantments;
    }

    public String getSkullOwner() {
        if (!this.item.getMod().equals(ProtocolDefinition.DEFAULT_MOD) || !this.item.getIdentifier().equals("skull")) {
            throw new IllegalArgumentException("Item is not a skull!");
        }
        return this.skullOwner;
    }

    public void setSkullOwner(String skullOwner) {
        this.skullOwner = skullOwner;
    }

    public ArrayList<ChatComponent> getLore() {
        return this.lore;
    }

    public void setShouldHideEnchantments(boolean hideEnchantments) {
        if (hideEnchantments) {
            this.hideFlags |= 1;
        } else
            this.hideFlags &= ~(1);
    }

    public void setShouldHideModifiers(boolean hideModifiers) {
        if (hideModifiers) {
            this.hideFlags |= 1 << 1;
        } else {
            this.hideFlags &= ~(1 << 1);
        }
    }

    public void setShouldHideUnbreakable(boolean hideUnbreakable) {
        if (hideUnbreakable) {
            this.hideFlags |= 1 << 2;
        } else {
            this.hideFlags &= ~(1 << 2);
        }
    }

    public void setShouldHideCanDestroy(boolean hideCanDestroy) {
        if (hideCanDestroy) {
            this.hideFlags |= 1 << 3;
        } else {
            this.hideFlags &= ~(1 << 3);
        }
    }

    public void setShouldHideCanPlaceOn(boolean hideCanPlaceOn) {
        if (hideCanPlaceOn) {
            this.hideFlags |= 1 << 4;
        } else {
            this.hideFlags &= ~(1 << 4);
        }
    }

    /**
     * @ hides other information, including potion effects, shield pattern info, "StoredEnchantments", written book "generation" and "author", "Explosion", "Fireworks", and map tooltips
     */
    public void setShouldHideOtherInformation(boolean hideOtherInformation) {
        if (hideOtherInformation) {
            this.hideFlags |= 1 << 5;
        } else {
            this.hideFlags &= ~(1 << 5);
        }
    }

    public void setShouldHideLeatherDyeColor(boolean hideLeatherDyeColor) {
        if (hideLeatherDyeColor) {
            this.hideFlags |= 1 << 6;
        } else {
            this.hideFlags &= ~(1 << 6);
        }
    }
}
