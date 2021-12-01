package de.bixilon.minosoft.config.profile.change.listener

import de.bixilon.minosoft.config.profile.profiles.Profile
import java.lang.reflect.Field
import kotlin.reflect.KProperty

interface ProfileChangeListener<T> {
    val property: KProperty<T>
    val field: Field
    val profile: Profile?


    fun invoke(previous: Any?, value: Any?)
}
