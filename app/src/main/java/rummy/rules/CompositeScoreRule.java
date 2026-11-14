package rummy.rules;
import java.util.*;

public class CompositeScoreRule implements ScoreRule{

    private final List<ScoreRule> rules = new ArrayList<>();

    /* Adding rules to this compositions */
    public CompositeScoreRule add(ScoreRule rule) {
        rules.add(rule);
        return this;
    }

    @Override
    public int getScore(ScoreContext ctx) {
        int total = 0;
        for (ScoreRule rule : rules) {
            total += rule.getScore(ctx);
        }
        return total;
    }
}