package rummy.ai.pickupStrategry;

import ch.aplu.jcardgame.Card;
import rummy.engine.TablePort;
import rummy.model.Player;

import java.util.Random;


public class RandomPickupPolicy implements PickupPolicy {
    private final Random rnd = new Random();

    @Override
    public Card pick(Player player, TablePort table) {
        // If discard pile not empty: choose between discard top vs stock top;
        // if discard pile empty: can only draw from stock
        if (table.peekDiscardTop() != null && rnd.nextBoolean()) {
            return table.drawFromDiscard(player);
        }

        return table.drawFromStock(player);
    }
}