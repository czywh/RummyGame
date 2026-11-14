package rummy.engine;

import rummy.rules.ScoreRule;
import java.util.Properties;

public class GinRummy extends RummyGame {

    public GinRummy(Properties properties, ScoreRule scoring) {
        super(properties, scoring);
    }

    @Override
    protected int getDefaultStartCards() {
        return 10;
    }
}
