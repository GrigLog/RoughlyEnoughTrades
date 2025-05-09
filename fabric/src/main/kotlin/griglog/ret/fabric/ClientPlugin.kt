package griglog.ret.fabric


import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry

class ClientPlugin : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        griglog.ret.ClientPlugin.registerCategories(registry)
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        griglog.ret.ClientPlugin.registerDisplays(registry)
    }
}