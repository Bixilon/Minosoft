package de.bixilon.minosoft.config.profile.profiles.entity

import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.entity.hitbox.HitboxC

/**
 * Profile for entity
 */
class EntityProfile(
    description: String? = null,
) : Profile {
    override var initializing: Boolean = true
        private set
    override var saved: Boolean = true
    override val version: Int = latestVersion
    override val description by delegate(description ?: "")


    val hitbox = HitboxC()

    override fun toString(): String {
        return EntityProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
