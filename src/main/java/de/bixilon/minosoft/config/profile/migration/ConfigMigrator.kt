package de.bixilon.minosoft.config.profile.migration

interface ConfigMigrator {

    fun migrate(data: MutableMap<String, Any>)
}
