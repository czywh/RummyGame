package rummy.rules;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import rummy.Rank;
import rummy.meld.Meld;
import rummy.meld.MeldFinder;

import java.util.List;

/**
 * Handle scoring logic at round end
 */
public class RuleRoundEndScoring {

    /**
     * Calculate round score (for Classic Rummy and Gin Rummy)
     */
    public static int calculateRoundScore(Hand hand0, Hand hand1, int[] scores) {
        MeldFinder meldFinder = new MeldFinder();

        // Extract player 0's best meld and deadwood
        Meld m0 = meldFinder.detectBestMeldAndDeadwood(hand0);
        List<Card> deadwood0 = m0.getDeadwood();

        // Extract player 1's best meld and deadwood
        Meld m1 = meldFinder.detectBestMeldAndDeadwood(hand1);
        List<Card> deadwood1 = m1.getDeadwood();

        // Calculate each player's total deadwood points
        int points0 = calculateDeadwoodPoints(deadwood0);
        int points1 = calculateDeadwoodPoints(deadwood1);

        // Compare values, determine scoring
        if (points0 < points1) {
            scores[0] += points1;  // Player 0 wins, gets player 1's score
            return 0;
        } else if (points0 > points1) {
            scores[1] += points0;  // Player 1 wins, gets player 0's score
            return 1;
        } else {
            // Tie, nothing happens
            return -1;
        }
    }

    /**
     * Calculate Knock score (Gin Rummy specific)
     */
    public static int calculateKnock(Hand myHand, Hand opponentHand, int currPlayerIndex, int[] scores) {
        MeldFinder meldFinder = new MeldFinder();

        // Extract knocking player's best meld and deadwood
        Meld m0 = meldFinder.detectBestMeldAndDeadwood(myHand);
        List<Card> deadwood0 = m0.getDeadwood();

        // Extract opponent's best meld and deadwood
        Meld m1 = meldFinder.detectBestMeldAndDeadwood(opponentHand);
        List<Card> deadwood1 = m1.getDeadwood();

        // Calculate each player's total deadwood points
        int points0 = calculateDeadwoodPoints(deadwood0);
        int points1 = calculateDeadwoodPoints(deadwood1);

        int diff = Math.abs(points0 - points1);
        int opponentIndex = (currPlayerIndex + 1) % 2; // Assume 2-player game
        int winner = -1;

        if (points0 < points1) {
            scores[currPlayerIndex] += diff;  // Knocking player wins
            winner = currPlayerIndex;
        } else if (points0 > points1) {
            scores[opponentIndex] += diff;  // Opponent wins
            winner = opponentIndex;
        } else {
            // Tie, nothing happens
            winner = -1;
        }
        return winner;
    }

    /**
     * Calculate deadwood card points
     */
    private static int calculateDeadwoodPoints(List<Card> deadwood) {
        int total = 0;
        for (Card c : deadwood) {
            Rank rank = (Rank) c.getRank();
            total += rank.getPointValue();
        }
        return total;
    }
}

