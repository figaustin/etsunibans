package com.etsuni.etsunibans;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Unban implements CommandExecutor {

    private final EtsuniBans plugin;

    public Unban(EtsuniBans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            if(command.getName().equalsIgnoreCase("unban")) {
                if(args.length > 0) {
                    OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[0]);
                    UUID uuid = targetPlayer.getUniqueId();

                    if(forceUnban(uuid.toString()) == null) {
                        sender.sendMessage(ChatColor.RED + "Could not find target player or player is already unbanned!");
                    }
                    else if(forceUnban(uuid.toString()).wasAcknowledged()){
                        sender.sendMessage(ChatColor.GREEN + "You have unbanned " + targetPlayer.getName());
                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "Please specify a player's name");
                }
            }
        }
        return false;
    }

    //Override the expiration of a ban and set "expired" to true
    public UpdateResult forceUnban(String uuid) {
        Document find = new Document("uuid", uuid.toString());
        FindIterable<Document> finds = plugin.getCollection().find(find);

        if(finds.first() == null) {
            return null;
        }

        Bson filter = Filters.and(Filters.eq("uuid", uuid), Filters.eq("bans.expired", false));
        Bson update = Updates.set("bans.$.expired", true);
        return plugin.getCollection().updateOne(filter, update);
    }

    //Checks to see if player should continue being banned or not: Looks to see if expiration date is in the past, if so
    //Set "expired" to true and allow the player to join
    public Boolean allowPlayerToJoin(String uuid) {
        LocalDateTime now = LocalDateTime.now();
        Document find = new Document("uuid", uuid.toString());
        FindIterable<Document> finds = plugin.getCollection().find(find);

        if(finds.first() == null) {
            return true;
        }

        for(Document doc : finds) {
            List<Document> list = doc.getList("bans", Document.class);
            int index = list.size() - 1;
            Date date = null;

            if(list.get(index).get("expiration") instanceof Date){
                date = list.get(index).getDate("expiration");
                if(list.get(index).getBoolean("expired")) {
                    return true;
                }
            }
            else if(list.get(index).get("expiration") instanceof String) {
                if(list.get(index).getString("expiration").equalsIgnoreCase("Permanent")) {
                    return list.get(index).getBoolean("expired");
                }
            }
            LocalDateTime convert = Objects.requireNonNull(date).toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
            if(now.isAfter(convert)) {
                Bson filter = Filters.and(Filters.eq("uuid", uuid), Filters.eq("bans.expired", false));
                Bson update = Updates.set("bans.$.expired", true);
                UpdateResult result = plugin.getCollection().updateOne(filter, update);
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
}
