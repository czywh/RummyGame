package rummy.rules;

import ch.aplu.jcardgame.Card;
import rummy.Rank;
import rummy.meld.MeldFinder;
import rummy.meld.Meld;

import java.util.List;

public class RuleStrategyRun implements ScoreRule {

    @Override
    public int getScore(ScoreContext ctx) {
        int score = 0;

        // Use MeldFinder to detect runs
        MeldFinder meldFinder = new MeldFinder();
        Meld meld = meldFinder.detectBestMeldAndDeadwood(ctx.hand);
        List<List<Card>> bestMelds = meld.getBestMelds();

        // Calculate run scores
        for (List<Card> meldGroup : bestMelds) {
            if (isRun(meldGroup)) {
                // Run score is sum of all card points
                for (Card card : meldGroup) {
                    Rank rank = (Rank) card.getRank();
                    score += rank.getPointValue();
                }
            }
        }

        return score;
    }

    /**
     * Check if it's a run
     */
    private boolean isRun(List<Card> cards) {
        if (cards.size() < 3) return false;

        // Check if same suit
        var firstSuit = cards.get(0).getSuit();
        for (Card card : cards) {
            if (!card.getSuit().equals(firstSuit)) {
                return false;
            }
        }

        // Check if consecutive
        cards.sort((c1, c2) -> {
            Rank r1 = (Rank) c1.getRank();
            Rank r2 = (Rank) c2.getRank();
            return Integer.compare(r1.getShortHandValue(), r2.getShortHandValue());
        });

        for (int i = 1; i < cards.size(); i++) {
            Rank prevRank = (Rank) cards.get(i-1).getRank();
            Rank currRank = (Rank) cards.get(i).getRank();
            if (currRank.getShortHandValue() != prevRank.getShortHandValue() + 1) {
                return false;
            }
        }

        return true;
    }
}
