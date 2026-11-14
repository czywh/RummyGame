package rummy.ai.evaluationStrategy.discardEvaluation;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import rummy.engine.TablePort;
import rummy.model.Player;

import java.util.List;


public interface DiscardEvaluation {
    List<Card> decideDiscard(List<Card> candidates, Player player, Hand fullHand, TablePort table);
}
