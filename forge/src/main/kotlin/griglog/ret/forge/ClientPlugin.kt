package griglog.ret.forge

import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.forge.REIPluginClient

@REIPluginClient
class ClientPlugin : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        griglog.ret.ClientPlugin.registerCategories(registry)
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        griglog.ret.ClientPlugin.registerDisplays(registry)
    }
}