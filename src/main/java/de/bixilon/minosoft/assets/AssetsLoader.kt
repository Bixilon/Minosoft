package de.bixilon.minosoft.assets

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.assets.minecraft.JarAssetsManager
import de.bixilon.minosoft.assets.minecraft.index.IndexAssetsManager
import de.bixilon.minosoft.assets.multi.PriorityAssetsManager
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperties
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperty
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.versions.Version

object AssetsLoader {

    fun create(profile: ResourcesProfile, version: Version, latch: CountUpAndDownLatch, property: AssetsVersionProperty = AssetsVersionProperties[version] ?: throw IllegalAccessException("$version has no assets!")): AssetsManager {
        val assetsManager = PriorityAssetsManager()

        for (resourcePack in profile.assets.resourcePacks) {
            resourcePack.type.creator(resourcePack).let {
                it.load(latch)
                assetsManager += it
            }
        }

        if (!profile.assets.disableIndexAssets) {
            assetsManager += IndexAssetsManager(profile, property.indexVersion, property.indexHash, profile.assets.indexAssetsTypes.toSet())
        }
        if (!profile.assets.disableJarAssets) {
            assetsManager += JarAssetsManager(property.jarAssetsHash, property.clientJarHash, profile, version)
        }
        assetsManager += Minosoft.MINOSOFT_ASSETS_MANAGER

        return assetsManager
    }
}
