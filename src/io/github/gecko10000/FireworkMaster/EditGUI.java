package io.github.gecko10000.FireworkMaster;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import redempt.redlib.configmanager.annotations.ConfigValue;
import redempt.redlib.inventorygui.InventoryGUI;
import redempt.redlib.inventorygui.ItemButton;
import redempt.redlib.inventorygui.PaginationPanel;
import redempt.redlib.itemutils.ItemBuilder;
import redempt.redlib.itemutils.ItemUtils;
import redempt.redlib.misc.ChatPrompt;
import redempt.redlib.misc.EventListener;
import redempt.redlib.misc.Task;

import java.util.concurrent.CompletableFuture;

public class EditGUI {

    @ConfigValue("gui.size")
    private int size = 54;

    @ConfigValue("gui.name")
    private String guiName = "&3Firework Editor";

    @ConfigValue("gui.prev-button")
    private ItemStack prevButton = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
            .setName(ChatColor.RED + "Previous Page");

    @ConfigValue("gui.new-button")
    private ItemStack newButton = new ItemBuilder(Material.GREEN_STAINED_GLASS)
            .setName(ChatColor.GREEN + "Create Firework");

    @ConfigValue("gui.next-button")
    private ItemStack nextButton = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
            .setName(ChatColor.GREEN + "Next Page");

    private final FireworkMaster plugin;
    private final InventoryGUI gui;
    private final PaginationPanel panel;

    public EditGUI(FireworkMaster plugin) {
        this.plugin = plugin;
        gui = new InventoryGUI(Bukkit.createInventory(null, size, LegacyComponentSerializer.legacyAmpersand().deserialize(guiName)));
        panel = new PaginationPanel(gui);
        init();
    }

    private void init() {
        gui.setDestroyOnClose(false);
        gui.setReturnsItems(false);
        gui.addButton(size - 6, ItemButton.create(prevButton, evt -> panel.prevPage()));
        gui.addButton(size - 5, ItemButton.create(newButton, evt -> {
            Player player = (Player) evt.getWhoClicked();
            Task.syncDelayed((Runnable) player::closeInventory);
            ChatPrompt.prompt(player,
                    ChatColor.GREEN + "Enter the name for the firework, or \"cancel\" to cancel.",
                    response -> enterEditMode(player, response).thenAccept(f -> {
                        if (f != null) {
                            plugin.fireworks.put(f.name, f);
                        }
                        open(player);
                        update();
                        plugin.config.save();
                    }),
                    reason -> {
                        if (reason == ChatPrompt.CancelReason.PLAYER_CANCELLED) {
                            open(player);
                        }
                    });
        }));
        gui.addButton(size - 4, ItemButton.create(nextButton, evt -> panel.nextPage()));
        panel.addSlots(0, size - 9);
        Task.syncDelayed(this::update);
    }

    public void open(Player player) {
        gui.open(player);
    }

    public void update() {
        panel.clear();
        plugin.fireworks.values().stream()
                .map(this::fireworkButton)
                .forEach(panel::addPagedButton);
    }

    public ItemButton fireworkButton(FireworkObject object) {
        ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET);
        firework.setItemMeta(object.meta);
        ItemStack finalFirework = new ItemBuilder(firework)
                .setName(ChatColor.WHITE + object.name)
                .setLore("", ChatColor.AQUA + "Instant: " + object.instant, "",
                        ChatColor.YELLOW + "Left click to set the firework item",
                        ChatColor.YELLOW + "Right click to toggle instant explosion",
                        ChatColor.YELLOW + "Middle click to get the firework item",
                        ChatColor.RED + "Shift click to delete");
        return ItemButton.create(finalFirework, evt -> {
            switch (evt.getClick()) {
                case MIDDLE -> ItemUtils.give((Player) evt.getWhoClicked(), firework);
                case RIGHT -> object.instant = !object.instant;
                case LEFT -> {
                    Player player = (Player) evt.getWhoClicked();
                    player.sendMessage(ChatColor.GREEN + "Enter the name for the firework, or \"cancel\" to cancel.");
                    enterEditMode(player, object.name).thenAccept(f -> {
                        if (f != null) {
                            plugin.fireworks.put(f.name, f);
                            update();
                        }
                        gui.open(player);
                    });
                }
                case SHIFT_LEFT, SHIFT_RIGHT -> {
                    plugin.fireworks.remove(object.name);
                    update();
                }
            }
            update();
            plugin.config.save();
        });
    }

    public CompletableFuture<FireworkObject> enterEditMode(Player player, String fireworkName) {
        Task.syncDelayed((Runnable) player::closeInventory);
        CompletableFuture<FireworkObject> future = new CompletableFuture<>();
        new EventListener<>(PlayerDropItemEvent.class, (l, evt) -> {
            if (future.isDone() || !player.isOnline()) {
                l.unregister();
                future.complete(null);
                return;
            }
            if (!evt.getPlayer().equals(player)) {
                return;
            }
            evt.setCancelled(true);
            ItemStack item = evt.getItemDrop().getItemStack();
            if (item.getType() != Material.FIREWORK_ROCKET) {
                player.sendMessage(Component.text("Drop a firework rocket, or type \"cancel\" to exit!", NamedTextColor.RED));
                return;
            }
            future.complete(new FireworkObject(fireworkName, (FireworkMeta) item.getItemMeta()));
            l.unregister();
        });
        new EventListener<>(AsyncChatEvent.class, (l, evt) -> {
            if (future.isDone() || !player.isOnline()) {
                l.unregister();
                future.complete(null);
                return;
            }
            if (!evt.getPlayer().equals(player)) {
                return;
            }
            evt.setCancelled(true);
            if (!PlainTextComponentSerializer.plainText().serialize(evt.message()).equalsIgnoreCase("cancel")) {
                player.sendMessage(Component.text("Drop a firework rocket, or type \"cancel\" to exit!", NamedTextColor.RED));
                return;
            }
            future.complete(null);
            l.unregister();
        });
        return future;
    }



}
