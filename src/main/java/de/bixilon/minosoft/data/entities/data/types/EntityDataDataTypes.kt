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

package de.bixilon.minosoft.data.entities.data.types

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.entities.data.types.registry.MotiveEntityDataType
import de.bixilon.minosoft.data.entities.data.types.registry.VariantsEntityDataType

enum class EntityDataDataTypes(val type: EntityDataType<*>) {
    BYTE(ByteEntityDataType),
    SHORT(ShortEntityDataType),
    INTEGER(IntEntityDataType),
    VAR_INT(VarIntEntityDataType),
    FLOAT(FloatEntityDataType),
    STRING(StringEntityDataType),
    TEXT_COMPONENT(ChatComponentEntityDataType),
    OPTIONAL_TEXT_COMPONENT(OptionalChatComponentEntityDataType),
    ITEM_STACK(ItemStackEntityDataType),
    BOOLEAN(BooleanEntityDataType),
    VEC3I(Vec3iEntityDataType),
    ROTATION(Vec3EntityDataType),
    OPTIONAL_VEC3I(OptionalVec3iEntityDataType),
    DIRECTION(DirectionEntityDataType),
    OPTIONAL_UUID(OptionalUUIDEntityDataType),
    BLOCK_STATE(BlockStateEntityDataType),
    OPTIONAL_BLOCK_STATE(BlockStateEntityDataType),
    NBT(NbtEntityDataType),
    PARTICLE(ParticleDataEntityDataType),
    VILLAGER_DATA(VillagerDataEntityDataType),
    POSE(PoseEntityDataType),
    OPTIONAL_INTEGER(OptionalIntEntityDataType),
    FIREWORK_DATA(OptionalIntEntityDataType),
    GLOBAL_POSITION(GlobalPositionEntityDataType),
    OPTIONAL_GLOBAL_POSITION(OptionalGlobalPositionEntityDataType),
    MOTIVE(MotiveEntityDataType),


    CAT_VARIANT(VariantsEntityDataType.CatVariantType),
    FROG_VARIANT(VariantsEntityDataType.FrogVariantType),
    ;

    companion object : ValuesEnum<EntityDataDataTypes> {
        override val VALUES = values()
        override val NAME_MAP: Map<String, EntityDataDataTypes> = EnumUtil.getEnumValues(VALUES)
    }
}
