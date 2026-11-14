package rummy.turnBehaviorStrategy;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import rummy.engine.TablePort;
import rummy.engine.RummyGame;
import rummy.model.Player;

/**
 * HumanBehavior - Human player behavior
 */
public class HumanBehavior implements TurnBehavior {
    private final boolean ginMode;
    private final HumanUIInteractionImpl uiInteraction;

    public HumanBehavior(boolean ginMode, RummyGame game) {
        this.ginMode = ginMode;
        this.uiInteraction = new HumanUIInteractionImpl(game);
    }

    @Override
    public void takeTurn(Player player, TablePort table) {

        if (ginMode) {
            takeTurnGin(player, table);
        } else {
            takeTurnClassic(player, table);
        }
    }


    private void takeTurnClassic(Player player, TablePort table) {
        // 1. Prompt player to select draw source
        if (table.peekDiscardTop() == null || table.isDiscardEmpty()) {
            // Discard pile empty, can only draw from stock
            uiInteraction.game.setStatusText("Player " + player.getId() + " - Double click stockpile to draw");
        } else {
            // Discard pile not empty, can choose
            uiInteraction.game.setStatusText("Player " + player.getId() + " - Double click a pile to draw");
        }

        // 2. Wait for player to select draw source
        Card drawnCard = uiInteraction.selectPickupSource(table);

        if (drawnCard != null) {
            // Add drawn card to hand
            Hand hand = table.getHand(player);
            hand.insert(drawnCard, false);
            table.sortHand(player);
            hand.draw();
        }

        // 3. Prompt player to select card to discard
        uiInteraction.game.setStatusText("Player " + player.getId() + " - Double click a card to discard");
        Hand hand = table.getHand(player);
        Card cardToDiscard = uiInteraction.selectDiscardCard(hand, drawnCard);

        // 4. Discard selected card
        if (cardToDiscard != null) {
            table.discard(player, cardToDiscard);
            hand.draw();
        }

        // 5. Prompt player to choose whether to declare Rummy
        uiInteraction.game.setStatusText("Player " + player.getId() + " - Click End Turn or declare RUMMY");
        boolean shouldDeclare = uiInteraction.shouldDeclareRummy(hand);

        if (shouldDeclare) {
            // Player clicked RUMMY button -> End entire round and score
            table.endRound(player, "RUMMY");
        } else {
            // Player clicked End Turn button -> Just normally end own turn, game continues
        }
    }


    private void takeTurnGin(Player player, TablePort table) {
        // 1. Prompt player to select draw source
        if (table.peekDiscardTop() == null || table.isDiscardEmpty()) {
            // Discard pile empty, can only draw from stock
            uiInteraction.game.setStatusText("Player " + player.getId() + " - Double click stockpile to draw");
        } else {
            // Discard pile not empty, can choose
            uiInteraction.game.setStatusText("Player " + player.getId() + " - Double click a pile to draw");
        }

        // 2. Wait for player to select draw source
        Card drawnCard = uiInteraction.selectPickupSource(table);

        if (drawnCard != null) {
            // Add drawn card to hand
            Hand hand = table.getHand(player);
            hand.insert(drawnCard, false);
            table.sortHand(player);
            hand.draw();
        }

        // 3. Prompt player to select card to discard
        uiInteraction.game.setStatusText("Player " + player.getId() + " - Double click a card to discard");
        Hand hand = table.getHand(player);
        Card cardToDiscard = uiInteraction.selectDiscardCard(hand, drawnCard);

        // 4. Discard selected card
        if (cardToDiscard != null) {
            table.discard(player, cardToDiscard);
            hand.draw();
        }

        // 5. Prompt player to choose whether to declare Gin or Knock
        uiInteraction.game.setStatusText("Player " + player.getId() + " - Click End Turn or declare GIN/KNOCK");
        String declareAction = uiInteraction.waitForDeclareOrEnd(hand);

        if (declareAction != null) {
            // Player clicked GIN or KNOCK button -> End entire round and score
            table.endRound(player, declareAction);
        } else {
            // Player clicked End Turn button -> Just normally end own turn, game continues
            // Don't call endRound(), let game loop continue to next player
        }
    }
}
