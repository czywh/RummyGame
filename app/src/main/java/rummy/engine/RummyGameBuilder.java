package rummy.engine;

import java.util.*;
import rummy.ai.*;
import rummy.model.Player;
import rummy.rules.*;
import rummy.turnBehaviorStrategy.*;

/**
 * RummyGameBuilder:
 * - Builds complete RummyGame system (Game + Players + Controllers + TablePort)
 */
public final class RummyGameBuilder {
    private final Properties props = new Properties();
    private final List<Player> players = new ArrayList<>();

    public RummyGameBuilder withProperties(Properties p) {
        if (p != null) this.props.putAll(p);
        return this;
    }

    public RummyGameBuilder addHuman(int id, String name) {
        players.add(new Player(id, name, true));
        return this;
    }

    public RummyGameBuilder addAI(int id, String name) {
        players.add(new Player(id, name, false));
        return this;
    }

    public RummyGame build(GameMode gameMode) {
        // 1. Use passed gameMode
        boolean isGin = (gameMode == GameMode.GIN);

        // 2. Read AI mode
        boolean smart = Boolean.parseBoolean(props.getProperty("computer_smart", "false"));

        // 3. Scoring rules
        RuleFactory rf = RuleFactory.getRuleFactory();
        ScoreRule scoring = isGin ? rf.forGin() : rf.forClassic();

        // 4. Create game object
        RummyGame game = isGin ? new GinRummy(props, scoring) : new ClassicRummy(props, scoring);

        // 5. Players (if not set)
        if (players.isEmpty()) {
            // Default: 0 is computer, 1 is human
            players.add(new Player(0, "Computer", false));
            players.add(new Player(1, "Human", true));
        }
        game.setPlayers(players);

        // 6. Create TablePort (don't deal cards here! Dealing happens in initRound())
        TablePort tablePort = new TablePort(game.deck, isGin);

        // 7. Create controllers
        List<TurnController> controllers = new ArrayList<>();
        for (Player p : players) {
            TurnBehavior behavior;
            if (p.isHuman()) {
                // Create HumanBehavior for human player, need to pass game instance
                behavior = new HumanBehavior(isGin, game);
            } else {
                ComputerStrategy strategy = smart
                        ? StrategyFactory.build(AIMode.SMART, isGin ? GameMode.GIN : GameMode.CLASSIC)
                        : StrategyFactory.build(AIMode.RANDOM, isGin ? GameMode.GIN : GameMode.CLASSIC);
                behavior = new ComputerBehavior(strategy);
            }
            controllers.add(new TurnController(p, behavior, tablePort));
        }

        game.setControllers(controllers);
        game.setTablePort(tablePort);

        // 8. Set UI listeners for human players
        for (Player p : players) {
            if (p.isHuman()) {
                break; // Assume only one human player
            }
        }

        return game;
    }
}