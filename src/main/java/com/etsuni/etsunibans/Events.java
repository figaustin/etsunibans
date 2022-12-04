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
        Unban unban = new Unban(plugin);
        if(!unban.allowPlayerToJoin(uuid.toString())){
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, getBanReason(uuid.toString()));
        }

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
