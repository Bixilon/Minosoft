package de.bixilon.minosoft.gui.eros.main.play.server.type.types

import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.Server
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCard
import de.bixilon.minosoft.protocol.protocol.LANServerListener
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.DelegateManager.listDelegate
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

object LANServerType : ServerType {
    override val icon: Ikon = FontAwesomeSolid.NETWORK_WIRED
    override val hidden: Boolean
        get() = !LANServerListener.listening
    override val servers: MutableList<Server> by listDelegate()
    override val translationKey: ResourceLocation = "minosoft:server_type.lan".toResourceLocation()

    override fun refresh(cards: List<ServerCard>) {
        LANServerListener.clear()
    }
}
