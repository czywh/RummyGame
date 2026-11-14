package rummy.rules;

public class RuleFactory {
    private static final RuleFactory ruleFactory = new RuleFactory();

    public static RuleFactory getRuleFactory() {return ruleFactory;}

    public ScoreRule forClassic() {
        return new CompositeScoreRule()
                .add(new RuleStrategyRun())
                .add(new RuleStrategySet())
                .add(new RuleClassicDeadwood());
    }

    public ScoreRule forGin() {
        return new CompositeScoreRule()
                .add(new RuleStrategyRun())
                .add(new RuleStrategySet())
                .add(new RuleGinDeadwood());
    }
}