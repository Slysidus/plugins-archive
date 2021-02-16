package net.lightning.core.stats;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FightGameStats implements GameStats {

    private short kills, deaths;

}
