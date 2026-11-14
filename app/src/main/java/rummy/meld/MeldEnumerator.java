package rummy.meld;

import ch.aplu.jcardgame.Card;
import java.util.List;

public interface MeldEnumerator {
    /** Based on the entire hand cards, list all possible melds  */
    List<List<Card>> enumerate(List<Card> cards);
}