/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.profile.delegate.watcher

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.watcher.WatchUtil.identifier
import de.bixilon.minosoft.config.profile.delegate.ProfilesDelegateManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.rendering.Rendering
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

class SimpleProfileDelegateWatcher<T>(
    override val property: KProperty<T>,
    override val profile: Profile?,
    instant: Boolean,
    private val callback: (T) -> Unit,
) : ProfileDelegateWatcher<T> {
    override val fieldIdentifier: String = property.identifier

    init {
        if (instant) {
            when (property) {
                is KProperty0<*> -> invoke(property.get(), property.get())
                else -> TODO("Instant fire is not supported for ${property::class.java}")
            }
        }
    }

    override fun invoke(previous: Any?, value: Any?) {
        callback(value.unsafeCast())
    }

    companion object {

        @JvmOverloads
        fun <T> KProperty<T>.profileWatch(reference: Any, instant: Boolean = false, profile: Profile? = null, callback: ((T) -> Unit)) {
            ProfilesDelegateManager.register(reference, SimpleProfileDelegateWatcher(this, profile, instant, callback))
        }

        @JvmOverloads
        fun <T> KProperty<T>.profileWatchFX(reference: Any, instant: Boolean = false, profile: Profile? = null, callback: ((T) -> Unit)) {
            ProfilesDelegateManager.register(reference, SimpleProfileDelegateWatcher(this, profile, instant) { JavaFXUtil.runLater { callback(it) } })
        }

        @JvmOverloads
        fun <T> KProperty<T>.profileWatchRendering(reference: Any, instant: Boolean = false, profile: Profile? = null, callback: ((T) -> Unit)) {
            val context = Rendering.currentContext ?: throw IllegalStateException("Can only be registered in a render context!")
            ProfilesDelegateManager.register(reference, SimpleProfileDelegateWatcher(this, profile, instant) {
                val changeContext = Rendering.currentContext
                if (changeContext === context) {
                    callback(it)
                } else {
                    context.queue += { callback(it) }
                }
            })
        }
    }
}
