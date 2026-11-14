package rummy.meld;

import ch.aplu.jcardgame.Card;
import rummy.Rank;

import java.util.*;

/**
 * Use the backtracking algorithm to select the optimal combination of non-overlapping melds from all candidates.
 */
public final class BacktrackingSelector implements MeldSelector {

    @Override
    public List<List<Card>> select(List<List<Card>> allMelds, List<Card> handCards) {
        List<List<Card>> best = new ArrayList<>();

        backtrack(allMelds, 0, new ArrayList<>(), new HashSet<>(), best, handCards);

        return best;
    }

    /**
     * Recursively find the better combination of cards
     * */
    private void backtrack( List<List<Card>> all, int idx, List<List<Card>> cur, Set<Card> used,  List<List<Card>> best, List<Card> hand)
    {

        // all cards have been iterated
        if (idx >= all.size()) {

            // repace cards combination with better option
            if (better(cur, best, hand) < 0) {
                best.clear();
                for (List<Card> m : cur) best.add(new ArrayList<>(m));
            }
            return;
        }

        List<Card> m = all.get(idx);

        boolean ok = true;
        for (Card c : m) if (used.contains(c)) { ok = false; break; }

        if (ok) {
            cur.add(m); used.addAll(m);
            backtrack(all, idx + 1, cur, used, best, hand);
            used.removeAll(m); cur.remove(cur.size() - 1);
        }

        backtrack(all, idx + 1, cur, used, best, hand);
    }

    /** Chose Cards Combination */
    private int better(List<List<Card>> a, List<List<Card>> b, List<Card> hand) {
        int sizeA = a.stream().mapToInt(List::size).sum();
        int sizeB = b.stream().mapToInt(List::size).sum();

        // choose option with larger card size
        if (sizeA != sizeB) return Integer.compare(sizeB, sizeA);

        int da = deadwoodSum(hand, a);
        int db = deadwoodSum(hand, b);

        // choos smaller deadwood
        if (da != db) return Integer.compare(da, db);

        // randomly choose
        return (Math.random() < 0.5) ? -1 : 1;
    }

    private int deadwoodSum(List<Card> hand, List<List<Card>> melds) {
        Set<Card> used = new HashSet<>();
        for (List<Card> m : melds) used.addAll(m);
        int sum = 0;
        for (Card c : hand)
            if (!used.contains(c)) sum += ((Rank) c.getRank()).getPointValue();
        return sum;
    }
}