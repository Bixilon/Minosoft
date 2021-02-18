package de.bixilon.minosoft.config.config.network

import com.squareup.moshi.Json

data class NetworkConfig(
    @Json(name = "fake_network_brand") var fakeNetworkBrand: Boolean = false,
    @Json(name = "show_lan_servers") var showLanServers: Boolean = true,
)
