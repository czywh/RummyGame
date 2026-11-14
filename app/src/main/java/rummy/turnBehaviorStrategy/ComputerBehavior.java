package rummy.turnBehaviorStrategy;

import ch.aplu.jcardgame.Card;
import rummy.ai.ComputerStrategy;
import rummy.engine.TablePort;
import rummy.model.Player;


/**
 * Unified computer behavior template:
 *   Step 1 + 2: Draw card and (if necessary) evaluate usefulness;
 *   Step 3: Discard card;
 *   Step 4: Declare / End.
 */
public class ComputerBehavior implements TurnBehavior {
    protected final ComputerStrategy strategy;

    public ComputerBehavior(ComputerStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public final void takeTurn(Player player, TablePort table) {
        // Step1 + Step2: Draw card (some strategies evaluate usefulness during draw phase)
        Card drawn = strategy.pickup().pick(player, table);

        // Step3: Discard card
        strategy.discard().discard(player, table, drawn);

        // Step4: Declare
        strategy.declare().tryDeclare(player, table);

    }
}