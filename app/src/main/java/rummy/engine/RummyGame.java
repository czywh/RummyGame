package rummy.engine;

import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import rummy.Rank;
import rummy.Suit;
import rummy.model.Player;
import rummy.rules.ScoreRule;

/**
 * Abstract Rummy game parent class -
 *
 * Responsibilities:
 *  Manage main game loop (initialization -> multiple rounds -> end
 *   Handle UI initialization (buttons, labels, background) and human player interaction
 *   Calculate scores, record logs, determine game/round end conditions
 */

public abstract class RummyGame extends CardGame {

    protected final Properties props;
    protected final ScoreRule scoring;
    public final List<Player> players = new ArrayList<>();
    protected final List<TurnController> controllers = new ArrayList<>();

    protected final Deck deck = new Deck(Suit.values(), Rank.values(), "cover");
    public TablePort table;
    protected int nbStartCards = 13;
    protected int roundNumber = 0;
    protected int delayTime = 600; 
    protected int thinkingTime = 300; 

    protected final Map<Player, Integer> scores = new HashMap<>();
    protected final Random rnd = new Random(30008);
    protected final StringBuilder log = new StringBuilder();
    protected boolean headlessMode = false; // Test mode flag
    protected int lastRoundWinnerIndex = 1; // Last round winner index, Round0 defaults to P1 first

    // UI Configuration
    protected Font bigFont = new Font("Arial", Font.BOLD, 36);
    protected Font smallFont = new Font("Arial", Font.BOLD, 18);

    // UI Buttons (Human player interaction)
    protected ch.aplu.jgamegrid.GGButton rummyButton = null;
    protected ch.aplu.jgamegrid.GGButton ginButton = null;
    protected ch.aplu.jgamegrid.GGButton knockButton = null;
    protected ch.aplu.jgamegrid.GGButton endTurnButton = null;

    //  UI Text Labels
    protected ch.aplu.jgamegrid.TextActor[] scoreActors = new ch.aplu.jgamegrid.TextActor[2];
    protected ch.aplu.jgamegrid.TextActor[] playerNameActors = new ch.aplu.jgamegrid.TextActor[2];
    protected ch.aplu.jgamegrid.TextActor stockpileLabel = null;
    protected ch.aplu.jgamegrid.TextActor discardLabel = null;

    // UI State Flags (Human player interaction) =
    public volatile ch.aplu.jcardgame.Card selectedCard = null;
    public volatile ch.aplu.jcardgame.Card drawnCardFromPile = null;
    public volatile boolean isEndingTurn = false;
    public volatile boolean isDeclaring = false;
    public volatile String declareAction = null; // "RUMMY", "GIN", "KNOCK"

    protected RummyGame(Properties props, ScoreRule scoring) {
        super(700, 700, 30);
        this.props = props;
        this.scoring = scoring;

        this.nbStartCards = Integer.parseInt(props.getProperty("number_cards", "13"));
        this.thinkingTime = Integer.parseInt(props.getProperty("thinkingTime", "300"));
        this.delayTime = Integer.parseInt(props.getProperty("delayTime", "600"));
    }

    public void setPlayers(List<Player> ps) {
        players.clear();
        players.addAll(ps);
        ps.forEach(p -> scores.put(p, 0));
    }

    public void setControllers(List<TurnController> cs) {
        controllers.clear();
        controllers.addAll(cs);
    }

    public void setTablePort(TablePort t) {
        this.table = t;
    }


    public final String runApp() {
        // Test mode: display window (Auto mode, predefined moves)
        this.headlessMode = false;

        setTitle("Rummy Game (V2.0) Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
        setStatusText("Initializing...");

        // Initialize UI (scores, buttons, etc.)
        initUI();

        // Show window
        show();

        // Run game logic
        runGameLoop();

        // Brief delay after game ends, let user see results
        delay(2000);

        return log.toString();
    }

    /**
     * Core game loop (shared by startGame and runApp)
     */
    private void runGameLoop() {
        while (!isGameOver()) {
            roundNumber++;
            playRound();
        }
        endGame();
    }

    protected void playRound() {
        initRound();

        // Add round info to log
        log.append("\n").append("Round").append(roundNumber-1).append(":");

        // Add initial hands to log
        log.append("\n").append("Initial Cards:");
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            log.append("P").append(p.getId()).append("-");
            log.append(convertHandToString(table.getHand(p)));
            if (i < players.size() - 1) {
                log.append("-");  // Add separator between P0 and P1
            }
        }

        int turnNumber = 0;
        // Starting player = last round winner (Round0 defaults to P1)
        int startingPlayerIndex = lastRoundWinnerIndex;
        int playerIndex = startingPlayerIndex;
        int totalActions = 0;
        int maxActions = 100;

        while (!isRoundOver() && totalActions < maxActions) {

            // Turn start: only add Turn marker when starting player acts
            if (playerIndex == startingPlayerIndex) {
                log.append("\n").append("Turn").append(turnNumber).append(":");
            } else {
                // Not turn start, add comma to separate from previous player
                log.append(",");
            }

            Player currentPlayer = players.get(playerIndex);

            // Check if there are predefined moves (Auto mode)
            if (table.hasAutoMovement(currentPlayer)) {
                // Reset turn log, avoid carrying lastAction from previous turn
                table.resetTurnLog();

                // Execute predefined move
                boolean shouldEndRound = table.executeAutoMovement(currentPlayer);

                // Record log
                ch.aplu.jcardgame.Card drawn = table.getLastDrawn();
                ch.aplu.jcardgame.Card discarded = table.getLastDiscarded();
                String action = table.getLastAction();

                log.append("P").append(currentPlayer.getId()).append("-");
                log.append(cardToString(drawn)).append("-");
                log.append(cardToString(discarded));
                if (action != null) {
                    log.append("-").append(action);
                }

                //Add delay (Auto mode also needs delay)
                if (!headlessMode) {
                    delay(delayTime);
                }

                if (shouldEndRound) {
                    break; // Declared, end round
                }
            } else {
                // No predefined moves, use TurnController (AI or Human)
                controllers.get(playerIndex).playTurn();

                // Get log info from table
                ch.aplu.jcardgame.Card drawn = table.getLastDrawn();
                ch.aplu.jcardgame.Card discarded = table.getLastDiscarded();
                String action = table.getLastAction();

                // Add player move to log
                log.append("P").append(currentPlayer.getId()).append("-");
                log.append(cardToString(drawn)).append("-");
                log.append(cardToString(discarded));
                if (action != null) {
                    log.append("-").append(action);
                }

                // Check if round is over
                if (isRoundOver()) {
                    break;
                }
            }

            //Add delay, let user see game process (consistent with old version)
            if (!headlessMode) {
                delay(delayTime);
            }

            totalActions++; // Count each player action
            playerIndex = (playerIndex + 1) % players.size();
            if (playerIndex == startingPlayerIndex) turnNumber++; // When back to starting player, Turn number +1
        }

        if (totalActions >= maxActions) {
            log.append("\n[WARNING: Max actions (").append(maxActions).append(") reached, force ending round]");
        }

        endRound();
    }

    /**
     * Convert card to log string
     */
    private String cardToString(ch.aplu.jcardgame.Card card) {
        if (card == null) {
            return "XX";
        }
        Rank rank = (Rank) card.getRank();
        Suit suit = (Suit) card.getSuit();
        return rank.getCardLog() + suit.getSuitShortHand();
    }

    /**
     * Convert hand to log string
     * Format: "5S,6D,7H,..."
     */
    private String convertHandToString(ch.aplu.jcardgame.Hand hand) {
        StringBuilder sb = new StringBuilder();
        java.util.List<ch.aplu.jcardgame.Card> cards = hand.getCardList();
        for (int i = 0; i < cards.size(); i++) {
            ch.aplu.jcardgame.Card card = cards.get(i);
            Rank rank = (Rank) card.getRank();
            Suit suit = (Suit) card.getSuit();
            sb.append(rank.getCardLog()).append(suit.getSuitShortHand());
            if (i < cards.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    // UI Initialization
    protected void initUI() {
        setTitle("Rummy Game");
        setStatusText("Initializing...");
        // Set green background (poker table color)
        setBgColor(new Color(20, 80, 20));
        setupButtons();
        setupScoreLabels();
        setupPileLabels();
    }

    /**
     * Initialize score and player name labels
     */
    protected void setupScoreLabels() {
        // Score positions (consistent with old project)
        ch.aplu.jgamegrid.Location[] scoreLocations = {
                new ch.aplu.jgamegrid.Location(25, 25),   // P0 (Computer) - Top left
                new ch.aplu.jgamegrid.Location(575, 675)  // P1 (Human) - Bottom right
        };

        // Player name positions
        ch.aplu.jgamegrid.Location[] nameLocations = {
                new ch.aplu.jgamegrid.Location(25, 50),   // P0
                new ch.aplu.jgamegrid.Location(575, 625)  // P1
        };

        Color bgColor = new Color(20, 80, 20); // Green background

        // Initialize score labels
        for (int i = 0; i < 2; i++) {
            String scoreText = "[P" + i + ": 0]";
            scoreActors[i] = new ch.aplu.jgamegrid.TextActor(scoreText, Color.WHITE, bgColor, bigFont);
            addActor(scoreActors[i], scoreLocations[i]);
        }

        // Initialize player name labels
        String[] names = {"Computer", "Human"};
        for (int i = 0; i < 2; i++) {
            playerNameActors[i] = new ch.aplu.jgamegrid.TextActor(names[i], Color.WHITE, bgColor, smallFont);
            addActor(playerNameActors[i], nameLocations[i]);
        }
    }

    /**
     * Initialize pile labels
     */
    protected void setupPileLabels() {
        Color bgColor = new Color(20, 80, 20); // Green background

        // Stockpile label (left side)
        ch.aplu.jgamegrid.Location stockLoc = new ch.aplu.jgamegrid.Location(30, 280);
        stockpileLabel = new ch.aplu.jgamegrid.TextActor("Stockpile", Color.WHITE, bgColor, smallFont);
        addActor(stockpileLabel, stockLoc);

        // Discard Pile label (right side)
        ch.aplu.jgamegrid.Location discardLoc = new ch.aplu.jgamegrid.Location(560, 280);
        discardLabel = new ch.aplu.jgamegrid.TextActor("Discard Pile", Color.WHITE, bgColor, smallFont);
        addActor(discardLabel, discardLoc);
    }

    /**
     * Update player score display
     */
    public void updateScore(int playerId, int score) {
        if (scoreActors[playerId] != null) {
            removeActor(scoreActors[playerId]);
        }
        Color bgColor = new Color(20, 80, 20); // Green background
        String scoreText = "P" + playerId + "[" + Math.max(score, 0) + "]";
        scoreActors[playerId] = new ch.aplu.jgamegrid.TextActor(scoreText, Color.WHITE, bgColor, bigFont);

        ch.aplu.jgamegrid.Location[] scoreLocations = {
                new ch.aplu.jgamegrid.Location(25, 25),
                new ch.aplu.jgamegrid.Location(575, 675)
        };
        addActor(scoreActors[playerId], scoreLocations[playerId]);
    }

    /**
     * Set up UI buttons (Classic and Gin modes)
     */
    protected void setupButtons() {
        boolean isGinMode = (this instanceof GinRummy);

        // Create End Turn button (all modes)
        endTurnButton = new ch.aplu.jgamegrid.GGButton("sprites/end.gif", false);
        addActor(endTurnButton, new ch.aplu.jgamegrid.Location(80, 610));
        endTurnButton.addButtonListener(new ch.aplu.jgamegrid.GGButtonListener() {
            @Override
            public void buttonClicked(ch.aplu.jgamegrid.GGButton button) {
                isEndingTurn = true;
            }
            @Override
            public void buttonPressed(ch.aplu.jgamegrid.GGButton button) {}
            @Override
            public void buttonReleased(ch.aplu.jgamegrid.GGButton button) {}
        });

        if (isGinMode) {
            // Gin Rummy mode: GIN and KNOCK buttons
            ginButton = new ch.aplu.jgamegrid.GGButton("sprites/gin.gif", false);
            addActor(ginButton, new ch.aplu.jgamegrid.Location(80, 650));
            ginButton.addButtonListener(new ch.aplu.jgamegrid.GGButtonListener() {
                @Override
                public void buttonClicked(ch.aplu.jgamegrid.GGButton button) {
                    isDeclaring = true;
                    declareAction = "GIN";
                }
                @Override
                public void buttonPressed(ch.aplu.jgamegrid.GGButton button) {}
                @Override
                public void buttonReleased(ch.aplu.jgamegrid.GGButton button) {}
            });

            knockButton = new ch.aplu.jgamegrid.GGButton("sprites/knock.gif", false);
            addActor(knockButton, new ch.aplu.jgamegrid.Location(80, 690));
            knockButton.addButtonListener(new ch.aplu.jgamegrid.GGButtonListener() {
                @Override
                public void buttonClicked(ch.aplu.jgamegrid.GGButton button) {
                    isDeclaring = true;
                    declareAction = "KNOCK";
                }
                @Override
                public void buttonPressed(ch.aplu.jgamegrid.GGButton button) {}
                @Override
                public void buttonReleased(ch.aplu.jgamegrid.GGButton button) {}
            });
        } else {
            // Classic Rummy mode: RUMMY button
            rummyButton = new ch.aplu.jgamegrid.GGButton("sprites/rummy.gif", false);
            addActor(rummyButton, new ch.aplu.jgamegrid.Location(80, 650));
            rummyButton.addButtonListener(new ch.aplu.jgamegrid.GGButtonListener() {
                @Override
                public void buttonClicked(ch.aplu.jgamegrid.GGButton button) {
                    isDeclaring = true;
                    declareAction = "RUMMY";
                }
                @Override
                public void buttonPressed(ch.aplu.jgamegrid.GGButton button) {}
                @Override
                public void buttonReleased(ch.aplu.jgamegrid.GGButton button) {}
            });
        }
    }

    /**
     * Reset UI state (called before each round starts)
     */
    protected void resetUIState() {
        selectedCard = null;
        drawnCardFromPile = null;
        isEndingTurn = false;
        isDeclaring = false;
        declareAction = null;
    }

    /**
     * Enable/disable buttons
     */
    public void setButtonsEnabled(boolean enabled) {
        if (endTurnButton != null) {
            endTurnButton.setMouseTouchEnabled(enabled);
        }
        if (rummyButton != null) {
            rummyButton.setMouseTouchEnabled(enabled);
        }
        if (ginButton != null) {
            ginButton.setMouseTouchEnabled(enabled);
        }
        if (knockButton != null) {
            knockButton.setMouseTouchEnabled(enabled);
        }
    }

    protected void initRound() {
        // If first round, create TablePort (Builder will set)
        // Otherwise reuse the same TablePort
        if (table == null) {
            table = new TablePort(deck, this instanceof GinRummy);
        }
        table.resetRound();

        // Deal cards (even in headless mode, pass this because setView() needs CardGame reference)
        table.dealInitialHands(players, nbStartCards, props, roundNumber - 1, this);

        // Set up UI listeners for human players (after dealing, because hands need to be created)
        if (!headlessMode) {
            for (Player p : players) {
                if (p.isHuman()) {
                    table.setupHumanPlayerListeners(this, p);
                    break; // Assume only one human player
                }
            }
        }

        if (!headlessMode) {
            setStatusText("Round " + (roundNumber - 1) + " started!");
        }
    }

    protected void endRound() {
        // Get declarer and declaration type
        Player declarer = table.getDeclarer();
        String action = table.getLastAction();

        rummy.meld.MeldFinder meldFinder = new rummy.meld.MeldFinder();

        if (declarer != null && action != null) {
            // Someone declared (RUMMY/GIN/KNOCK)
            if ("RUMMY".equals(action)) {
                // Classic Rummy: declarer gets opponent's total deadwood points
                Player opponent = players.get((declarer.getId() + 1) % players.size());
                rummy.meld.Meld opponentMeld = meldFinder.detectBestMeldAndDeadwood(table.getHand(opponent));

                int opponentDeadwood = 0;
                for (ch.aplu.jcardgame.Card c : opponentMeld.getDeadwood()) {
                    Rank r = (Rank) c.getRank();
                    opponentDeadwood += r.getPointValue();
                }

                scores.put(declarer, scores.get(declarer) + opponentDeadwood);
                // Opponent gets 0 points (no additional operation needed)
            } else if ("GIN".equals(action)) {
                // Gin Rummy - GIN: declarer gets opponent deadwood (old project had no 25 point bonus)
                Player opponent = players.get((declarer.getId() + 1) % players.size());
                rummy.meld.Meld opponentMeld = meldFinder.detectBestMeldAndDeadwood(table.getHand(opponent));

                int opponentDeadwood = 0;
                for (ch.aplu.jcardgame.Card c : opponentMeld.getDeadwood()) {
                    Rank r = (Rank) c.getRank();
                    opponentDeadwood += r.getPointValue();
                }

                scores.put(declarer, scores.get(declarer) + opponentDeadwood);
            } else if ("KNOCK".equals(action)) {
                // Gin Rummy - KNOCK: compare both sides' deadwood
                Player opponent = players.get((declarer.getId() + 1) % players.size());

                rummy.meld.Meld declarerMeld = meldFinder.detectBestMeldAndDeadwood(table.getHand(declarer));
                rummy.meld.Meld opponentMeld = meldFinder.detectBestMeldAndDeadwood(table.getHand(opponent));

                int declarerDeadwood = 0;
                for (ch.aplu.jcardgame.Card c : declarerMeld.getDeadwood()) {
                    Rank r = (Rank) c.getRank();
                    declarerDeadwood += r.getPointValue();
                }

                int opponentDeadwood = 0;
                for (ch.aplu.jcardgame.Card c : opponentMeld.getDeadwood()) {
                    Rank r = (Rank) c.getRank();
                    opponentDeadwood += r.getPointValue();
                }

                if (declarerDeadwood < opponentDeadwood) {
                    scores.put(declarer, scores.get(declarer) + (opponentDeadwood - declarerDeadwood));
                } else {
                    // Opponent undercut, opponent scores (no 25 point bonus in old architecture)
                    scores.put(opponent, scores.get(opponent) + (declarerDeadwood - opponentDeadwood));
                }
            }
        }
        // If no declaration (stockpile exhausted), both sides get 0 points

        //  Determine this round's winner (for next round's starting player)
        int roundWinnerId = -1;
        if (declarer != null && action != null) {
            // Case with declaration
            if ("RUMMY".equals(action) || "GIN".equals(action)) {
                roundWinnerId = declarer.getId(); // Declarer wins
            } else if ("KNOCK".equals(action)) {
                // KNOCK needs to compare deadwood
                Player opponent = players.get((declarer.getId() + 1) % players.size());
                rummy.meld.Meld declarerMeld = meldFinder.detectBestMeldAndDeadwood(table.getHand(declarer));
                rummy.meld.Meld opponentMeld = meldFinder.detectBestMeldAndDeadwood(table.getHand(opponent));

                int declarerDeadwood = 0;
                for (ch.aplu.jcardgame.Card c : declarerMeld.getDeadwood()) {
                    Rank r = (Rank) c.getRank();
                    declarerDeadwood += r.getPointValue();
                }

                int opponentDeadwood = 0;
                for (ch.aplu.jcardgame.Card c : opponentMeld.getDeadwood()) {
                    Rank r = (Rank) c.getRank();
                    opponentDeadwood += r.getPointValue();
                }

                if (declarerDeadwood < opponentDeadwood) {
                    roundWinnerId = declarer.getId();
                } else {
                    roundWinnerId = opponent.getId();
                }
            }
        }

        // If no winner found (tie or stockpile exhausted), keep previous round's starting player
        if (roundWinnerId != -1) {
            lastRoundWinnerIndex = roundWinnerId;
        }

        // Add round end log
        log.append("\n").append("Round").append(roundNumber-1).append(" End:");
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            log.append("P").append(p.getId()).append("-").append(scores.get(p));
            if (i < players.size() - 1) log.append(",");
        }

        if (!headlessMode) {
            setStatusText("Round " + (roundNumber-1) + " ended!");
            // Update score display on UI
            for (Player p : players) {
                updateScore(p.getId(), scores.get(p));
            }
        }
    }

    protected void endGame() {
        int max = scores.values().stream().max(Integer::compareTo).orElse(0);
        List<Player> winners = scores.entrySet().stream()
                .filter(e -> e.getValue() == max)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Add game end log
        log.append("\n");
        if (winners.size() == 1) {
            log.append("Game End:P").append(winners.get(0).getId());
        } else {
            log.append("Game End:Draw");
        }

        if (!headlessMode) {
            addActor(new Actor("sprites/gameover.gif"), new Location(350, 350));
            setStatusText("Game Over! Winner: " +
                    winners.stream().map(Player::getName).collect(Collectors.joining(", ")));
        }
    }

    // ========= Game Logic Methods =========
    protected boolean isRoundOver() {
        // Round end conditions:
        // 1. Someone declared Rummy/Gin/Knock (flag set by table.endRound())
        // 2. Stockpile is empty
        return table.isRoundEnded() || table.isStockEmpty();
    }

    protected boolean isGameOver() {
        // Game end condition: check if there's next round configuration
        String nextRoundKey = "rounds." + roundNumber + ".players.0.initialcards";
        String nextRoundValue = props.getProperty(nextRoundKey);

        // If no next round configuration, game ends
        return nextRoundValue == null;
    }

    // ========= Abstract Hook Methods =========
    protected abstract int getDefaultStartCards();


}