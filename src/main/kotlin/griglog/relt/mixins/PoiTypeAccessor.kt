package griglog.relt.mixins

import net.minecraft.world.entity.ai.village.poi.PoiType
import net.minecraft.world.level.block.state.BlockState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(PoiType::class)
interface PoiTypeAccessor {
    @Accessor("matchingStates")
    fun getMatchingStates(): Set<BlockState>
}