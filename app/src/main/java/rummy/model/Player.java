package rummy.model;

import ch.aplu.jcardgame.Hand;

/**
 * Rummy player object.
 * Supports human/computer players, holds respective hands and scores.
 */
public class Player {
    private final int id;
    private final String name;
    private final boolean human;
    private final Hand hand;
    private int totalScore = 0;


    public Player(int id, String name, boolean human, Hand hand) {
        this.id = id;
        this.name = name;
        this.human = human;
        this.hand = hand;
    }


    public Player(int id, String name, boolean human) {
        this(id, name, human, null);
    }

    // ---------------- Getter ----------------
    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isHuman() { return human; }
    public Hand getHand() { return hand; }

    // ---------------- Score ----------------
    public int getTotalScore() { return totalScore; }
    public void addScore(int delta) { totalScore += delta; }
    public void resetScore() { totalScore = 0; }

    // ---------------- Utility ----------------
    @Override
    public String toString() {
        return String.format("%s[%s] (id=%d, score=%d)",
                name, human ? "Human" : "AI", id, totalScore);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player other = (Player) o;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}