package rummy.meld;

import ch.aplu.jcardgame.Card;

import java.util.Collections;
import java.util.List;

public class Meld {
    private final List<List<Card>> bestMelds;
    private final List<Card> deadwood;

    public Meld(List<List<Card>> bestMelds, List<Card> deadwoods) {
        this.bestMelds = bestMelds;
        this.deadwood = deadwoods;
    }

    public List<List<Card>> getBestMelds() {
        return Collections.unmodifiableList(bestMelds);
    }

    public List<Card> getDeadwood() {
        return Collections.unmodifiableList(deadwood);
    }
}