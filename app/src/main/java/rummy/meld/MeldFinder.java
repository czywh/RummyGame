package rummy.meld;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.*;

public class MeldFinder {
    private final MeldEnumerator enumerator;
    private final MeldSelector selector;

    /** Enumerator: run+set；
     * Backtracking Selector ；deadwood ）
     * */
    public MeldFinder() {
        this.enumerator = new RunAndSetEnumerator(3, true);
        this.selector   = new BacktrackingSelector();
    }

    public Meld detectBestMeldAndDeadwood(Hand hand){
        List<Card> allCards = new ArrayList<>(hand.getCardList());

        // 1. find all possible combination (Set & Run)
        List<List<Card>> candidates = enumerator.enumerate(allCards);

        // 2. chose non-overlapping combination
        List<List<Card>> best = selector.select(candidates, allCards);

        // 3. Calculate deadwood
        List<Card> dw = computeDeadwood(allCards, best);

        return new Meld(best, dw);
    }


    /**
     * Calculate the deadwood based on the selected meld.
     * */
    private List<Card> computeDeadwood(List<Card> all, Collection<List<Card>> chosen){
        Set<Card> used = new HashSet<>();
        List<Card> dw = new ArrayList<>();

        for (List<Card> m : chosen) used.addAll(m);

        for (Card c : all) {
            if (!used.contains(c)) dw.add(c);
        }

        return dw;
    }

    /**
     * Check if current hand is form all meld
     * */
    public static boolean isAllMelds(List<Card> hand) {
        MeldFinder finder = new MeldFinder();
        Hand tempHand = new Hand(null);

        for (Card c : hand) tempHand.insert(c, false);
        Meld meld = finder.detectBestMeldAndDeadwood(tempHand);

        return meld.getDeadwood().isEmpty();
    }
}