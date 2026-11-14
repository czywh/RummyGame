package rummy;

public enum Rank implements Comparable<Rank> {
    ACE(1,1), KING(13,10), QUEEN(12,10), JACK(11,10),
    TEN(10,10), NINE(9,9), EIGHT(8,8), SEVEN(7,7), SIX(6,6),
    FIVE(5,5), FOUR(4,4), THREE(3,3), TWO(2,2);

    private int shortHandValue = 0;
    private int pointValue;

    Rank(int shortHandValue, int pointValue) {
        this.shortHandValue = shortHandValue;
        this.pointValue = pointValue;
    }

    public int getShortHandValue() {
        return shortHandValue;
    }

    public String getCardLog() {
        return String.format("%d", shortHandValue);
    }

    public int getPointValue() {
        return pointValue;
    }
}