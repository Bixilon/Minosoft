package de.bixilon.minosoft.config.profile.profiles.resources.assets

import de.bixilon.minosoft.assets.minecraft.index.IndexAssetsType
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager.listDelegate
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager.setDelegate
import de.bixilon.minosoft.config.profile.profiles.resources.assets.packs.ResourcePack

class AssetsC {
    var disableJarAssets by delegate(false)
    var disableIndexAssets by delegate(false)
    val indexAssetsTypes: MutableSet<IndexAssetsType> by setDelegate(mutableSetOf(*IndexAssetsType.VALUES))

    val resourcePacks: List<ResourcePack> by listDelegate()
}
