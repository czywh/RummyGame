package rummy.ai.evaluationStrategy.discardEvaluation;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import rummy.engine.TablePort;
import rummy.meld.Meld;
import rummy.meld.MeldFinder;
import rummy.model.Player;

import java.util.*;

public class DiscardRareSuit implements DiscardEvaluation{
    @Override
    public List<Card> decideDiscard(List<Card> cards, Player player, Hand hand, TablePort table) {

        // Calculate suit frequency of deadwood (not entire hand)
        MeldFinder mf = new MeldFinder();
        Meld best = mf.detectBestMeldAndDeadwood(hand);
        List<Card> deadwoods = best.getDeadwood();

        Map<Object, Integer> suitFreq = new HashMap<>();

        // Only calculate frequency of deadwood
        for (Card c : deadwoods) {
            Object s = c.getSuit();
            suitFreq.put(s, suitFreq.getOrDefault(s, 0) + 1);
        }

        int min = Integer.MAX_VALUE;

        List<Card> discards = new ArrayList<>();
        for (Card c : cards) {
            int f = suitFreq.getOrDefault(c.getSuit(), 0);

            if (f < min) {
                discards.clear();
                discards.add(c);
                min = f;
            }
            else if (f == min) discards.add(c);
        }
        return discards;
    }
}
