package com.hiddentech.blockdrops;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class BlockDrops extends JavaPlugin implements CommandExecutor, Listener {

    private Set<UUID> notBlockingDrops = new HashSet<>();
    private String enableString;
    private String disableString;
    private String errorString;
    private Set<UUID> playerDropItemOnTick = new HashSet<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(this,this);
        this.getCommand("drop").setExecutor(this);

        if(!this.getConfig().contains("Enable"))
            this.getConfig().set("Enable","&3Enabled Dropping");
        if(!this.getConfig().contains("Disable"))
            this.getConfig().set("Disable","&3Disabled Dropping");
        if(!this.getConfig().contains("NotAllowed"))
            this.getConfig().set("NotAllowed","&4Do /drop to enable dropping items!");
        this.saveConfig();
        this.enableString=ChatColor.translateAlternateColorCodes('&',this.getConfig().getString("Enable"));
        this.disableString=ChatColor.translateAlternateColorCodes('&',this.getConfig().getString("Disable"));
        this.errorString=ChatColor.translateAlternateColorCodes('&',this.getConfig().getString("NotAllowed"));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    @EventHandler
    public void playerJoin(PlayerJoinEvent event){
        this.notBlockingDrops.remove(event.getPlayer().getUniqueId());
    }
    @EventHandler
    public void playerLeave(PlayerQuitEvent event){
        this.notBlockingDrops.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void playerDrop(InventoryClickEvent event){
        if(event.getClickedInventory()==null){
            playerDropItemOnTick.add(event.getWhoClicked().getUniqueId());
            this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> playerDropItemOnTick.remove(event.getWhoClicked().getUniqueId()), 2L);
        }
    }

    @EventHandler
    public void playerDrop(InventoryCloseEvent event){
            playerDropItemOnTick.add(event.getPlayer().getUniqueId());
            this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> playerDropItemOnTick.remove(event.getPlayer().getUniqueId()), 2L);

    }

    @EventHandler
    public void playerDrop(PlayerDropItemEvent event){
        if(playerDropItemOnTick.contains(event.getPlayer().getUniqueId()))return;
        if(this.notBlockingDrops.contains(event.getPlayer().getUniqueId()))return;
        event.getPlayer().sendMessage(this.errorString);
        event.setCancelled(true);
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            if (this.notBlockingDrops.contains(((Player) sender).getUniqueId())){
                this.notBlockingDrops.remove(((Player) sender).getUniqueId());
                sender.sendMessage(ChatColor.GRAY+enableString);
                return true;
            }
            this.notBlockingDrops.add(((Player) sender).getUniqueId());
            sender.sendMessage(ChatColor.GRAY+disableString);
        }

        return true;
    }
}
