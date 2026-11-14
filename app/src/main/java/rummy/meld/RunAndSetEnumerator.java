package rummy.meld;

import ch.aplu.jcardgame.Card;
import rummy.Rank;
import rummy.Suit;

import java.util.*;

/** Responsible for enumerating all possible combinations of runs and sets in a single hand of cards
 * - The output of this type is all the "candidate combinations that could form a meld".
 * - Subsequently, the BacktrackingSelector selects the optimal and non-overlapping combination among them.
 * */
public final class RunAndSetEnumerator implements MeldEnumerator {
    private final int minRun;
    private final boolean allowFourSet;

    public RunAndSetEnumerator(int minRun, boolean allowFourSet) {
        this.minRun = minRun;
        this.allowFourSet = allowFourSet;
    }

    @Override
    public List<List<Card>> enumerate(List<Card> cards) {
        List<List<Card>> out = new ArrayList<>();
        out.addAll(findAllRuns(cards));
        out.addAll(findAllSets(cards));
        return out;
    }

    /**
     * Find all the candidate sequences of the same suit and consecutive point values.
     * */
    private List<List<Card>> findAllRuns(List<Card> cards) {
        Map<Suit, List<Card>> suitMap = new HashMap<>();

        // Classified by suit: Each Suit corresponds to a set of cards
        for (Card c : cards) {
            Suit s = (Suit) c.getSuit();
            suitMap.computeIfAbsent(s, k -> new ArrayList<>()).add(c);
        }

        List<List<Card>> runs = new ArrayList<>();

        //  detection for each suit group
        for (List<Card> group : suitMap.values()) {

            // Sort by the face value (in ascending order)
            group.sort(Comparator.comparingInt(c -> ((Rank) c.getRank()).getShortHandValue()));

            // Starting from each card, attempt to expand the consecutive sequence backward
            for (int i = 0; i < group.size(); i++) {
                List<Card> cur = new ArrayList<>();
                cur.add(group.get(i));

                int last = ((Rank) group.get(i).getRank()).getShortHandValue();

                for (int j = i + 1; j < group.size(); j++) {

                    int next = ((Rank) group.get(j).getRank()).getShortHandValue();

                    if (next == last + 1) {
                        cur.add(group.get(j));
                        last = next;

                        if (cur.size() >= minRun)
                            runs.add(new ArrayList<>(cur));

                    } else if (next == last) {
                        //
                        continue;
                    } else {
                        break;
                    }
                }
            }
        }
        return runs;
    }

    /**
     * find all the candidate stones that have the same number of value.
     * */
    private List<List<Card>> findAllSets(List<Card> cards) {
        Map<Rank, List<Card>> rankMap = new HashMap<>();

        // Classified by point value, each Rank corresponds to a set of cards
        for (Card c : cards) {
            Rank r = (Rank) c.getRank();
            rankMap.computeIfAbsent(r, k -> new ArrayList<>()).add(c);
        }

        List<List<Card>> sets = new ArrayList<>();

        // detection for each Rank group
        for (List<Card> group : rankMap.values()) {

            // add card to group as long as size >= 3
            if (group.size() >= 3) {
                sets.add(new ArrayList<>(group.subList(0, 3)));

                if (allowFourSet && group.size() >= 4) {
                    sets.add(new ArrayList<>(group.subList(0, 4)));
                }
            }
        }
        return sets;
    }
}