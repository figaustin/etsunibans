package com.etsuni.etsunibans;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;


public final class EtsuniBans extends JavaPlugin {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    private File customConfigFile;
    private FileConfiguration customConfig;

    private static final Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onEnable() {
        createCustomConfig();
        if(!connect()) {
            log.severe(String.format("[%s] - Disabled due to config not setup correctly, please add the correct values! " +
                    "Please change uri to your own uri/connection string!", getDescription().getName()));
            log.severe(String.format("[%s] - If you get another error after setting this up, you have entered info wrong!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.getCommand("ban").setExecutor(new BanEvent(this));
        this.getServer().getPluginManager().registerEvents(new Events(this), this);
        this.getCommand("history").setExecutor(new HistoryGUI(this));
        this.getCommand("unban").setExecutor(new Unban(this));
    }

    @Override
    public void onDisable() {


    }

    public Boolean connect() {
        String uri = customConfig.getString("database.uri");
        String databaseName = customConfig.getString("database.database_name");
        String collectionName = customConfig.getString("database.collection_name");

        if(uri == null) {
            return false;
        }
        if(databaseName == null) {
            return false;
        }
        if(collectionName == null) {
            return false;
        }
        mongoClient = MongoClients.create(uri);
        database = mongoClient.getDatabase(databaseName);
        collection = database.getCollection(collectionName);
        return true;
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "config.yml");
        if(!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        customConfig = new YamlConfiguration();

        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

    }

    public FileConfiguration getCustomConfig() {
        return this.customConfig;
    }

}
