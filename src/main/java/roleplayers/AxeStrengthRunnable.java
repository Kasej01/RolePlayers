package roleplayers;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AxeStrengthRunnable implements Runnable {
    private JavaPlugin plugin;

    public AxeStrengthRunnable(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item != null && item.getType() == Material.STONE_AXE && ClassManager.isLumberjack(player)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 1, true, true), true);
            } else {
                player.removePotionEffect(PotionEffectType.STRENGTH);
            }
        }
    }
}
