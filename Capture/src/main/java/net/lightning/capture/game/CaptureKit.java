package net.lightning.capture.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CaptureKit {

    SOLDIER("Soldier"), ARCHER("Archer"), HEDGEHOG("Hedgehog");

    private final String name;

}
