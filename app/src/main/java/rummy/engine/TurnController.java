package rummy.engine;

import rummy.model.*;
import rummy.turnBehaviorStrategy.TurnBehavior;

/**
 * TurnController - Turn controller
 * Responsible for executing player turns
 */
public class TurnController {
    protected final Player player;
    protected final TurnBehavior behavior;
    protected final TablePort table;

    public TurnController(Player player, TurnBehavior behavior, TablePort table) {
        this.player = player;
        this.behavior = behavior;
        this.table = table;
    }

    /**
     * Execute player turn (Template Method: fixed turn framework)
     */
    public void playTurn() {
        // Reset turn log
        table.resetTurnLog();

        // Execute turn
        behavior.takeTurn(player, table);
    }
}
