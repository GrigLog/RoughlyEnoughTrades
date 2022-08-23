package griglog.relt.rei

import griglog.relt.RELT
import griglog.relt.utils.toRoman
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryIngredients
import me.shedaniel.rei.api.common.util.EntryStacks
import net.minecraft.client.gui.screens.inventory.AnvilScreen
import net.minecraft.core.Registry
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.npc.VillagerTrades
import net.minecraft.world.item.EnchantedBookItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.Items.EMERALD
import net.minecraft.world.item.SuspiciousStewItem
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.EnchantmentInstance
import net.minecraft.world.item.enchantment.Enchantments
import java.util.*

class VillagerTradesCategory : DisplayCategory<VillagerTradesCategory.Display> {
    companion object Costants {
        val category = CategoryIdentifier.of<Display>(RELT.id, "plugins/villager_trades")
    }

    override fun getCategoryIdentifier(): CategoryIdentifier<Display> = category

    override fun getTitle() = TranslatableComponent("${RELT.id}.villager_trades")

    override fun getIcon() = EntryStacks.of(Items.EMERALD)

    override fun setupDisplay(display: Display, bounds: Rectangle): MutableList<Widget> {
        val center = Point(bounds.centerX, bounds.centerY)
        val widgets = mutableListOf<Widget>()

        val extraSlot = display.inputSecondary != null
        widgets.add(Widgets.createRecipeBase(Rectangle(bounds).apply{width -= if (extraSlot) 20 else 0}))

        val arrow = Widgets.createArrow(Point(center.x - 12, center.y - 8))
        widgets.add(arrow)
        widgets.add(Widgets.createTooltip(arrow.bounds,
            TranslatableComponent("entity.minecraft.villager." + display.profession)))

        widgets.add(Widgets.createSlot(Point(center.x + 22, center.y - 9))
            .entries(EntryIngredients.of(display.output))
            .markOutput())

        widgets.add(Widgets.createLabel(Point(center.x + 50, center.y - 4),
            TextComponent(toRoman(display.tier)))
            .color(0x666666)
            .noShadow())

        if (extraSlot){
            widgets.add(Widgets.createSlot(Point(center.x - 80, center.y - 8)).
            entries(EntryIngredients.of(display.inputSecondary)))
        }

        widgets.add(Widgets.createSlot(Point(center.x - 40, center.y - 8))
            .entries(EntryIngredients.of(display.input))
            .markInput())

        widgets.add(Widgets.createSlot(Point(center.x - 60, center.y - 8))
            .entries(EntryIngredients.ofItemStacks(display.stations))
            .markInput())

        return widgets
    }

    override fun getDisplayHeight() = 30

    override fun getDisplayWidth(display: Display?) = if (display?.inputSecondary == null) 130 else 170

    class Display
        (val profession: String,
         val stations: Collection<ItemStack>,
         val tier: Int,
         val input: ItemStack,
         val output: ItemStack,
         var inputSecondary: ItemStack? = null)
        : me.shedaniel.rei.api.common.display.Display {

            override fun getCategoryIdentifier(): CategoryIdentifier<*> = category

            override fun getInputEntries(): List<EntryIngredient> {
                val inputs = mutableListOf(EntryIngredients.ofItemStacks(stations), EntryIngredients.of(input))
                inputSecondary?.let{ inputs.add(EntryIngredients.of(it)) }
                return inputs
            }

            override fun getOutputEntries(): MutableList<EntryIngredient> = Collections.singletonList(EntryIngredients.of(output))

    }
}


fun villagerTradesRegister(registry: DisplayRegistry) {
    for (profession in Registry.VILLAGER_PROFESSION) {
        val knownJobBlocks = HashMap<ResourceLocation, ItemStack>();
        for (state in profession.jobPoiType.matchingStates) {
            val jobBlockItem = ItemStack(state.block)
            if (jobBlockItem.isEmpty)
                continue;
            val id = Registry.ITEM.getKey(jobBlockItem.item)
            knownJobBlocks.putIfAbsent(id, jobBlockItem)
        }
        if (knownJobBlocks.isEmpty())
            continue
        VillagerTrades.TRADES[profession]?.toSortedMap()?.forEach { (tier, trades) ->
            val create = { a:ItemStack, b:ItemStack -> VillagerTradesCategory.Display(profession.name, knownJobBlocks.values, tier, a, b)}
            for (trade in trades){
                when(trade){
                    is VillagerTrades.EmeraldForItems ->
                        registry.add(create(ItemStack(trade.item, trade.cost), ItemStack(EMERALD)))
                    is VillagerTrades.ItemsForEmeralds ->
                        registry.add(create(ItemStack(EMERALD, trade.emeraldCost), ItemStack(trade.itemStack.item, trade.numberOfItems)))
                    is VillagerTrades.SuspiciousStewForEmerald -> {
                        val stack = ItemStack(Items.SUSPICIOUS_STEW)
                        SuspiciousStewItem.saveMobEffect(stack, trade.effect, trade.duration)
                        registry.add(create(ItemStack(EMERALD), stack))
                    }
                    is VillagerTrades.ItemsAndEmeraldsToItems ->
                        registry.add(
                            create(ItemStack(EMERALD, trade.emeraldCost), ItemStack(trade.toItem.item, trade.toCount))
                            .apply{inputSecondary = ItemStack(trade.fromItem.item, trade.fromCount)})
                    is VillagerTrades.EnchantedItemForEmeralds -> {
                        val stack = ItemStack(trade.itemStack.item)
                        stack.hoverName = TextComponent("*").append(stack.hoverName).append(TextComponent("*"))
                        registry.add(create(ItemStack(EMERALD), stack))
                    }
                    is VillagerTrades.EmeraldsForVillagerTypeItem -> {
                        for (item in trade.trades.values){
                            registry.add(create(ItemStack(item), ItemStack(EMERALD, trade.cost)))
                        }
                    }
                    is VillagerTrades.EnchantBookForEmeralds -> {
                        for (ench in Registry.ENCHANTMENT){
                            if (!ench.isTradeable)
                                continue
                            for (lvl in (ench.minLevel..ench.maxLevel)){
                                val book = EnchantedBookItem.createForEnchantment(EnchantmentInstance(ench, lvl))
                                var avgCost: Int = 4 + 8 * lvl
                                if (ench.isTreasureOnly)
                                    avgCost *= 2
                                if (avgCost > 64)
                                    avgCost = 64
                                registry.add(create(ItemStack(EMERALD, avgCost), book))
                            }
                        }
                    }
                }
            }
        }
    }
}


