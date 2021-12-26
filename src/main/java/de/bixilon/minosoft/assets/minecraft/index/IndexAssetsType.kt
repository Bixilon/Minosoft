package de.bixilon.minosoft.assets.minecraft.index

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum

enum class IndexAssetsType {
    LANGUAGE,
    SOUNDS,
    TEXTURES,
    ;

    companion object : ValuesEnum<IndexAssetsType> {
        override val VALUES: Array<IndexAssetsType> = values()
        override val NAME_MAP: Map<String, IndexAssetsType> = EnumUtil.getEnumValues(VALUES)

    }
}
