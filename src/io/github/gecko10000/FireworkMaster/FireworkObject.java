package io.github.gecko10000.FireworkMaster;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import redempt.redlib.configmanager.annotations.ConfigMappable;
import redempt.redlib.configmanager.annotations.ConfigPath;
import redempt.redlib.configmanager.annotations.ConfigValue;

@ConfigMappable
public class FireworkObject {

    @ConfigPath
    String name;

    @ConfigValue
    FireworkMeta meta;

    @ConfigValue
    boolean instant = false;

    public FireworkObject(String name, FireworkMeta meta) {
        this.name = name;
        this.meta = meta;
    }

    protected FireworkObject() {}

    public void launch(Player player) {
        launch(player.getLocation());
    }

    public void launch(Location location) {
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        firework.setFireworkMeta(meta);
        if (instant) {
            firework.detonate();
        }
    }

}
