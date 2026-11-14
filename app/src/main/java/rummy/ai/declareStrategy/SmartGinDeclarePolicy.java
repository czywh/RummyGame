package rummy.ai.declareStrategy;

import rummy.engine.TablePort;
import rummy.model.Player;
import rummy.meld.Meld;
import rummy.meld.MeldFinder;
import rummy.Rank;

import java.util.List;

/**
 * Smart Gin Rummy Declare Policy:
 * Check if can declare Gin (all cards can form melds)
 * Check if can Knock (deadwood <= 10)
 */
public class SmartGinDeclarePolicy implements DeclarePolicy {

    @Override
    public void tryDeclare(Player player, TablePort table) {
        List<?> handCards = table.getHand(player).getCardList();
        boolean allMelds = MeldFinder.isAllMelds((List)handCards);

        if (allMelds) {
            // Gin Rummy: declare Gin
            table.endRound(player, "GIN");
        } else {
            // Check if can Knock (deadwood <= 10)
            if (canKnock(player, table)) {
                // Declare Knock
                table.endRound(player, "KNOCK");
            }
        }
        // Otherwise do nothing -> turn automatically ends (upper loop moves to next player)
    }

    private boolean canKnock(Player player, TablePort table) {
        // Use MeldFinder to find best meld combination and deadwood
        MeldFinder meldFinder = new MeldFinder();
        Meld myMeld = meldFinder.detectBestMeldAndDeadwood(table.getHand(player));
        List<?> deadwood = myMeld.getDeadwood();

        // Calculate total deadwood points
        int points = 0;
        for (Object cardObj : deadwood) {
            if (cardObj instanceof ch.aplu.jcardgame.Card) {
                ch.aplu.jcardgame.Card card = (ch.aplu.jcardgame.Card) cardObj;
                Rank rank = (Rank) card.getRank();
                // Use correct point calculation method
                points += rank.getPointValue();
            }
        }

        // Can only Knock when deadwood points <= 10 (Gin Rummy rules)
        return points <= 10;
    }
}
