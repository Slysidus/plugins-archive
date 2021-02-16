package net.lightning.mapmaker.templates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class MapMakerMeta {

    private String targetTemplate;

    private String configFile;

    private String version;

}
