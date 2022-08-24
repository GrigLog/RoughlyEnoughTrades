package griglog.ret

import griglog.ret.villager_trades.TradesCategory
import griglog.ret.villager_trades.villagerTradesRegister
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import net.minecraft.resources.ResourceLocation


class ClientPlugin : REIClientPlugin{
    override fun registerCategories(registry: CategoryRegistry?) {
        registry?.add(TradesCategory())
    }

    override fun registerDisplays(registry: DisplayRegistry?) {
        villagerTradesRegister(registry!!)
    }
}