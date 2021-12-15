package de.bixilon.minosoft.config.profile.profiles.resources.assets.packs

import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.assets.directory.DirectoryAssetsManager
import de.bixilon.minosoft.assets.file.ZipAssetsManager

enum class ResourcePackType(val creator: (ResourcePack) -> AssetsManager) {
    ZIP({ ZipAssetsManager(it.path) }),
    DIRECTORY({ DirectoryAssetsManager(it.path) }),
    ;
}
