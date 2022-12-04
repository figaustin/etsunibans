package com.etsuni.etsunibans;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


public final class EtsuniBans extends JavaPlugin {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    @Override
    public void onEnable() {
        connect();
        this.getCommand("ban").setExecutor(new BanEvent(this));
        this.getServer().getPluginManager().registerEvents(new Events(this), this);
        this.getCommand("history").setExecutor(new History(this));
    }

    @Override
    public void onDisable() {


    }

    public void connect() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("etsunibans");
        collection = database.getCollection("bans");
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }

}
