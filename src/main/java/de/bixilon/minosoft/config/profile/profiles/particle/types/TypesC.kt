package de.bixilon.minosoft.config.profile.profiles.particle.types

import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfileManager.delegate

class TypesC {
    /**
     * Shows particles from explosions
     */
    var explosions by delegate(true)

    /**
     * Shows custom particles
     */
    var packet by delegate(true)
}
