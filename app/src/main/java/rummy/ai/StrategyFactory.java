package rummy.ai;

import rummy.ai.pickupStrategry.*;
import rummy.ai.discardStrategy.*;
import rummy.ai.declareStrategy.*;
import rummy.ai.evaluationStrategy.compositeEvaluation.CompositeEvaluation;
import rummy.ai.evaluationStrategy.EvaluationPolicy;
import rummy.ai.evaluationStrategy.NullEvaluationPolicy;

import rummy.engine.GameMode;


/**
 * StrategyFactory:
 * - Responsible for creating complete ComputerStrategy (Pickup + Evaluate + Discard + Declare)
 */
public final class StrategyFactory {

    private StrategyFactory() {}

    public static ComputerStrategy build(AIMode aiMode, GameMode gameMode) {
        boolean smart = (aiMode == AIMode.SMART);
        boolean gin = (gameMode == GameMode.GIN);


        // chose corresponding policies
        EvaluationPolicy evaluation = smart ? new CompositeEvaluation() : new NullEvaluationPolicy();
        PickupPolicy pickup = smart ? new SmartPickupPolicy(evaluation) : new RandomPickupPolicy();
        DiscardPolicy discard = smart ? new SmartDiscardPolicy() : new RandomDiscardPolicy();

        DeclarePolicy declare = smart
                ? (gin ? new SmartGinDeclarePolicy() : new SmartClassicDeclarePolicy())
                : (gin ? new RandomGinDeclarePolicy() : new RandomDeclarePolicy());


        return new ComputerStrategy(pickup, evaluation, discard, declare);
    }
}