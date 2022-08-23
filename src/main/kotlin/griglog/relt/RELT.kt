package griglog.relt
import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager

object RELT: ModInitializer {
    const val id = "roughly_enough_loot_tables"
    val logger = LogManager.getLogger(id)

    override fun onInitialize() {
        //ServerLifecycleEvents.SERVER_STARTED.register { server -> logger.info("RELT server started");}
    }
}