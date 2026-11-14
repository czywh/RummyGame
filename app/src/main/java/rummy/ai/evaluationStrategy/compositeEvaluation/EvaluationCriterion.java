package rummy.ai.evaluationStrategy.compositeEvaluation;

import ch.aplu.jcardgame.Card;
import rummy.engine.TablePort;
import rummy.model.Player;

import java.util.List;


public interface EvaluationCriterion {
    boolean test(Player player, List<Card> currentHand, Card candidate, TablePort table);
}