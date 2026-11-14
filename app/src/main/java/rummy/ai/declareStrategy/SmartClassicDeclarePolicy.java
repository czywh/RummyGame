package rummy.ai.declareStrategy;

import ch.aplu.jcardgame.Hand;
import rummy.engine.TablePort;
import rummy.model.Player;
import rummy.meld.Meld;
import rummy.meld.MeldFinder;

import java.util.List;

/**
 * Smart Classic Rummy Declare Policy:
 */
public class SmartClassicDeclarePolicy implements DeclarePolicy {

    @Override
    public void tryDeclare(Player player, TablePort table) {
        if (canDeclare(player, table)) {
            // Classic Rummy: declare Rummy
            table.endRound(player, "RUMMY");
        }
        // Otherwise do nothing
    }

    private boolean canDeclare(Player player, TablePort table) {

        // check if can declare Rummy
        int rummyResult = checkRummy(table.getHand(player), getOpponentHand(player, table));

        // Rummy achieved, add points and return true
        if (rummyResult != -1) {

            return true;
        }
        return false;
    }

    private int checkRummy(Hand myHand, Hand opponentHand) {
        MeldFinder meldFinder = new MeldFinder();
        Meld myMeld = meldFinder.detectBestMeldAndDeadwood(myHand);

        if (!myMeld.getDeadwood().isEmpty()) {
            // has deadwood, not Rummy
            return -1;
        }

        // If Rummy, analyze opponent's deadwood
        Meld opponentMeld = meldFinder.detectBestMeldAndDeadwood(opponentHand);
        List<?> opponentDeadwood = opponentMeld.getDeadwood();

        // Calculate opponent's deadwood points
        int opponentPoints = 0;
        for (Object cardObj : opponentDeadwood) {
            if (cardObj instanceof ch.aplu.jcardgame.Card) {
                ch.aplu.jcardgame.Card card = (ch.aplu.jcardgame.Card) cardObj;
                rummy.Rank rank = (rummy.Rank) card.getRank();
                opponentPoints += rank.getPointValue();
            }
        }

        return opponentPoints;
    }

    private Hand getOpponentHand(Player player, TablePort table) {
        // 2-player game, return other player's hand
        // Player IDs are 0 and 1, opponent ID = (myID + 1) % 2
        int opponentId = (player.getId() + 1) % 2;
        Player opponent = new Player(opponentId, "Opponent", false);
        return table.getHand(opponent);
    }
}