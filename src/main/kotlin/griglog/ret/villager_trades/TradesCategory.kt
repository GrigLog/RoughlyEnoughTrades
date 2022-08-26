package griglog.ret.villager_trades

import griglog.ret.RET
import griglog.ret.utils.toRoman
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.util.EntryIngredients
import me.shedaniel.rei.api.common.util.EntryStacks
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.item.Items.EMERALD

//responsible for GUI drawing
class TradesCategory : DisplayCategory<TradesDisplay> {
    companion object Costants {
        val category = CategoryIdentifier.of<TradesDisplay>(RET.id, "plugins/villager_trades")
    }

    override fun getCategoryIdentifier(): CategoryIdentifier<TradesDisplay> = category

    override fun getTitle() = TranslatableComponent("merchant.trades")

    override fun getIcon() = EntryStacks.of(EMERALD)

    override fun getDisplayHeight() = 30

    override fun getDisplayWidth(display: TradesDisplay?) = 130

    override fun setupDisplay(display: TradesDisplay, bounds: Rectangle): MutableList<Widget> {
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
            .entries(display.outputEntries[0])
            .markOutput())

        widgets.add(Widgets.createLabel(Point(center.x + 50, center.y - 4),
            TextComponent(toRoman(display.tier)))
            .color(0x666666)
            .noShadow())

        widgets.add(Widgets.createSlot(Point(center.x - 40, center.y - 8))
            .entries(display.inputEntries[0])
            .markInput())

        widgets.add(Widgets.createSlot(Point(center.x - 60, center.y - 8))
            .entries(display.inputEntries[1])
            .markInput())

        if (extraSlot){
            widgets.add(Widgets.createSlot(Point(center.x - 80, center.y - 8)).
            entries(display.inputEntries[2]))
        }

        return widgets
    }
}