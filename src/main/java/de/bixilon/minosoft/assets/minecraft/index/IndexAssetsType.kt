package de.bixilon.minosoft.assets.minecraft.index

import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

enum class IndexAssetsType {
    LANGUAGE,
    SOUNDS,
    TEXTURES,
    ;

    companion object : ValuesEnum<IndexAssetsType> {
        override val VALUES: Array<IndexAssetsType> = values()
        override val NAME_MAP: Map<String, IndexAssetsType> = KUtil.getEnumValues(VALUES)

    }
}
