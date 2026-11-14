package rummy.engine;

import java.util.Properties;
import rummy.ai.*;

/**
 * RummyGameFactory - Game factory class
 * Creates corresponding game instances based on properties
 */
public class RummyGameFactory {

    public static RummyGame create(Properties properties) {
        String mode = properties.getProperty("mode", "classic");

        // Determine game mode
        GameMode gameMode = "gin".equalsIgnoreCase(mode) ? GameMode.GIN : GameMode.CLASSIC;

        // Use Builder to construct game
        // Note: In tests, both P0 and P1 are AI (isAuto=true)
        boolean isAuto = Boolean.parseBoolean(properties.getProperty("isAuto", "false"));

        RummyGameBuilder builder = new RummyGameBuilder().withProperties(properties);

        // Configure players based on isAuto mode
        if (isAuto) {
            // Auto mode: both players are AI
            builder.addAI(0, "P0");
            builder.addAI(1, "P1");
        } else {

            builder.addAI(0, "Computer");
            builder.addHuman(1, "Human");
        }

        return builder.build(gameMode);
    }

    /**
     * Original method - kept for compatibility
     */
    public static RummyGame createRummyGame(Properties properties) {
        return create(properties);
    }
}