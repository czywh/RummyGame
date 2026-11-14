package rummy.rules;

import ch.aplu.jcardgame.Card;
import rummy.Rank;

public class RuleGinDeadwood implements ScoreRule{

    @Override
    public int getScore(ScoreContext ctx) {
        int penalty = 0;
        for (Card c : ctx.deadwood) {
            Rank rank = (Rank) c.getRank();
            penalty += rank.getPointValue();
        }
        return -penalty;
    }
}
