package rummy.ai.discardStrategy;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import rummy.engine.TablePort;
import rummy.model.Player;

import java.util.List;
import java.util.Random;

public class RandomDiscardPolicy implements DiscardPolicy {
    private final Random random = new Random();

    @Override
    public void discard(Player player, TablePort table, Card drawn) {
        Hand hand = table.getHand(player);
        Card cardToDiscard = getRandomCard(hand);

        // Use TablePort's discard method to discard selected card
        table.discard(player, cardToDiscard);
    }

    private Card getRandomCard(Hand hand) {
        List<Card> cards = hand.getCardList();
        if (cards.isEmpty()) {
            return null;
        }
        int x = random.nextInt(cards.size());
        return cards.get(x);
    }
}
