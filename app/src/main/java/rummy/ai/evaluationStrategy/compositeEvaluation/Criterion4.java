package rummy.ai.evaluationStrategy.compositeEvaluation;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import rummy.engine.TablePort;
import rummy.model.Player;
import rummy.Rank;
import rummy.meld.Meld;
import rummy.meld.MeldFinder;

import java.util.List;

public class Criterion4 implements EvaluationCriterion {

    /**
     * Criterion 4: If this card can form a pair (same points) in deadwood,
     */
    @Override
    public boolean test(Player player, List<Card> hand, Card c, TablePort t) {
        return formsDeadwoodPair(c, hand, t);
    }

    private boolean formsDeadwoodPair(Card drawnCard, List<Card> handCards, TablePort table) {
        // Need to create a temporary Hand for MeldFinder analysis
        if (table == null) return false;

        // Construct hand containing drawnCard
        Hand tempHand = new Hand(null);
        for (Card card : handCards) {
            tempHand.insert(card.clone(), false);
        }
        tempHand.insert(drawnCard.clone(), false);

        // Find deadwood in original hand
        MeldFinder meldFinder = new MeldFinder();
        Meld m = meldFinder.detectBestMeldAndDeadwood(tempHand);
        List<Card> deadwood = m.getDeadwood();

        // Whether matches rank in deadwood
        for (Card card : deadwood) {
            Rank cRank = (Rank) card.getRank();
            Rank drawnRank = (Rank) drawnCard.getRank();
            if (cRank.getShortHandValue() == drawnRank.getShortHandValue()) {
                return true;
            }
        }

        return false;
    }
}
