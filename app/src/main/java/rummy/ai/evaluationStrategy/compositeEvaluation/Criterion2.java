package rummy.ai.evaluationStrategy.compositeEvaluation;

import ch.aplu.jcardgame.Card;
import rummy.engine.TablePort;
import rummy.model.Player;
import rummy.Rank;
import rummy.Suit;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Criterion2 implements EvaluationCriterion {

    /**
     * Criterion 2: Whether this card can reduce the gap in same-suit sequence (closer to straight).
     */
    @Override
    public boolean test(Player player, List<Card> hand, Card c, TablePort t) {
        return decreasesMinGap(c, hand);
    }

    private boolean decreasesMinGap(Card drawnCard, List<Card> handCards) {
        Suit suit = (Suit) drawnCard.getSuit();
        List<Integer> ranks = new ArrayList<>();

        // Traverse each card in hand
        for (Card card : handCards) {
            Suit cSuit = (Suit) card.getSuit();
            // If current hand card has same suit as drawn card, add to ranks
            if (suit.getSuitShortHand().equals(cSuit.getSuitShortHand())) {
                Rank cRank = (Rank) card.getRank();
                ranks.add(cRank.getShortHandValue());
            }
        }

        // If no hand cards have same suit as drawn, return false directly
        if (ranks.isEmpty()) return false;

        // If only one hand card has same suit as drawn, return true directly
        if (ranks.size() == 1) return true;

        // Two or more cards case
        // Calculate original gap
        Collections.sort(ranks);
        int minGapBefore = Integer.MAX_VALUE;
        for (int i = 1; i < ranks.size(); i++) {
            minGapBefore = Math.min(minGapBefore, ranks.get(i) - ranks.get(i - 1) - 1);
        }

        // Calculate gap after adding drawnCard
        Rank drawnRank = (Rank) drawnCard.getRank();
        ranks.add(drawnRank.getShortHandValue());
        Collections.sort(ranks);
        int minGapAfter = Integer.MAX_VALUE;
        for (int i = 1; i < ranks.size(); i++) {
            minGapAfter = Math.min(minGapAfter, ranks.get(i) - ranks.get(i - 1) - 1);
        }

        // Check if there are consecutive three cards including drawnCard
        for (int i = 0; i <= ranks.size() - 3; i++) {
            int r1 = ranks.get(i);
            int r2 = ranks.get(i + 1);
            int r3 = ranks.get(i + 2);

            if (r2 == r1 + 1 && r3 == r2 + 1) {
                // Only when drawnCard participates in this straight, it meets Criterion 1
                if (drawnRank.getShortHandValue() == r1 || drawnRank.getShortHandValue() == r2 || drawnRank.getShortHandValue() == r3) {
                    return true;
                }
            }
        }

        return minGapAfter < minGapBefore;
    }
}
