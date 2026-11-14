package rummy.ai.evaluationStrategy.discardEvaluation;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import rummy.engine.TablePort;
import rummy.model.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Composite discard decision (default: Rare suit → High points → Random)
 */
public class CompositeDiscardStrategy implements DiscardEvaluation {

    private final List<DiscardEvaluation> strategies = new ArrayList<>();

    public CompositeDiscardStrategy() {
        strategies.addAll(Arrays.asList(
                new DiscardRareSuit(),
                new DiscardHighRank(),
                new DiscardRandom()
        ));
    }


    public CompositeDiscardStrategy add(DiscardEvaluation strategy) {
        strategies.add(strategy);
        return this;
    }

    @Override
    public List<Card> decideDiscard(List<Card> cards, Player player, Hand hand, TablePort table) {
        List<Card> cur = cards;
        for (DiscardEvaluation s : strategies) {
            if (cur.size() <= 1) break;
            cur = s.decideDiscard(cur, player, hand, table);
        }
        return cur;
    }
}