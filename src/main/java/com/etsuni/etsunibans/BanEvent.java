package com.etsuni.etsunibans;

import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class BanEvent implements CommandExecutor {

    private final EtsuniBans plugin;

    public BanEvent(EtsuniBans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            if(command.getName().equalsIgnoreCase("ban")) {
                Player p = Bukkit.getPlayer(args[0]);
                if (p == null) {
                    return false;
                }
                String args1 = args[1];
                if(args1 != null) {
                    if(isDuration(args1)) {
                        String reason = reason(args, false);
                        if(reason == null) {
                            return false;
                        }
                        tempbanPlayer(((Player) sender).getUniqueId().toString(), p.getUniqueId().toString(),args1, reason);
                        p.kickPlayer(reason);
                    }
                    else{
                        String reason = reason(args, true);
                        if(reason == null) {
                            return false;
                        }
                        permaBanPlayer(((Player) sender).getUniqueId().toString(), p.getUniqueId().toString(), reason);
                        p.kickPlayer("You have been banned for: " + reason);
                    }
                }

            }
        }
        return false;
    }

    private void tempbanPlayer(String issuer, String uuid, String duration, String reason) {
        playerInDB(uuid);
        Document find = new Document("uuid", uuid);
        LocalDateTime now = LocalDateTime.now();

        BasicDBObject ban = new BasicDBObject("bans", new BasicDBObject("date_issued", now)
                .append("issuer", issuer)
                .append("reason", reason)
                .append("duration", duration)
                .append("expiration", parseDuration(duration))
                .append("expired", false));
        BasicDBObject update = new BasicDBObject("$push", ban);
        plugin.getCollection().updateOne(find, update);
    }

    private void permaBanPlayer(String issuer, String uuid, String reason) {
        playerInDB(uuid);
        Document find = new Document("uuid", uuid);
        LocalDateTime now = LocalDateTime.now();

        BasicDBObject ban = new BasicDBObject("bans", new BasicDBObject("date_issued", now)
                .append("issuer", issuer)
                .append("reason", reason)
                .append("duration", "Permanent")
                .append("expiration", "Permanent")
                .append("expired", false));
        BasicDBObject update = new BasicDBObject("$push", ban);
        plugin.getCollection().updateOne(find, update);
    }

    private void playerInDB(String uuid) {
        List<Document> bansList = new ArrayList();
        Document find = new Document("uuid", uuid);
        Document document = new Document("uuid", uuid).append("bans", bansList);
        if(plugin.getCollection().find(find).first() == null) {
            plugin.getCollection().insertOne(document);
            Bukkit.broadcastMessage("inserting one");
        }
    }

    private String reason(String[] args, boolean permaBan) {
        int index = 1;
        String reason = "";
        if(!permaBan) {
            //if this is a temp ban, the start of the "reason" would be at index 2
            index = 2;
        }
        if(args[index] == null) {
            return null;
        }

        for(int i = index ; i < args.length; i++) {
            reason = reason.concat(args[i] + " ");
        }

        return reason;
    }

    private boolean isDuration(String str) {
        return isNumeric(str.charAt(0));
    }

    private LocalDateTime parseDuration(String duration){

        String splitDuration = duration;
        char[] durationChars = duration.toCharArray();

        //Loop through string and put a '/' after letters: reason why this is here is in the loop below
        for(int i = duration.length() - 1; i >= 1; i--) {
            Character c = durationChars[i];
            if(!isNumeric(c)) {
                if(!isNumeric(durationChars[i - 1])) {
                    splitDuration = addChar(splitDuration, '/', i + 1);
                    i--;
                }
                else {
                    splitDuration = addChar(splitDuration, '/', i + 1);
                }
            }
        }

        String[] strArr = splitDuration.split("/");
        LocalDateTime time = LocalDateTime.now();

        //Loop through our previous loop's string that was set... that is now split into a string[] from the '/' we put in. look for the keywords
        //and parse the int thats in the string and add the correct amount of time.
        for(String str : strArr) {
            if(str.contains("mo")) {
                String temp = str.replace("mo", "");
                time = time.plusMonths(Integer.parseInt(temp));
            }
            else if(str.contains("d")) {
                String temp = str.replace("d", "");
                time = time.plusDays(Integer.parseInt(temp));
            }
            else if(str.contains("h")) {
                String temp = str.replace("h", "");
                time = time.plusHours(Integer.parseInt(temp));
            }
            else if(str.contains("m")) {
                String temp = str.replace("m", "");
                time = time.plusMinutes(Integer.parseInt(temp));
            }
            else if(str.contains("s")) {
                String temp = str.replace("s", "");
                time = time.plusSeconds(Integer.parseInt(temp));
            }
        }
        Bukkit.broadcastMessage(time.toString());
        return time;
    }

    private String addChar(String str, char ch, int position) {
        StringBuilder sb = new StringBuilder(str);
        sb.insert(position, ch);
        return sb.toString();
    }

    private boolean isNumeric(Character c) {
        try {
            int i = Integer.parseInt(c.toString());
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private boolean isNumeric(String str) {
        if(str == null) {
            return false;
        }
        try {
            int i = Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
