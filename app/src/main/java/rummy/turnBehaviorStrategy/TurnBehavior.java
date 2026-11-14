package rummy.turnBehaviorStrategy;

import rummy.engine.TablePort;
import rummy.model.Player;

public interface TurnBehavior {
    void takeTurn(Player player, TablePort table);
}