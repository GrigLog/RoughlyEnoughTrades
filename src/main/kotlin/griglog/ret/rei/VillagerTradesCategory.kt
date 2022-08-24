package griglog.ret.rei

import griglog.ret.RET
import griglog.ret.utils.toRoman
import griglog.ret.utils.wrapHoverName
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
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
import net.minecraft.client.Minecraft
import net.minecraft.core.Registry
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.npc.VillagerTrades
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.EnchantedBookItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.Items.EMERALD
import net.minecraft.world.item.SuspiciousStewItem
import net.minecraft.world.item.alchemy.PotionBrewing
import net.minecraft.world.item.alchemy.PotionUtils
import net.minecraft.world.item.enchantment.EnchantmentInstance
import net.minecraft.world.item.trading.MerchantOffer
import java.util.*
import kotlin.collections.HashSet

class VillagerTradesCategory : DisplayCategory<VillagerTradesCategory.Display> {
    companion object Costants {
        val category = CategoryIdentifier.of<Display>(RET.id, "plugins/villager_trades")
    }

    override fun getCategoryIdentifier(): CategoryIdentifier<Display> = category

    override fun getTitle() = TranslatableComponent("merchant.trades")

    override fun getIcon() = EntryStacks.of(EMERALD)

    override fun setupDisplay(display: Display, bounds: Rectangle): MutableList<Widget> {
        val center = Point(bounds.centerX, bounds.centerY)
        val widgets = mutableListOf<Widget>()

        val extraSlot = display.inputSecondary != null

        val rect = Rectangle(bounds).apply{ if(extraSlot) {x -= 20; width += 20} }
        widgets.add(Widgets.createRecipeBase(rect))

        val arrow = Widgets.createArrow(Point(center.x - 12, center.y - 8))
        widgets.add(arrow)
        widgets.add(Widgets.createTooltip(arrow.bounds,
            TranslatableComponent(display.profNameKey)))
        if (!display.reliable){
            widgets.add(Widgets.createLabel(Point(center.x-4, center.y-4),
                TextComponent("???"))
                .color(0x666666)
                .noShadow())
        }

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

    override fun getDisplayWidth(display: Display?) = 130

    class Display
        (val profNameKey: String,
         val stations: Collection<ItemStack>,
         val tier: Int,
         val input: ItemStack,
         val output: ItemStack,
         var inputSecondary: ItemStack? = null,
         var reliable: Boolean = true)
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
        val profId = Registry.VILLAGER_PROFESSION.getKey(profession)
        VillagerTrades.TRADES[profession]?.let {
            regProfession(registry, "entity.minecraft.villager." + profId.path, knownJobBlocks.values, it) }
    }
    regProfession(registry, "entity.minecraft.wandering_trader",
        listOf(ItemStack(Items.WANDERING_TRADER_SPAWN_EGG)), VillagerTrades.WANDERING_TRADER_TRADES)
}

val rand = Random()
private fun regProfession(registry: DisplayRegistry, name: String, jobBlocks: Collection<ItemStack>, trades: Int2ObjectMap<Array<VillagerTrades.ItemListing>>){
    var someFailed = false
    trades.toSortedMap().forEach { (tier, trades) ->
        val create = { a:ItemStack, b:ItemStack -> VillagerTradesCategory.Display(name, jobBlocks, tier, a, b)}
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
                is VillagerTrades.EnchantedItemForEmeralds ->
                    registry.add(create(ItemStack(EMERALD), wrapHoverName(ItemStack(trade.itemStack.item))))
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
                is VillagerTrades.TreasureMapForEmeralds ->
                    registry.add(create(ItemStack(EMERALD, trade.emeraldCost), wrapHoverName(ItemStack(Items.FILLED_MAP))))
                is VillagerTrades.TippedArrowForItemsAndEmeralds -> {
                    for (potion in Registry.POTION){
                        if (potion.effects.isEmpty() || !PotionBrewing.isBrewablePotion(potion))
                            continue
                        val newArrow = ItemStack(trade.toItem.item, trade.toCount)
                        val display = create(ItemStack(EMERALD, trade.emeraldCost), PotionUtils.setPotion(newArrow, potion))
                        display.inputSecondary = ItemStack(trade.fromItem, trade.fromCount)
                        registry.add(display)
                    }
                }
                is VillagerTrades.DyedArmorForEmeralds -> {
                    val display = create(ItemStack(EMERALD, trade.value), wrapHoverName(ItemStack(trade.item)))
                    display.inputSecondary = ItemStack(trade.item)
                    registry.add(display)
                }
                else -> {
                    someFailed = true
                    try {
                        var attempts = 5
                        val tryDifferentOffers = TreeSet(::compareOffers)
                        var offer: MerchantOffer?
                        while (attempts > 0){
                            offer = trade.getOffer(Minecraft.getInstance().player!!, rand)
                            if (offer == null || tryDifferentOffers.add(offer) == false)
                                attempts--
                        }
                        tryDifferentOffers.forEach {registry.add(create(it.baseCostA, it.result)
                            .apply{inputSecondary = if (it.costB.isEmpty) null else it.costB})}
                    } catch (e: Exception){}
                }
            }
        }
    }
    if (someFailed){
        registry.add(VillagerTradesCategory.Display(name, jobBlocks, 0, ItemStack(EMERALD), ItemStack(EMERALD)).apply { reliable = false })
    }
}

fun compareOffers(a: MerchantOffer, b: MerchantOffer): Int {
    var diff = Registry.ITEM.getId(a.baseCostA.item) - Registry.ITEM.getId(b.baseCostA.item)
    if (diff != 0) return diff
    diff = Registry.ITEM.getId(a.costB.item) - Registry.ITEM.getId(b.costB.item)
    if (diff != 0) return diff
    diff = Registry.ITEM.getId(a.result.item) - Registry.ITEM.getId(b.result.item)
    return diff
}