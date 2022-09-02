package griglog.ret

import griglog.ret.villager_trades.TradesCategory
import griglog.ret.villager_trades.villagerTradesRegister
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items


class ClientPlugin : REIClientPlugin{
    override fun registerCategories(registry: CategoryRegistry) {
        registry.add(TradesCategory())
        registry.addWorkstations(TradesCategory.category, EntryIngredients.of(ItemStack(Items.VILLAGER_SPAWN_EGG)))
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        villagerTradesRegister(registry)
    }
}