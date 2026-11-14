package rummy.ai.discardStrategy;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import rummy.ai.evaluationStrategy.compositeEvaluation.*;
import rummy.ai.evaluationStrategy.discardEvaluation.*;
import rummy.engine.TablePort;
import rummy.meld.Meld;
import rummy.meld.MeldFinder;
import rummy.model.Player;

import java.util.*;

/**
 * Smart mode discard strategy
 */
public class SmartDiscardPolicy implements DiscardPolicy {

    private final CompositeEvaluation evaluator = new CompositeEvaluation();

    // discard decision chain: default "Rare suit → High points → Random"
    private final DiscardEvaluation discardEvaluation = new CompositeDiscardStrategy();


    @Override
    public void discard(Player player, TablePort table, Card drawn) {
        Hand hand = table.getHand(player);

        // Ensure always discard to 13 cards (regardless of whether drawn is null)
        if (hand.getNumberOfCards() == 13) {
            return; // Already 13 cards, no need to discard
        }

        Card cardToDiscard = chooseDiscard(player, hand, table);
        table.discard(player, cardToDiscard);
    }

    /** Step 3: Select card to discard based on "composite evaluation + discard evaluation" */
    private Card chooseDiscard(Player player, Hand hand, TablePort table) {
        // 1. Calculate deadwoods (candidates only from deadwood)
        MeldFinder mf = new MeldFinder();
        Meld best = mf.detectBestMeldAndDeadwood(hand);
        List<Card> deadwoods = best.getDeadwood();

        if (deadwoods == null || deadwoods.isEmpty()) {
            // If no deadwood, randomly select one
            List<Card> handCards = hand.getCardList();
            if (handCards.isEmpty()) return null;
            return handCards.get(new Random().nextInt(handCards.size()));
        }

        // 2. Score each deadwood candidate:
        Map<Card, Integer> scoreMap = new HashMap<>();
        int minScore = Integer.MAX_VALUE;

        for (Card c : deadwoods) {
            // Construct hand without c
            List<Card> handWithoutC = new ArrayList<>();
            for (Card card : hand.getCardList()) {
                if (card != c) {
                    handWithoutC.add(card);
                }
            }

            // Evaluate c's number of criteria satisfied
            int score = evaluator.countSatisfiedForContext(handWithoutC, c);
            scoreMap.put(c, score);
            if (score < minScore) minScore = score;
        }

        // 3. Take candidate set with minimum score
        List<Card> candidates = new ArrayList<>();
        for (Map.Entry<Card, Integer> e : scoreMap.entrySet()) {
            if (e.getValue() == minScore) candidates.add(e.getKey());
        }

        // 4. discard Evaluation: Rare suit → High points → Random
        if (candidates.size() > 1) {
            candidates = discardEvaluation.decideDiscard(candidates, player, hand, table);
        }
        // At least one card at this point
        return candidates.get(0);
    }
}