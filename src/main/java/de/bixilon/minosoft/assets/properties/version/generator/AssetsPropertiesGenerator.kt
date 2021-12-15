package de.bixilon.minosoft.assets.properties.version.generator

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.assets.InvalidAssetException
import de.bixilon.minosoft.assets.minecraft.JarAssetsManager
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.util.CountUpAndDownLatch

object AssetsPropertiesGenerator {

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 2) {
            throw IllegalArgumentException("Usage: application <version id> <client jar hash>")
        }
        // create profile to not make crashes (or load an actual profile)
        val profile = ResourcesProfileManager.createProfile()
        profile.verify = false
        val (versionId, clientJarHash) = args

        val assetsManager = JarAssetsManager("dummy", clientJarHash, profile, Version(versionId, -1, -1, HashBiMap.create(), HashBiMap.create()))
        try {
            assetsManager.load(CountUpAndDownLatch(1))
        } catch (exception: InvalidAssetException) {
            // this exception is thrown, because our initial has is "dummy"
            print(exception.hash)
        }
    }
}
