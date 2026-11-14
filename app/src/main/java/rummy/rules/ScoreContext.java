package rummy.rules;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.*;

public class ScoreContext {
    public boolean isGinMode;
    public Hand hand;
    public final List<Card> deadwood;

    public ScoreContext(Hand hand, List<Card> deadwood, boolean isGinMode){
        this.hand = hand;
        this.isGinMode = isGinMode;
        this.deadwood = deadwood;
    }
}