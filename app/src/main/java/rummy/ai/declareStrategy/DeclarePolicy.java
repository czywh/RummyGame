package rummy.ai.declareStrategy;


import rummy.engine.TablePort;
import rummy.model.Player;

public interface DeclarePolicy {
    /** Determine if declare conditions are met (Gin/Knock/Classic discard) and execute */
    void tryDeclare(Player player, TablePort table);
}