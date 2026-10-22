package com.mahin;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Scanner;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
public final class Plugin extends JavaPlugin implements Listener {
    BukkitTask ShutdownTask = null;
    private void shutdown() {
        getLogger().info("Server has been empty for " + getConfig().getInt("time") + " seconds");
        List<String> commands = getConfig().getStringList("commands");
        int CommandCount = commands.size();
        if (CommandCount != 0) {
            getLogger().info("Executing " + CommandCount + " shutdown commands");
            for (int i=0; i<CommandCount; i++) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commands.get(i));
            }
        }
        else {
            getLogger().info("No shutdown commands configured in config");
        }
        getLogger().info("Shutting server down");
        Bukkit.shutdown();
    }
    @Override
    public void onEnable() {
        String VersionString = "";
        int VersionInteger = 0;
        HttpURLConnection GithubRequest = null;
        Scanner scanner = null;
        try {
            GithubRequest = (HttpURLConnection) new URI("https://api.github.com/repos/MahinBro001238/AutoShutdown/releases/latest").toURL().openConnection();
            GithubRequest.setConnectTimeout(2500);
            GithubRequest.setReadTimeout(2500);
            scanner = new Scanner(GithubRequest.getInputStream());
            VersionString = scanner.useDelimiter("\\A").next().split("\"tag_name\":\"v")[1].split("\"")[0];
            scanner.close();
            GithubRequest.disconnect();
            VersionInteger = Integer.parseInt(VersionString.replace(".", ""));
        }
        catch (Exception e) {
            if (GithubRequest != null) {
                GithubRequest.disconnect();
            }
            if (scanner != null) {
                scanner.close();
            }
        }
        String UpdateStatus;
        if (VersionInteger > 10) {
            UpdateStatus = "There is a new update: v" + VersionString;
        }
        else if (VersionInteger == 10) {
            UpdateStatus = "You are using the latest version";
        }
        else {
            UpdateStatus = "Unable to check for updates";
        }
        saveDefaultConfig();
        new Metrics(this, 34854);
        Bukkit.getPluginManager().registerEvents(this, this);
        ShutdownTask = Bukkit.getScheduler().runTaskLater(this, this::shutdown, getConfig().getInt("time") * 20L);
        getLogger().info("=============================================");
        getLogger().info("Auto Shutdown v1.0");
        getLogger().info("Made by MahinBro001238");
        getLogger().info("Licensed under the Apache License 2.0");
        getLogger().info("For support: https://discord.gg/8yrbGUCFFD");
        getLogger().info(UpdateStatus);
        getLogger().info("Auto Shutdown has been enabled");
        getLogger().info("If no players join for the next " + getConfig().getInt("time") + " seconds, server will shut down");
        getLogger().info("=============================================");
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (ShutdownTask != null) {
            ShutdownTask.cancel();
            ShutdownTask = null;
            getLogger().info("A player joined, shutdown cancelled");
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (Bukkit.getOnlinePlayers().size() == 1) {
            getLogger().info("Server is empty, shutting down in " + getConfig().getInt("time") + " seconds if no one joins");
            ShutdownTask = Bukkit.getScheduler().runTaskLater(this, this::shutdown, getConfig().getInt("time") * 20L);
        }
    }
    @Override
    public void onDisable() {
        if (ShutdownTask != null) {
            ShutdownTask.cancel();
        }
        getLogger().info("Auto Shutdown v1.0 has been disabled");
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player CommandExecutor = (Player) sender;
            if (CommandExecutor.hasPermission("autoshutdown.reload")) {
                reloadConfig();
                CommandExecutor.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully reloaded config."));

            }
            else {
                CommandExecutor.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("no_permission_message")));
            }
        }
        else {
            reloadConfig();
            sender.sendMessage("Successfully reloaded config.");
            if (Bukkit.getOnlinePlayers().size() == 0) {
                ShutdownTask.cancel();
                ShutdownTask = Bukkit.getScheduler().runTaskLater(this, this::shutdown, getConfig().getInt("time") * 20L);
                sender.sendMessage("If no players join for the next " + getConfig().getInt("time") + " seconds, server will shut down");
            }
        }
        return true;
    }
}