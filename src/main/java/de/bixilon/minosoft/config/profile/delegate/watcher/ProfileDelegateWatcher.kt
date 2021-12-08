package de.bixilon.minosoft.config.profile.delegate.watcher

import de.bixilon.minosoft.config.profile.profiles.Profile
import kotlin.reflect.KProperty

interface ProfileDelegateWatcher<T> {
    val property: KProperty<T>
    val fieldIdentifier: String
    val profile: Profile?


    fun invoke(previous: Any?, value: Any?)
}
