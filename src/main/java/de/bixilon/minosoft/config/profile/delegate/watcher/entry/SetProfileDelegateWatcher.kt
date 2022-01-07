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

package de.bixilon.minosoft.config.profile.delegate.watcher.entry

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.watcher.WatchUtil.identifier
import de.bixilon.minosoft.config.profile.delegate.ProfilesDelegateManager
import de.bixilon.minosoft.config.profile.delegate.watcher.ProfileDelegateWatcher
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import javafx.collections.SetChangeListener
import kotlin.reflect.KProperty

class SetProfileDelegateWatcher<V>(
    override val property: KProperty<MutableSet<V>>,
    override val profile: Profile?,
    private val callback: (SetChangeListener.Change<V>) -> Unit,
) : ProfileDelegateWatcher<MutableSet<V>> {
    override val fieldIdentifier = property.identifier

    override fun invoke(previous: Any?, value: Any?) {
        callback(value.unsafeCast())
    }

    companion object {

        @JvmOverloads
        fun <V> KProperty<MutableSet<V>>.profileWatchSet(reference: Any, profile: Profile? = null, callback: ((SetChangeListener.Change<V>) -> Unit)) {
            ProfilesDelegateManager.register(reference, SetProfileDelegateWatcher(this, profile, callback))
        }

        @JvmOverloads
        fun <V> KProperty<MutableSet<V>>.profileWatchSetFX(reference: Any, profile: Profile? = null, callback: ((SetChangeListener.Change<V>) -> Unit)) {
            ProfilesDelegateManager.register(reference, SetProfileDelegateWatcher(this, profile) { JavaFXUtil.runLater { callback(it) } })
        }
    }
}
