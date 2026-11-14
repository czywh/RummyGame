package rummy.ai.evaluationStrategy;


import ch.aplu.jcardgame.Card;
import rummy.engine.TablePort;
import rummy.model.Player;

/** Pure random mode: skip evaluation, always return false */
public class NullEvaluationPolicy implements EvaluationPolicy {
    @Override
    public boolean evaluate(Player player, Card drawn, TablePort table) {
        // No judgment made
        return false;
    }
}