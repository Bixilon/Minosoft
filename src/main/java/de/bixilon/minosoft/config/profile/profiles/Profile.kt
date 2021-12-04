package de.bixilon.minosoft.config.profile.profiles

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonMerge
import com.fasterxml.jackson.annotation.OptBoolean

interface Profile {
    @get:JsonMerge(OptBoolean.FALSE)
    val version: Int
    var description: String

    @get:JsonIgnore var saved: Boolean
    @get:JsonIgnore val initializing: Boolean
    @get:JsonIgnore var reloading: Boolean
}
