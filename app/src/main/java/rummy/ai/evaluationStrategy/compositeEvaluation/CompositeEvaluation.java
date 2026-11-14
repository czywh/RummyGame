package rummy.ai.evaluationStrategy.compositeEvaluation;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import rummy.ai.evaluationStrategy.*;
import rummy.engine.TablePort;
import rummy.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * CompositeEvaluationPolicy - Composite evaluation strategy integrating four evaluation criteria
 */
public class CompositeEvaluation implements EvaluationPolicy {
    private final List<EvaluationCriterion> criteria;

    public CompositeEvaluation( ) {
        this.criteria = new ArrayList<>();

        this.criteria.add(new Criterion1());
        this.criteria.add(new Criterion2());
        this.criteria.add(new Criterion3());
        this.criteria.add(new Criterion4());
    }

    @Override
    public boolean evaluate(Player player, Card drawn, TablePort table) {
        if (drawn == null) return false;

        // Use four evaluation criteria, satisfying any one means useful
        for (EvaluationCriterion criterion : criteria) {
            if (criterion.test(player, table.getHand(player).getCardList(), drawn, table)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Evaluate card's contribution to given hand context (number of criteria satisfied)
     */
    public int countSatisfiedForContext(List<Card> handContext, Card card) {
        if (card == null || handContext == null) return 0;

        int count = 0;
        for (EvaluationCriterion criterion : criteria) {
            if (criterion.test(null, handContext, card, null)) {
                count++;
            }
        }
        return count;
    }
}
