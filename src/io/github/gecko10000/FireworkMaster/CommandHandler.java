package io.github.gecko10000.FireworkMaster;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redempt.redlib.commandmanager.ArgType;
import redempt.redlib.commandmanager.CommandHook;
import redempt.redlib.commandmanager.CommandParser;

public class CommandHandler {

    private final FireworkMaster plugin;

    public CommandHandler(FireworkMaster plugin) {
        this.plugin = plugin;
        new CommandParser(plugin.getResource("command.rdcml"))
                .setArgTypes(new ArgType<FireworkObject>("firework", plugin.fireworks::get)
                        .tabStream(s -> plugin.fireworks.keySet().stream()),
                        new ArgType<World>("world", Bukkit::getWorld)
                                .tabStream(s -> Bukkit.getWorlds().stream().map(World::getName)))
                .parse().register("fireworkmaster", this);
    }

    @CommandHook("reload")
    public void reload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(Component.text("Config reloaded!", NamedTextColor.GREEN));
    }

    @CommandHook("edit")
    public void edit(Player player) {
       plugin.editGUI.open(player);
    }

    @CommandHook("launchPlayer")
    public void launchPlayer(CommandSender sender, FireworkObject firework, Player target) {
        firework.launch(target);
    }

    @CommandHook("launchLocation")
    public void launchLocation(CommandSender sender, FireworkObject firework, World world, double x, double y, double z) {
        firework.launch(new Location(world, x, y, z));
    }

}
