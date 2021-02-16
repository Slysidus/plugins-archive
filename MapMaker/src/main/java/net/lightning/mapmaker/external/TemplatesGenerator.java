package net.lightning.mapmaker.external;

import net.lightning.capture.game.CaptureGameMapModel;
import net.lightning.core.loader.ConfigurationModelLoadException;
import net.lightning.core.map.GameMapModel;
import net.lightning.lobby.LobbyMapModel;
import net.lightning.mapmaker.tools.TemplateGenerator;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class TemplatesGenerator {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) {
        File generatedDir = new File("../MapMaker/src/main/resources/generated");
        if (!generatedDir.exists()) {
            generatedDir.mkdirs();
        }

        generateTemplate(generatedDir, GameMapModel.class);
        generateTemplate(generatedDir, CaptureGameMapModel.class);
        generateTemplate(generatedDir, LobbyMapModel.class);
    }

    private static void generateTemplate(File generatedDir, Class<? extends GameMapModel> model) {
        String name = model.getSimpleName();
        if (name.toLowerCase().endsWith("model")) {
            name = name.substring(0, name.length() - 5);
        }
        if (name.toLowerCase().endsWith("gamemap")) {
            name = name.substring(0, name.length() - 7);
        }

        if (name.isEmpty()) {
            name = "default";
        }
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
        name = name.replaceAll("([A-Z])", "-$1").toLowerCase();

        File outputFile = new File(generatedDir, name + ".template.yml");
        try {
            FileConfiguration template = TemplateGenerator.generateTemplate(model);
            template.save(outputFile);
        }
        catch (IllegalAccessException | ConfigurationModelLoadException | InvocationTargetException | IOException e) {
            e.printStackTrace();
            System.err.println("[ERROR] Unable to generated template from model '" + model.getCanonicalName() + "'.");
            return;
        }
        System.out.println("[SUCCESS] Generated template \"" + outputFile.getName() + "\" from model '" + model.getCanonicalName() + "'");
    }

}
