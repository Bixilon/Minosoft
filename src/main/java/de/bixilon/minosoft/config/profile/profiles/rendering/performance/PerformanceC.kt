package de.bixilon.minosoft.config.profile.profiles.rendering.performance

import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager.delegate

class PerformanceC {

    /**
     * Does not render the bottom bedrock face when at minimum y.
     * Kind of xray when falling out of the world.
     */
    var fastBedrock by delegate(true)

    /**
     * Disables the voronoi noise for biome cache building.
     * Biomes may not match anymore.
     * If true, chunk receiving is way faster.
     * Only affects 19w36+ (~1.14.4)
     * ToDo: Requires rejoin to apply
     */
    var fastBiomeNoise by delegate(true)
}
