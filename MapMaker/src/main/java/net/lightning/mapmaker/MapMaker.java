package net.lightning.mapmaker;

import lombok.SneakyThrows;
import net.lightning.core.modules.WeatherModule;
import net.lightning.core.server.CraftServerHandler;
import net.lightning.mapmaker.commands.MapMakerCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.security.CodeSource;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MapMaker extends JavaPlugin {

    @SneakyThrows
    @Override
    public void onEnable() {
        File templatesFolder = new File(getDataFolder(), "templates");
        if (!templatesFolder.exists() && !templatesFolder.mkdirs()) {
            templatesFolder.delete();
            templatesFolder.mkdir();
        }

        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            ZipInputStream zipInputStream = new ZipInputStream(codeSource.getLocation().openStream());
            while (true) {
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                if (zipEntry == null) {
                    break;
                }
                String name = zipEntry.getName();
                if (name.startsWith("generated/") && name.endsWith(".template.yml")) {
                    saveResource(name, "templates/" + name.substring(10), true);
                }
            }
        }

        CraftServerHandler craftServerHandler = new CraftServerHandler(this);
        File libsDir = new File(getDataFolder(), "libs");
        if (libsDir.exists() && libsDir.isDirectory()) {
            for (File file : libsDir.listFiles()) {
                if (file.getName().endsWith(".jar")) {
                    craftServerHandler.injectClasspath(file);
                }
            }
        }

        getCommand("mapmaker").setExecutor(new MapMakerCommand(this));
        Bukkit.getPluginManager().registerEvents(WeatherModule.builder()
                .build(), this);
    }

    public void saveResource(String resourcePath, String outPath, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + getFile());
        }

        resourcePath = outPath.replace('\\', '/');
        File outFile = new File(getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
            else {
                getLogger().log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }
        }
        catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }

}
