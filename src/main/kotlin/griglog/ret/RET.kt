package griglog.ret
import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager

object RET: ModInitializer {
    const val id = "roughly_enough_trades"
    val logger = LogManager.getLogger(id)

    override fun onInitialize() {
        //ServerLifecycleEvents.SERVER_STARTED.register { server -> logger.info("RELT server started");}
    }
}