package rummy.ai.evaluationStrategy.compositeEvaluation;

import ch.aplu.jcardgame.Card;
import rummy.engine.TablePort;
import rummy.model.Player;
import rummy.Rank;
import rummy.Suit;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Criterion 1: If this card can currently form a meld, then high score.
 */
public class Criterion1 implements EvaluationCriterion {
    @Override
    public boolean test(Player player, List<Card> hand, Card c, TablePort t) {
        return canFormMeldWith(c, hand);
    }

    private boolean canFormMeldWith(Card drawnCard, List<Card> handCards) {
        // Collect cards with same suit or rank as drawnCard
        List<Card> sameRank = new ArrayList<>();
        List<Card> sameSuit = new ArrayList<>();

        // Classification: collect cards with same rank and same suit
        for (Card card : handCards) {
            Rank cRank = (Rank) card.getRank();
            Rank drawnRank = (Rank) drawnCard.getRank();
            if (cRank.getShortHandValue() == drawnRank.getShortHandValue()) {
                sameRank.add(card);
            }

            Suit cSuit = (Suit) card.getSuit();
            Suit drawnSuit = (Suit) drawnCard.getSuit();
            if (cSuit.getSuitShortHand().equals(drawnSuit.getSuitShortHand())) {
                sameSuit.add(card);
            }
        }

        // Check if can form set with two or more cards of same rank (at least three cards)
        if (sameRank.size() >= 2) {
            return true;
        }

        // Check if can form run with other cards (same suit consecutive three cards)
        sameSuit.add(drawnCard); // Add drawnCard to try forming run

        if (sameSuit.size() >= 3) {
            // Extract all different ranks and sort
            // Put all cards in sameSuit into ranks, convert cards to corresponding rank numbers
            List<Integer> ranks = new ArrayList<>();
            for (Card card : sameSuit) {
                Rank cRank = (Rank) card.getRank();
                int r = cRank.getShortHandValue();
                if (!ranks.contains(r)) {
                    ranks.add(r);
                }
            }
            // Then sort directly
            Collections.sort(ranks);

            // Get drawnCard's rank value
            int drawnRank = ((Rank) drawnCard.getRank()).getShortHandValue();

            // Consecutive three cards and must include drawnCard's rank
            for (int i = 0; i <= ranks.size() - 3; i++) {
                int r1 = ranks.get(i);
                int r2 = ranks.get(i + 1);
                int r3 = ranks.get(i + 2);

                if (r2 == r1 + 1 && r3 == r2 + 1) {
                    // Only when drawnCard participates in this straight, it meets Criterion 1
                    if (drawnRank == r1 || drawnRank == r2 || drawnRank == r3) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
