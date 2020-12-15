package adudecalledleo.ostbm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

public final class Main {
    public static final Logger LOGGER = LogManager.getLogger("OSTBM");

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Sadly, OneShot Textbox Maker cannot run in headless mode.");
            System.exit(0);
        }
        LOGGER.fatal("fatal");
        LOGGER.error("error");
        LOGGER.warn("warn");
        LOGGER.info("info");
        LOGGER.debug("debug");
    }
}
