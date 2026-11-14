package rummy.meld;

import ch.aplu.jcardgame.Card;
import java.util.List;

public interface MeldSelector {
    /** Select the non-overlapping and optimal combination from the handCards */
    List<List<Card>> select(List<List<Card>> allMelds, List<Card> handCards);
}