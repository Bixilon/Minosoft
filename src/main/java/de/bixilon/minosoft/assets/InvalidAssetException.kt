package de.bixilon.minosoft.assets

import de.bixilon.minosoft.data.registries.ResourceLocation
import java.io.IOException

class InvalidAssetException(
    val path: ResourceLocation,
    val hash: String,
    val expectedHash: String,
) : IOException("Assets verification exception ($path): Got $hash, expected $expectedHash")
