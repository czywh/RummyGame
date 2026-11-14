package rummy.ai.pickupStrategry;


import ch.aplu.jcardgame.Card;
import rummy.engine.TablePort;
import rummy.model.Player;


public interface PickupPolicy {
    /** Return drawn card  */
    Card pick(Player player, TablePort table);
}