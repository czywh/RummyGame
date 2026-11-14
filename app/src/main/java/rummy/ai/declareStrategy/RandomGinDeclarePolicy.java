package rummy.ai.declareStrategy;

import rummy.engine.TablePort;
import rummy.model.Player;
import rummy.meld.MeldFinder;

import java.util.List;

/**
 * Random Gin Rummy Declare Policy
 */
public class RandomGinDeclarePolicy implements DeclarePolicy {

    @Override
    public void tryDeclare(Player player, TablePort table) {

        List<?> handCards = table.getHand(player).getCardList();

        // Only declare Gin when "all hand cards happen to be covered by melds"
        boolean allMelds = MeldFinder.isAllMelds((List)handCards);

        if (allMelds) {
            // Gin Rummy: declare Gin
            table.endRound(player, "GIN");
        }
        // Otherwise do nothing
    }
}
