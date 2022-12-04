package com.etsuni.etsunibans;

import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;



public class Events implements Listener {

    private final EtsuniBans plugin;

    public Events(EtsuniBans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if(isBanned(player.getUniqueId().toString())){
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "reason");
        }

    }

    private boolean isBanned(String uuid) {
        LocalDateTime now = LocalDateTime.now();

        Document find = new Document("uuid", uuid.toString());
        FindIterable<Document> finds = plugin.getCollection().find(find);
        if(finds == null) {
            return false;
        }

        for(Document doc : finds) {
            Bukkit.broadcastMessage(doc.getList("bans", Document.class).get(0).getString("reason"));
            List<Document> list = doc.getList("bans", Document.class);
            Date date = list.get(list.size() - 1).getDate("expiration");
            LocalDateTime convert = date.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDateTime();

            if(now.isAfter(convert)) {
                return false;
            }
        }
        return true;
    }


}
