package de.bixilon.minosoft.config.profile.profiles

import com.fasterxml.jackson.annotation.JsonIgnore

interface Profile {
    val version: Int
    val description: String?

    @get:JsonIgnore var saved: Boolean
    @get:JsonIgnore val initializing: Boolean
}
