package rummy.ai.evaluationStrategy.compositeEvaluation;

import ch.aplu.jcardgame.Card;
import rummy.engine.TablePort;
import rummy.model.Player;
import rummy.Suit;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Criterion3 implements EvaluationCriterion {

    /**
     * Criterion 3: Whether this card increases the count of this suit (improves straight potential).
     */
    @Override
    public boolean test(Player player, List<Card> hand, Card c, TablePort t) {
        return increasesMaxSuitCount(c, hand);
    }

    private boolean increasesMaxSuitCount(Card drawnCard, List<Card> handCards) {
        // First without drawn, count how many cards of each suit in original hand
        Map<Suit, Integer> suitCounts = new HashMap<>();
        for (Card card : handCards) {
            Suit cSuit = (Suit) card.getSuit();
            suitCounts.put(cSuit, suitCounts.getOrDefault(cSuit, 0) + 1);
        }

        // Find the one with most cards
        int maxBefore = -1;
        for (int count : suitCounts.values()) {
            if (count > maxBefore) {
                maxBefore = count;
            }
        }

        // Add drawn to corresponding suit
        Suit drawnSuit = (Suit) drawnCard.getSuit();
        // Its corresponding suit might not have been added to map, so need this
        int countDrawnSuit = suitCounts.getOrDefault(drawnSuit, 0) + 1;

        return countDrawnSuit > maxBefore;
    }
}
