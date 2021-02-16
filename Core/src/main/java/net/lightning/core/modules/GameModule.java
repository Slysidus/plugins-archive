package net.lightning.core.modules;

import net.lightning.core.Game;

import java.util.ArrayList;
import java.util.List;

public abstract class GameModule<GameObject extends Game> {

    protected GameObject game;

    public abstract String getName();

    public List<Class<? extends GameModule<?>>> getDependencies() {
        return new ArrayList<>();
    }

    public void onLoad() {
    }

    @SuppressWarnings("unchecked")
    public void setGame(Game game) {
        this.game = (GameObject) game;
    }

}
