package rummy.ai.evaluationStrategy;


import ch.aplu.jcardgame.Card;
import rummy.engine.TablePort;
import rummy.model.Player;

public interface EvaluationPolicy {
    /** Evaluate based on drawn card and current hand;
     * returns "whether useful"
     * */
    boolean evaluate(Player player, Card drawn, TablePort table);
}