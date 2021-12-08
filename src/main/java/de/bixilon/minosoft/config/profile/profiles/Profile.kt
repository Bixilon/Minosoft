package de.bixilon.minosoft.config.profile.profiles

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonMerge
import com.fasterxml.jackson.annotation.OptBoolean
import de.bixilon.minosoft.config.profile.ProfileManager

interface Profile {
    @get:JsonMerge(OptBoolean.FALSE)
    val version: Int
    var description: String
    @get:JsonIgnore val manager: ProfileManager<Profile>
    @get:JsonIgnore val name: String
        get() = manager.getName(this)

    @get:JsonIgnore var saved: Boolean
    @get:JsonIgnore val initializing: Boolean
    @get:JsonIgnore var reloading: Boolean
    @get:JsonIgnore var ignoreNextReload: Boolean // used for saving and not instantly reloading
}
