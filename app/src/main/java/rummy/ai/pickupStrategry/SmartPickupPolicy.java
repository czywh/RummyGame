package rummy.ai.pickupStrategry;

import ch.aplu.jcardgame.Card;
import rummy.ai.evaluationStrategy.compositeEvaluation.CompositeEvaluation;
import rummy.ai.evaluationStrategy.EvaluationPolicy;
import rummy.engine.TablePort;
import rummy.model.Player;

/**
 * Smart AI pickup strategy (Strategy Pattern)
 */
public class SmartPickupPolicy implements PickupPolicy {
    private final EvaluationPolicy evaluation;


    public SmartPickupPolicy(EvaluationPolicy evaluation) {
        this.evaluation = evaluation;
    }

    @Override
    public Card pick(Player player, TablePort table) {
        // First check discard pile top
        Card discardTop = table.peekDiscardTop();

        if (discardTop != null) {
            boolean useful = evaluation.evaluate(player, discardTop, table);
            if (useful) {
                return table.drawFromDiscard(player);
            }
        }

        // Draw from stockpile
        Card stockCard = table.drawFromStock(player);


        return stockCard;
    }

}