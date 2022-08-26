package griglog.ret.villager_trades

import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.world.item.ItemStack
import java.util.*

//represets one JEI recipe. Despite its name, has nothing to do with drawing :/
class TradesDisplay(
    val profNameKey: String,
    val stations: Collection<ItemStack>,
    val tier: Int,
    val input: ItemStack,
    val output: Collection<ItemStack>,
    val inputSecondary: ItemStack?,
    val reliable: Boolean)
    : Display {

    override fun getCategoryIdentifier(): CategoryIdentifier<*> = TradesCategory.category

    override fun getInputEntries(): List<EntryIngredient> {
        val inputs = mutableListOf(EntryIngredients.of(input), EntryIngredients.ofItemStacks(stations))
        inputSecondary?.let{ inputs.add(EntryIngredients.of(it)) }
        return inputs
    }

    override fun getOutputEntries(): MutableList<EntryIngredient> =
        Collections.singletonList(EntryIngredients.ofItemStacks(output))

    class Builder(val profNameKey: String, val stations: Collection<ItemStack>, val tier: Int){
        fun build(input: ItemStack, output: Collection<ItemStack>, inputSecondary: ItemStack? = null, reliable: Boolean = true) =
            TradesDisplay(profNameKey, stations, tier, input, output, inputSecondary, reliable)

        fun build(input: ItemStack, output: ItemStack, inputSecondary: ItemStack? = null, reliable: Boolean = true) =
            build(input, listOf(output), inputSecondary, reliable)
    }

}