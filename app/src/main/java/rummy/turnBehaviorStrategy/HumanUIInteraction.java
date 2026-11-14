package rummy.turnBehaviorStrategy;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import rummy.engine.TablePort;

/**
 * HumanUIInteraction - Human player UI interaction interface
 * Defines all UI interaction operations for human players in the game
 */
public interface HumanUIInteraction {

    /**
     * Select draw source (discard pile or stock)
     */
    Card selectPickupSource(TablePort table);

    /**
     * Select card to discard
     */
    Card selectDiscardCard(Hand hand, Card drawnCard);

    /**
     * Determine if should declare Rummy (Classic Rummy)
     */
    boolean shouldDeclareRummy(Hand hand);

    /**
     * Determine if should declare Gin (Gin Rummy)
     */
    boolean shouldDeclareGin(Hand hand);

    /**
     * Determine if should declare Knock (Gin Rummy)
     */
    boolean shouldDeclareKnock(Hand hand);
}
