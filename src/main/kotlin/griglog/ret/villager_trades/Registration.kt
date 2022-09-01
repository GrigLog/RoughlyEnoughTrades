package griglog.ret.villager_trades

import griglog.ret.utils.wrapHoverName
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import net.minecraft.client.Minecraft
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.npc.VillagerTrades
import net.minecraft.world.item.EnchantedBookItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.Items.EMERALD
import net.minecraft.world.item.SuspiciousStewItem
import net.minecraft.world.item.alchemy.PotionBrewing
import net.minecraft.world.item.alchemy.PotionUtils
import net.minecraft.world.item.enchantment.EnchantmentInstance
import net.minecraft.world.item.trading.MerchantOffer
import net.minecraft.world.level.block.Blocks
import java.util.*

fun villagerTradesRegister(registry: DisplayRegistry) {
    val rand = RandomSource.create()
    for (profession in Registry.VILLAGER_PROFESSION) {
        val knownJobBlocks = HashMap<ResourceLocation, ItemStack>()
        for (poiType in Registry.POINT_OF_INTEREST_TYPE.holders()){
            if (!profession.acquirableJobSite.test(poiType))
                continue
            for (state in poiType.value().matchingStates) {
                val jobBlockItem = ItemStack(state.block)
                if (jobBlockItem.isEmpty)
                    continue
                val id = Registry.ITEM.getKey(jobBlockItem.item)
                knownJobBlocks.putIfAbsent(id, jobBlockItem)
            }
        }
        if (knownJobBlocks.isEmpty())
            continue
        val profId = Registry.VILLAGER_PROFESSION.getKey(profession)
        VillagerTrades.TRADES[profession]?.let {
            regProfession(registry, "entity.minecraft.villager." + profId.path, knownJobBlocks.values, it, rand) }
    }
    regProfession(registry, "entity.minecraft.wandering_trader",
        listOf(ItemStack(Items.WANDERING_TRADER_SPAWN_EGG)), VillagerTrades.WANDERING_TRADER_TRADES, rand)
}


private fun regProfession(registry: DisplayRegistry, name: String, jobBlocks: Collection<ItemStack>,
                          trades: Int2ObjectMap<Array<VillagerTrades.ItemListing>>, rand: RandomSource){
    var someFailed = false
    trades.toSortedMap().forEach { (tier, trades) ->
        val display = TradesDisplay.Builder(name, jobBlocks, tier)
        for (trade in trades){
            when(trade){
                is VillagerTrades.EmeraldForItems ->
                    registry.add(display.build(
                        ItemStack(trade.item, trade.cost),
                        ItemStack(EMERALD)))
                is VillagerTrades.ItemsForEmeralds ->
                    registry.add(display.build(
                        ItemStack(EMERALD, trade.emeraldCost),
                        ItemStack(trade.itemStack.item, trade.numberOfItems)))
                is VillagerTrades.SuspiciousStewForEmerald -> {
                    val stack = ItemStack(Items.SUSPICIOUS_STEW)
                    SuspiciousStewItem.saveMobEffect(stack, trade.effect, trade.duration)
                    registry.add(display.build(
                        ItemStack(EMERALD),
                        stack))
                }
                is VillagerTrades.ItemsAndEmeraldsToItems ->
                    registry.add(display.build(
                        ItemStack(EMERALD, trade.emeraldCost),
                        ItemStack(trade.toItem.item, trade.toCount),
                        ItemStack(trade.fromItem.item, trade.fromCount)))
                is VillagerTrades.EnchantedItemForEmeralds ->
                    registry.add(display.build(
                        ItemStack(EMERALD, trade.baseEmeraldCost),
                        wrapHoverName(ItemStack(trade.itemStack.item))))
                is VillagerTrades.EmeraldsForVillagerTypeItem -> {
                    for (item in trade.trades.values)
                        registry.add(display.build(
                            ItemStack(item),
                            ItemStack(EMERALD, trade.cost)))
                }
                is VillagerTrades.EnchantBookForEmeralds -> {
                    val map = mutableMapOf<Int, MutableList<ItemStack>>()
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
                            map.computeIfAbsent(avgCost) { mutableListOf() }.add(book)
                        }
                    }
                    map.forEach{(cost, books) ->
                        registry.add(display.build(ItemStack(EMERALD, cost), books)) }
                }
                is VillagerTrades.TreasureMapForEmeralds ->
                    registry.add(display.build(ItemStack(EMERALD, trade.emeraldCost), wrapHoverName(ItemStack(Items.FILLED_MAP))))
                is VillagerTrades.TippedArrowForItemsAndEmeralds -> {
                    val arrows = Registry.POTION
                        .filter { !it.effects.isEmpty() && PotionBrewing.isBrewablePotion(it) }
                        .map { PotionUtils.setPotion(ItemStack(trade.toItem.item, trade.toCount), it) }
                    registry.add(display.build(
                        ItemStack(EMERALD, trade.emeraldCost),
                        arrows))
                }
                is VillagerTrades.DyedArmorForEmeralds -> {
                    registry.add(display.build(
                        ItemStack(EMERALD, trade.value),
                        wrapHoverName(ItemStack(trade.item)),
                        ItemStack(trade.item)))
                }
                else -> {
                    someFailed = true
                    try {
                        var attempts = 5
                        val tryDifferentOffers = TreeSet(::compareOffers)
                        var offer: MerchantOffer?
                        while (attempts > 0){
                            offer = trade.getOffer(Minecraft.getInstance().player!!, rand)
                            if (offer != null && tryDifferentOffers.add(offer))
                                attempts++
                            else
                                attempts--;
                        }
                        tryDifferentOffers.forEach {registry.add(display.build(it.baseCostA, it.result), if (it.costB.isEmpty) null else it.costB)}
                    } catch (e: Exception){}
                }
            }
        }
    }
    if (someFailed){
        registry.add(TradesDisplay(name, jobBlocks, 0, ItemStack(EMERALD), listOf(ItemStack(EMERALD)), null, false))
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