package rummy.ai.discardStrategy;


import ch.aplu.jcardgame.Card;
import rummy.engine.TablePort;
import rummy.model.Player;

public interface DiscardPolicy {
    /** Discard a card based on evaluation result (can discard drawn card itself or other cards in hand) */
    void discard(Player player, TablePort table, Card drawn);
}