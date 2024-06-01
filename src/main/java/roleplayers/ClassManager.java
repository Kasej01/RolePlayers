package roleplayers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import java.util.HashMap;

public class ClassManager {
    private static final HashMap<Player, String> playerClasses = new HashMap<>();

    public static ItemStack createItemWithName(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public static void assignClass(Player player, String className) {
        playerClasses.put(player, className);
        player.sendMessage(ChatColor.GREEN + "You have chosen the " + className + " class!");
    }

    public static boolean isWarrior(Player player) {
        return playerClasses.getOrDefault(player, "").equals(ChatColor.GOLD + "Warrior");
    }

    public static boolean isMiner(Player player) {
        return playerClasses.getOrDefault(player, "").equals(ChatColor.BLACK + "Miner");
    }

    public static boolean isScout(Player player){
        return playerClasses.getOrDefault(player, "").equals(ChatColor.GRAY + "Scout");
    }

    public static boolean isBarbarian(Player player){
        return playerClasses.getOrDefault(player, "").equals(ChatColor.RED + "Barbarian");
    }

    public static boolean isLumberjack(Player player){
        return playerClasses.getOrDefault(player, "").equals(ChatColor.GREEN + "Lumberjack");
    }
}
