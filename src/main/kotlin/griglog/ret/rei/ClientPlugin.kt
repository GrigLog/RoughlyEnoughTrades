package griglog.ret.rei

import griglog.ret.RET
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import net.minecraft.resources.ResourceLocation


class ClientPlugin : REIClientPlugin{
    companion object Constants{
        val id = ResourceLocation(RET.id, "client_plugin")
    }

    override fun registerCategories(registry: CategoryRegistry?) {
        registry?.add(VillagerTradesCategory())
    }

    override fun registerDisplays(registry: DisplayRegistry?) {
        villagerTradesRegister(registry!!)
    }
}