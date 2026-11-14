package rummy.ai.evaluationStrategy.discardEvaluation;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import rummy.Rank;
import rummy.engine.TablePort;
import rummy.model.Player;

import java.util.*;


public class DiscardHighRank implements DiscardEvaluation{
    @Override
    public List<Card> decideDiscard(List<Card> cards, Player player, Hand hand, TablePort table) {
        int max = -1;
        List<Card> out = new ArrayList<>();
        for (Card c : cards) {
            int v = ((Rank) c.getRank()).getShortHandValue();

            if (v > max) {
                out.clear();
                out.add(c);
                max = v;
            }
            else if (v == max) out.add(c);
        }
        return out;
    }
}
