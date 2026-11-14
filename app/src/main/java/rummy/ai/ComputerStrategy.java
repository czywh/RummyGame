package rummy.ai;


import rummy.ai.declareStrategy.DeclarePolicy;
import rummy.ai.discardStrategy.DiscardPolicy;
import rummy.ai.evaluationStrategy.EvaluationPolicy;
import rummy.ai.pickupStrategry.PickupPolicy;

public class ComputerStrategy {
    private final PickupPolicy pickup;
    private final EvaluationPolicy evaluator;
    private final DiscardPolicy discard;
    private final DeclarePolicy declare;

    public ComputerStrategy(PickupPolicy p, EvaluationPolicy e, DiscardPolicy d, DeclarePolicy c) {
        this.pickup = p;
        this.evaluator = e;
        this.discard = d;
        this.declare = c;
    }

    public PickupPolicy pickup(){ return pickup; }
    public EvaluationPolicy evaluator(){ return evaluator; }
    public DiscardPolicy discard(){ return discard; }
    public DeclarePolicy declare(){ return declare; }
}