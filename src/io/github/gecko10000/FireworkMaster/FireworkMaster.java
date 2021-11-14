package io.github.gecko10000.FireworkMaster;

import org.bukkit.plugin.java.JavaPlugin;
import redempt.redlib.configmanager.ConfigManager;
import redempt.redlib.configmanager.annotations.ConfigValue;

import java.util.HashMap;
import java.util.Map;

public class FireworkMaster extends JavaPlugin {

    @ConfigValue
    public Map<String, FireworkObject> fireworks = new HashMap<>();
    public ConfigManager config;
    public EditGUI editGUI;

    public void onEnable() {
        reload();
        new CommandHandler(this);
    }

    public void reload() {
        editGUI = new EditGUI(this);
        config = new ConfigManager(this)
                .register(editGUI, this).saveDefaults().load();
    }

}
