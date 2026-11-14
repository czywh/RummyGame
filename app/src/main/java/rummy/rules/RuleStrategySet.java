package rummy.rules;

import ch.aplu.jcardgame.Card;
import rummy.Rank;
import rummy.meld.MeldFinder;
import rummy.meld.Meld;

import java.util.List;

public class RuleStrategySet implements ScoreRule{

    @Override
    public int getScore(ScoreContext ctx) {
        int score = 0;

        // Use MeldFinder to detect same-rank card groups
        MeldFinder meldFinder = new MeldFinder();
        Meld meld = meldFinder.detectBestMeldAndDeadwood(ctx.hand);
        List<List<Card>> bestMelds = meld.getBestMelds();

        // Calculate same-rank card group scores
        for (List<Card> meldGroup : bestMelds) {
            if (isSet(meldGroup)) {
                // Same-rank card group score is sum of all card points
                for (Card card : meldGroup) {
                    Rank rank = (Rank) card.getRank();
                    score += rank.getPointValue();
                }
            }
        }

        return score;
    }

    /**
     * Check if it's a same-rank card group
     */
    private boolean isSet(List<Card> cards) {
        if (cards.size() < 3) return false;

        // Check if same rank
        var firstRank = cards.get(0).getRank();
        for (Card card : cards) {
            if (!card.getRank().equals(firstRank)) {
                return false;
            }
        }

        return true;
    }
}
