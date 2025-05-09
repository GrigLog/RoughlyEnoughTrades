package griglog.ret.forge

import dev.architectury.platform.forge.EventBuses
import griglog.ret.RET
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT

@Mod(RET.id)
object RETForge {
    init {
        System.out.println("KOTLIN FORGE INIT")
        EventBuses.registerModEventBus(RET.id, MOD_CONTEXT.getKEventBus()) //The game crashes if I don't register???
    }
}