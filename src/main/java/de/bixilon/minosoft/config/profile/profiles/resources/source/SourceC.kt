package de.bixilon.minosoft.config.profile.profiles.resources.source

import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager.delegate

class SourceC {
    var pixlyzer by delegate("https://gitlab.com/bixilon/pixlyzer-data/-/raw/master/hash/\${hashPrefix}/\${fullHash}.mbf?inline=false")
    var minecraftResources by delegate("https://resources.download.minecraft.net/\${hashPrefix}/\${fullHash}")
    var mojangPackages by delegate("https://launchermeta.mojang.com/v1/packages/\${fullHash}/\${filename}")
    var launcherPackages by delegate("https://launcher.mojang.com/v1/objects/\${fullHash}/\${filename}")
}
