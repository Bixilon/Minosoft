package de.bixilon.minosoft.config.profile.profiles.entity.hitbox

import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfileManager.delegate

class HitboxC {
    /**
     * Enables or disables hit-boxes for all entities
     */
    var enabled by delegate(true)

    /**
     * Shows your own hit-box when in first person view
     */
    var showLocal by delegate(false)

    /**
     * Shows hit-boxes from invisible entities
     */
    var showInvisible by delegate(false)

    /**
     * If true: Shows full colored hit-boxes (aka. lazy boxes).
     * If false: Shows just the outline of the hit-box
     */
    var lazy by delegate(false)

    /**
     * Disables the z-buffer when rendering
     * => Shows the boxes through walls
     */
    var showThroughWalls by delegate(false)
}
