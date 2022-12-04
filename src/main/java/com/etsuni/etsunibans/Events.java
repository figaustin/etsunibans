package com.etsuni.etsunibans;

import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;

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
        if(isBanned(uuid.toString())){
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, getBanReason(uuid.toString()));
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
            System.out.println("Date string" + date.toString());
            LocalDateTime convert = date.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
            System.out.println("Convert string" + convert.toString());
            if(now.isAfter(convert)) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private String getBanReason(String uuid) {
        Document find = new Document("uuid", uuid.toString());
        String str = "";
        FindIterable<Document> finds = plugin.getCollection().find(find);
        if(finds == null) {
            return null;
        }

        for(Document doc : finds) {
            Bukkit.broadcastMessage(doc.getList("bans", Document.class).get(0).getString("reason"));
            List<Document> list = doc.getList("bans", Document.class);
            str = list.get(list.size() - 1).getString("reason");
        }

        return str;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        if(!inventory.getItem(0).getType().equals(Material.PLAYER_HEAD) && !inventory.getItem(0).hasItemMeta()) {
            return;
        }

        event.setCancelled(true);
    }

}
