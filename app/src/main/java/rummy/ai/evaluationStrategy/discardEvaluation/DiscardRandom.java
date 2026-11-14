package rummy.ai.evaluationStrategy.discardEvaluation;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import rummy.engine.TablePort;
import rummy.model.Player;

import java.util.*;

public class DiscardRandom implements DiscardEvaluation{
    private final Random rng = new Random();

    @Override
    public List<Card> decideDiscard(List<Card> candidates, Player player, Hand hand, TablePort table) {
        if (candidates.size() <= 1) return candidates;
        Card pick = candidates.get(rng.nextInt(candidates.size()));
        return Collections.singletonList(pick);
    }
}
