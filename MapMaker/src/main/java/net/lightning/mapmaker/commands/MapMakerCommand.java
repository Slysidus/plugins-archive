package net.lightning.mapmaker.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import net.lightning.api.util.FileUtil;
import net.lightning.core.loader.AdvancedAdapter;
import net.lightning.core.loader.ConfigValueAdapter;
import net.lightning.core.loader.ConfigurationModelLoadException;
import net.lightning.core.loader.ConfigurationModelLoader;
import net.lightning.core.loader.types.ReadableBlock;
import net.lightning.core.loader.types.ReadableItemStack;
import net.lightning.core.loader.types.ReadableLocation;
import net.lightning.core.world.NaiveCuboid;
import net.lightning.core.world.NaiveRegion;
import net.lightning.mapmaker.MapMaker;
import net.lightning.mapmaker.templates.LiveTemplate;
import net.lightning.mapmaker.templates.MapMakerMeta;
import net.lightning.mapmaker.templates.Template;
import net.lightning.mapmaker.templates.TemplateField;
import net.lightning.mapmaker.tools.TemplateLoader;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

public class MapMakerCommand implements CommandExecutor {

    private final MapMaker plugin;
    private final Map<String, String> generalCommands, editorCommands;
    private final File templatesDir, configsDir;

    private final Map<UUID, LiveTemplate> uuidToLiveTemplate;

    public MapMakerCommand(MapMaker plugin) {
        this.plugin = plugin;

        this.generalCommands = Maps.newLinkedHashMap();
        generalCommands.put("templates", "List all templates");
        generalCommands.put("configs", "List all configs by their template in tree view.");
        generalCommands.put("version", "MapMaker version");
        generalCommands.put("world <world>", "Go to world");

        this.editorCommands = Maps.newLinkedHashMap();
        editorCommands.put("load <config_file> [<template_file>]", "Load or create a model configuration with a template.");
        editorCommands.put("meta", "Get meta about loaded configuration.");
        editorCommands.put("save", "Save loaded configuration.");
        editorCommands.put("save-as <config_file>", "Save loaded configuration as.");
        editorCommands.put("dump", "Prints every full key with and their values.");
        editorCommands.put("check", "Check unset and invalid values.");
        editorCommands.put("set <key> [<value>]", "Set a value with type checking. Value is not required for locations and item stacks.");
        editorCommands.put("list add/remove <key> [<value>]", "Add a value to a list with type checking.");

        this.templatesDir = new File(plugin.getDataFolder(), "templates");
        this.configsDir = new File(plugin.getDataFolder(), "configurations");
        FileUtil.checkDirPersistence(templatesDir);
        FileUtil.checkDirPersistence(configsDir);

        this.uuidToLiveTemplate = Maps.newHashMap();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!(sender instanceof Player)) {
                return false;
            }

            final Player player = (Player) sender;
            final UUID uniqueId = player.getUniqueId();

            final LiveTemplate liveTemplate = uuidToLiveTemplate.get(uniqueId);
            final String firstArg = args.length > 0 ? args[0].toLowerCase() : "";

            execute:
            switch (firstArg) {
                case "help":
                case "?":
                    player.sendMessage(" ");
                    player.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "Map maker commands:");
                    generalCommands.forEach((usage, description) ->
                            player.sendMessage(ChatColor.AQUA + "/" + label + " " + usage + ChatColor.GRAY + " - " + ChatColor.WHITE + description));
                    player.sendMessage(" ");
                    player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Editor commands:");
                    editorCommands.forEach((usage, description) ->
                            player.sendMessage(ChatColor.YELLOW + "/" + label + " " + usage + ChatColor.GRAY + " - " + ChatColor.WHITE + description));
                    player.sendMessage(" ");
                    break;

                case "version":
                case "ver":
                    player.sendMessage(ChatColor.AQUA + "Running MapMaker version " + plugin.getDescription().getVersion() + ".");
                    break;

                case "templates":
                    player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.AQUA + "Template files: " +
                            ChatColor.WHITE + String.join(", ", FileUtil.getFileNamesRecursively(templatesDir)));
                    break;

                case "configs":
                    Map<String, List<String>> configs = Maps.newHashMap();
                    for (File file : FileUtil.getFilesRecursively(configsDir)) {
                        MapMakerMeta meta = TemplateLoader.loadMeta(YamlConfiguration.loadConfiguration(file));
                        if (meta.getTargetTemplate() != null) {
                            final String templateName = meta.getTargetTemplate().substring(0, meta.getTargetTemplate().length() - ".template.yml".length());
                            final String configName = file.getAbsolutePath().substring(configsDir.getAbsolutePath().length() + 1);

                            if (configs.containsKey(templateName)) {
                                configs.get(templateName).add(configName);
                            }
                            else {
                                configs.put(templateName, Lists.newArrayList(configName));
                            }
                        }
                    }

                    player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.AQUA + "Configs:");
                    configs.forEach((template, configNames) -> {
                        player.sendMessage(ChatColor.YELLOW + template);
                        for (String configName : configNames) {
                            player.sendMessage(ChatColor.WHITE + "  " + configName);
                        }
                    });
                    break;

                case "worlds":
                case "world":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Please specify a world to teleport to!");
                        break;
                    }

                    final String worldName = args[1];
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        File worldFile = new File(Bukkit.getServer().getWorldContainer(), worldName);
                        if (worldFile.exists() && worldFile.isDirectory()) {
                            WorldCreator worldCreator = WorldCreator.name(worldName);
                            world = worldCreator.createWorld();
                        }
                    }

                    if (world == null) {
                        player.sendMessage(ChatColor.RED + "This world doesn't exist!");
                        break;
                    }

                    player.teleport(world.getSpawnLocation());
                    player.sendMessage(ChatColor.GREEN + "You've been warped to \"" + worldName + "\".");
                    break;

                case "create":
                case "load":
                    if (uuidToLiveTemplate.containsKey(uniqueId)) {
                        player.sendMessage(ChatColor.RED + "You are already editing a configuration.");
                        break;
                    }

                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Missing configuration file.");
                        break;
                    }
                    String configFileName = args[1];
                    if (!configFileName.endsWith(".map.yml")) {
                        configFileName += ".map.yml";
                    }
                    if (!configFileName.endsWith(".yml")) {
                        configFileName += ".yml";
                    }

                    File configFile = new File(configsDir, configFileName);
                    if (!configFile.exists()) {
                        player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Configuration file does not exist, it will be created on save.");
                    }

                    FileConfiguration fileConfiguration = configFile.exists()
                            ? YamlConfiguration.loadConfiguration(configFile)
                            : new YamlConfiguration();
                    MapMakerMeta meta = TemplateLoader.loadMeta(fileConfiguration);
                    String templateFileName = meta.getTargetTemplate() != null
                            ? meta.getTargetTemplate() : args.length > 2 ? args[2] : null;
                    if (templateFileName == null) {
                        player.sendMessage(ChatColor.RED + "Missing template file name.");
                        break;
                    }

                    if (!templateFileName.endsWith(".template.yml")) {
                        templateFileName += ".template.yml";
                    }

                    File templateFile = new File(templatesDir, templateFileName);
                    if (!templateFile.exists()) {
                        player.sendMessage(ChatColor.RED + "Template file does not exist.");
                        break;
                    }

                    Template template;
                    try {
                        template = TemplateLoader.parseTemplate(YamlConfiguration.loadConfiguration(templateFile));
                    }
                    catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "This template cannot be fully loaded. Make sure the game mode is loaded on the server.");
                        break;
                    }

                    ConfigurationModelLoader modelLoader = new ConfigurationModelLoader();
                    try {
                        LiveTemplate newLiveTemplate = TemplateLoader.loadLiveTemplate(modelLoader, template, fileConfiguration);
                        newLiveTemplate.getMeta().setConfigFile(configFileName);
                        newLiveTemplate.getMeta().setTargetTemplate(templateFileName);
                        if (newLiveTemplate.getMeta().getVersion() == null) {
                            newLiveTemplate.getMeta().setVersion(plugin.getDescription().getVersion());
                        }
                        uuidToLiveTemplate.put(uniqueId, newLiveTemplate);
                        player.sendMessage(ChatColor.GREEN + "Configuration file '" + configFileName + "' has been loaded.");
                    }
                    catch (ConfigurationModelLoadException e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.DARK_RED + "Unable to load live template.");
                        player.sendMessage(ChatColor.RED.toString() + ChatColor.ITALIC + e.getMessage());
                    }
                    break;

                case "meta":
                case "info":
                    if (!uuidToLiveTemplate.containsKey(uniqueId)) {
                        player.sendMessage(ChatColor.RED + "Please load a live template before using this command.");
                        break;
                    }

                    meta = liveTemplate.getMeta();
                    player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.BLUE + "Configuration meta:");
                    player.sendMessage(ChatColor.WHITE + "Template: " + ChatColor.YELLOW + meta.getTargetTemplate());
                    player.sendMessage(ChatColor.WHITE + "Output file: " + ChatColor.YELLOW + meta.getConfigFile());
                    player.sendMessage(ChatColor.WHITE + "MapMaker version: " + ChatColor.YELLOW + meta.getVersion());
                    break;

                case "save-as":
                case "save":
                    if (!uuidToLiveTemplate.containsKey(uniqueId)) {
                        player.sendMessage(ChatColor.RED + "Please load a live template before using this command.");
                        break;
                    }

                    if (firstArg.equals("save-as")) {
                        if (args.length < 2) {
                            player.sendMessage(ChatColor.RED + "Save-as must be followed by the config file name.");
                            break;
                        }
                        configFileName = args[1];
                    }
                    else {
                        configFileName = liveTemplate.getMeta().getConfigFile();
                    }

                    if (!configFileName.endsWith(".map.yml")) {
                        configFileName += ".map.yml";
                    }
                    if (!configFileName.endsWith(".yml")) {
                        configFileName += ".yml";
                    }
                    configFile = new File(configsDir, configFileName);
                    try {
                        FileUtil.checkFilePersistence(configFile);
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                        sender.sendMessage(ChatColor.DARK_RED + "An error occured while creating config file, please check the console.");
                        break;
                    }

                    int missing = 0, unset = 0, invalidSize = liveTemplate.getInvalidSize().size();
                    for (TemplateField unsetValue : liveTemplate.getUnsetValues()) {
                        if (unsetValue.isNotNull()) {
                            missing++;
                        }
                        else {
                            unset++;
                        }
                    }

                    if (missing > 0 || unset > 0 || invalidSize > 0) {
                        player.sendMessage((missing > 0 || invalidSize > 0 ? ChatColor.RED : ChatColor.GOLD).toString() + ChatColor.ITALIC +
                                "WARNING: This configuration file contains " + missing + " missing values, " +
                                invalidSize + " collections with invalid size and " + unset + " unset values.");
                    }

                    modelLoader = new ConfigurationModelLoader();
                    FileConfiguration output = new YamlConfiguration();
                    liveTemplate.getMeta().setVersion(plugin.getDescription().getVersion());
                    TemplateLoader.saveMeta(liveTemplate.getMeta(), output);
                    liveTemplate.getSetValues().forEach(entry -> {
                        TemplateField field = entry.getKey();
                        Object value = entry.getValue();

                        ConfigValueAdapter<?> customAdapter = modelLoader.getAdapters().get(field.getType());
                        if (customAdapter instanceof AdvancedAdapter) {
                            ((AdvancedAdapter<?>) customAdapter).pleaseJavaLetMeSetTheValue(modelLoader, field.getFakeField(), output, field.getFullPath(), value);
                        }
                        else {
                            output.set(field.getFullPath(), value);
                        }
                    });

                    try {
                        output.save(configFile);
                        player.sendMessage(ChatColor.GREEN + "Configuration file '" + configFileName + "' has been saved.");
                        uuidToLiveTemplate.remove(uniqueId);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.DARK_RED + "Unable to save configuration file! Check console for more information.");
                    }
                    break;

                case "dump":
                    if (!uuidToLiveTemplate.containsKey(uniqueId)) {
                        player.sendMessage(ChatColor.RED + "Please load a live template before using this command.");
                        break;
                    }

                    player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.BLUE + "Configuration values:");
                    player.sendMessage(ChatColor.GOLD + "UNSET" + ChatColor.WHITE + " = Optional " + ChatColor.RED + "MISSING" + ChatColor.WHITE + " = Required");
                    player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Hovering a white key show the value.");

                    List<String> fields = Lists.newArrayList(liveTemplate.getTemplateFields().keySet());
                    fields.sort(Comparator.comparingInt(String::length)
                            .thenComparing(a -> a));
                    fields.forEach(key -> {
                        TemplateField field = liveTemplate.getTemplateFields().get(key);
                        boolean isSet = liveTemplate.getValues().containsKey(key) && liveTemplate.getValues().get(key) != null;

                        ChatColor color = isSet ? ChatColor.WHITE : field.isNotNull() ? ChatColor.RED : ChatColor.GOLD;
                        String tickHover = "SET";

                        HoverEvent showValue = null;

                        int foundSize = -1;
                        if (isSet) {
                            Object value = liveTemplate.getValues().get(key);
                            String valueString = value.toString();

                            if (field.getFixedLength() != -1 && value instanceof Collection) {
                                Collection<?> collection = (Collection<?>) value;
                                foundSize = collection.size();
                                valueString = collection.stream().map(Object::toString)
                                        .collect(Collectors.joining("- \n"));
                            }

                            showValue = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(valueString)
                                    .color(color)
                                    .create());
                        }
                        else {
                            if (field.isNotNull()) {
                                tickHover = "MISSING";
                            }
                            else {
                                tickHover = "UNSET";
                            }
                        }

                        ComponentBuilder componentBuilder = new ComponentBuilder("");
                        componentBuilder.append(isSet ? "[✔]" : "[✘]").color(color)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, fromLegacy(color + tickHover)));
                        componentBuilder.append(" ").event((HoverEvent) null);
                        componentBuilder.append(key).color(color);
                        if (showValue != null) {
                            componentBuilder.event(showValue);
                        }
                        if (foundSize != -1) {
                            boolean valid = foundSize == field.getFixedLength();
                            componentBuilder.append(" (" + foundSize + "/" + field.getFixedLength() + ")")
                                    .color(valid ? ChatColor.GREEN : ChatColor.RED)
                                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new ComponentBuilder((valid ? "Valid" : "Invalid") + " list size")
                                                    .color(valid ? ChatColor.GREEN : ChatColor.RED).create()));
                        }

                        player.spigot().sendMessage(componentBuilder.create());
                    });
                    break;

                case "check":
                    if (!uuidToLiveTemplate.containsKey(uniqueId)) {
                        player.sendMessage(ChatColor.RED + "Please load a live template before using this command.");
                        break;
                    }

                    player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.BLUE + "Unset values: " + liveTemplate.getUnsetValues().stream()
                            .map(field -> (field.isNotNull() ? ChatColor.RED : ChatColor.GOLD) + field.getFullPath())
                            .collect(Collectors.joining(ChatColor.WHITE + ", ")));
                    player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.BLUE + "Invalid length: " + liveTemplate.getInvalidSize().stream()
                            .map(TemplateField::getFullPath)
                            .map(s -> ChatColor.RED + s)
                            .collect(Collectors.joining(ChatColor.WHITE + ", ")));
                    break;

                case "set":
                    if (!uuidToLiveTemplate.containsKey(uniqueId)) {
                        player.sendMessage(ChatColor.RED + "Please load a live template before using this command.");
                        break;
                    }

                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Set must be followed by the key.");
                        break;
                    }

                    String key = args[1].toLowerCase();
                    if (!liveTemplate.getTemplateFields().containsKey(key)) {
                        player.sendMessage(ChatColor.RED.toString() + ChatColor.ITALIC + "Unknown key '" + key + "'.");
                        break;
                    }

                    TemplateField field = liveTemplate.getTemplateFields().get(key);
                    if (Collection.class.isAssignableFrom(field.getType())) {
                        player.sendMessage(ChatColor.GRAY + "This field is a collection, please use '/" + label + " list add <key> [<value>]' instead.");
                        break;
                    }

                    String value = args.length > 2 ? args[2].toLowerCase() : null;

                    Class<?> fieldType = field.getType();
                    Object object;
                    if ((object = getRelativeValue(player, fieldType, value)) == null) {
                        if (args.length < 3) {
                            player.sendMessage(ChatColor.RED + "Give a value to the key.");
                            break;
                        }

                        if (value.equals("null")) {
                            player.sendMessage(ChatColor.GREEN + "Value has been removed. " + ChatColor.GRAY + "(" + field.getFullPath() + ")");
                            break;
                        }

                        object = getValue(fieldType, value);
                        if (object == null) {
                            player.sendMessage(ChatColor.DARK_RED + "No adapter found for this field type!");
                            break;
                        }
                    }

                    liveTemplate.getValues().put(key, object);

                    ComponentBuilder successMessageBuilder = new ComponentBuilder("Value has been set!")
                            .color(ChatColor.GREEN).append(" ")
                            .italic(true)
                            .append("(" + field.getFullPath() + ")")
                            .color(ChatColor.WHITE)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(object.toString()).create()));
                    player.spigot().sendMessage(successMessageBuilder.create());
                    break;

                case "list":
                    if (!uuidToLiveTemplate.containsKey(uniqueId)) {
                        player.sendMessage(ChatColor.RED + "Please load a live template before using this command.");
                        break;
                    }

                    boolean remove = false, clear = false;
                    switch (args.length > 1 ? args[1].toLowerCase() : "") {
                        case "add":
                        case "set":
                            remove = false;
                            break;

                        case "remove":
                        case "delete":
                            remove = true;
                            break;

                        case "clear":
                        case "wipe":
                            clear = true;
                            break;

                        default:
                            player.sendMessage(ChatColor.RED + "Invalid list action! Please use either add or remove.");
                            break execute;
                    }

                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "Please specify the key");
                        break;
                    }

                    key = args[2].toLowerCase();
                    if (!liveTemplate.getTemplateFields().containsKey(key)) {
                        player.sendMessage(ChatColor.RED.toString() + ChatColor.ITALIC + "Unknown key '" + key + "'.");
                        break;
                    }

                    field = liveTemplate.getTemplateFields().get(key);
                    if (!Collection.class.isAssignableFrom(field.getType())) {
                        player.sendMessage(ChatColor.GRAY + "This field is a not collection, please use '/" + label + " set <key> [<value>]' instead.");
                        break;
                    }

                    Collection collection;
                    try {
                        collection = liveTemplate.getValues().get(key) != null
                                ? (Collection) liveTemplate.getValues().get(key)
                                : (Collection) field.getType().newInstance();
                    }
                    catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.DARK_RED + "Unable to create an instance of this collection!");
                        break;
                    }

                    if (clear) {
                        collection.clear();
                        player.sendMessage(ChatColor.GREEN + "Values cleared.");
                    }
                    else {
                        ParameterizedType listType = (ParameterizedType) field.getFakeField().getGenericType();
                        fieldType = (Class<?>) listType.getActualTypeArguments()[0];
                        value = args.length > 3 ? args[3].toLowerCase() : null;
                        if ((object = getRelativeValue(player, fieldType, value)) == null) {
                            if (args.length < 4) {
                                player.sendMessage(ChatColor.RED + "Give a value to the key.");
                                break;
                            }

                            if (value.equals("null")) {
                                if (field.isNotNull()) {
                                    player.sendMessage(ChatColor.RED.toString() + ChatColor.ITALIC + "Unable to add null value to this list.");
                                    break;
                                }
                            }
                            else {
                                object = getValue(fieldType, value);
                                if (object == null) {
                                    player.sendMessage(ChatColor.DARK_RED + "No adapter found for this field type!");
                                    break;
                                }
                            }
                        }

                        if (remove) {
                            collection.remove(object);
                        }
                        else {
                            if (collection.contains(object)) {
                                player.sendMessage(ChatColor.YELLOW + "Tip: This value is already in the list. It has been added anyway.");
                            }
                            collection.add(object);
                        }
                        successMessageBuilder = new ComponentBuilder("Value has been " + (remove ? "removed" : "added") + "!")
                                .color(ChatColor.GREEN).append(" ")
                                .italic(true)
                                .append("(" + field.getFullPath() + ")")
                                .color(ChatColor.WHITE)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(object == null ? null : object.toString()).create()));
                        player.spigot().sendMessage(successMessageBuilder.create());
                    }

                    liveTemplate.getValues().put(key, collection);
                    break;

                default:
                    player.sendMessage(ChatColor.RED + "Unknown or invalid command argument. Please use '/" + label + " help' to get all commands.");
            }

            return true;
        }
        catch (StopCommandExecution ignored) {
        }
        return false;
    }

    private Object getRelativeValue(Player player, Class<?> fieldType, @Nullable String value) {
        if (Location.class.isAssignableFrom(fieldType)) {
            Location from;
            if ("target".equals(value)) {
                from = player.getTargetBlock(Sets.newHashSet(Material.AIR), 10).getLocation();
                if (from == null) {
                    player.sendMessage(ChatColor.RED + "You are not looking at any block within a 10 blocks range.");
                    throw new StopCommandExecution();
                }
            }
            else {
                if (fieldType.equals(ReadableBlock.class)) {
                    player.sendMessage(ChatColor.YELLOW + "Inspection: Expected type is a block, you may want to use 'target' instead of 'here'.");
                }
                from = player.getLocation();
            }

            if (fieldType.equals(Location.class)) {
                return from;
            }
            else if (fieldType.equals(ReadableLocation.class)) {
                return new ReadableLocation(from);
            }
            else if (fieldType.equals(ReadableBlock.class)) {
                return new ReadableBlock(from);
            }
        }
        else if (ItemStack.class.isAssignableFrom(fieldType)) {
            ItemStack from = player.getItemInHand();
            if (from == null) {
                from = new ItemStack(Material.AIR);
            }

            if (fieldType.equals(ItemStack.class)) {
                return from;
            }
            else if (fieldType.equals(ReadableItemStack.class)) {
                return new ReadableItemStack(from);
            }
        }
        else if (NaiveRegion.class.isAssignableFrom(fieldType) || NaiveCuboid.class.isAssignableFrom(fieldType)) {
            WorldEditPlugin worldEdit = JavaPlugin.getPlugin(WorldEditPlugin.class);
            Selection selection = worldEdit.getSelection(player);
            if (selection instanceof CuboidSelection) {
                CuboidSelection cuboidSelection = (CuboidSelection) selection;
                if (NaiveRegion.class.isAssignableFrom(fieldType)) {
                    return new NaiveRegion(cuboidSelection.getMinimumPoint(), cuboidSelection.getMaximumPoint());
                }
                else {
                    return new NaiveCuboid(cuboidSelection.getMinimumPoint(), cuboidSelection.getMaximumPoint());
                }
            }
            else {
                player.sendMessage(ChatColor.RED + "Invalid selection!");
            }
        }
        return null;
    }

    private Object getValue(Class<?> fieldType, String value) {
        Object object = null;
        if (fieldType.equals(String.class)) {
            object = value;
        }
        else if (fieldType.equals(char.class) || fieldType.equals(Character.class)) {
            object = value.charAt(0);
        }
        if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
            object = Byte.parseByte(value);
        }
        else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
            object = Short.parseShort(value);
        }

        else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
            object = Integer.parseInt(value);
        }

        else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
            object = Long.parseLong(value);
        }

        else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
            object = Float.parseFloat(value);
        }

        else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
            object = Double.parseDouble(value);
        }
        return object;
    }

    private BaseComponent[] fromLegacy(String legacy) {
        return TextComponent.fromLegacyText(legacy);
    }

}
