package rummy.engine;

import rummy.rules.ScoreRule;
import java.util.Properties;

public class ClassicRummy extends RummyGame {

    public ClassicRummy(Properties properties, ScoreRule scoring) {
        super(properties, scoring);
    }

    @Override
    protected int getDefaultStartCards() {
        return 13;
    }
}
