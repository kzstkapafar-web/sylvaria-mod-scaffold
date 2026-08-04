package net.sylvariamod;

import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sylvaria — a magical forest dimension mod.
 * Reach it through an ancient stone portal grown over with glowing moss.
 */
@Mod(SylvariaMod.MODID)
public class SylvariaMod {
    public static final String MODID = "sylvaria";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public SylvariaMod() {
        LOGGER.info("Sylvaria mod initializing - the forest awakens");
    }
}
