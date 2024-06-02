package roleplayers;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.util.List;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RolePlayers extends JavaPlugin implements Listener {

    private File playerDataFile;
    private FileConfiguration playerData;
    private FileConfiguration config = getConfig();

    private Map<String, String> playerClasses = new HashMap<>();

    @Override
    public void onEnable() {
        // Save the default config file if it doesn't exist
        saveDefaultConfig();

        createPlayerDataFile();

        playerData = YamlConfiguration.loadConfiguration(playerDataFile);

        loadPlayerClasses();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("class").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                openClassSelection((Player) sender);
                return true;
            }
            return false;
        });
    }


    @Override
    public void onDisable() {
        savePlayerClasses();
    }

    private void createPlayerDataFile() {
        playerDataFile = new File(getDataFolder(), "playerData.yml");
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadPlayerClasses() {
        for (String key : playerData.getKeys(false)) {
            playerClasses.put(key, playerData.getString(key));
        }
    }

    private void savePlayerClasses() {
        for (Map.Entry<String, String> entry : playerClasses.entrySet()) {
            playerData.set(entry.getKey(), entry.getValue());
        }
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openClassSelection(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Select Your Class");

        // List to hold the enabled classes
        List<ItemStack> enabledClasses = new ArrayList<>();
        // Retrieve the chat color for the player's class from the config
        String uuid = player.getUniqueId().toString();
        String playerClass = playerClasses.get(uuid);
        String classKey = playerClass.toLowerCase();
        String chatColorCode = getConfig().getString("settings." + classKey + ".chatColor");
        ChatColor chatColor = ChatColor.valueOf(chatColorCode.toUpperCase());

        // Warrior
        if (getConfig().getInt("settings.warrior.enabled") == 1) {
            List<String> warriorLore = getConfig().getStringList("settings.warrior.description");
            ItemStack warrior = createClassItem(Material.DIAMOND_SWORD, chatColor + "Warrior", warriorLore);
            enabledClasses.add(warrior);
        }

        // Farmer
        if (getConfig().getInt("settings.farmer.enabled") == 1) {
            List<String> farmerLore = getConfig().getStringList("settings.farmer.description");
            ItemStack farmer = createClassItem(Material.WHEAT, chatColor + "Farmer", farmerLore);
            enabledClasses.add(farmer);
        }

        // Lumberjack
        if (getConfig().getInt("settings.lumberjack.enabled") == 1) {
            List<String> lumberjackLore = getConfig().getStringList("settings.lumberjack.description");
            ItemStack lumberjack = createClassItem(Material.IRON_AXE, chatColor + "Lumberjack", lumberjackLore);
            enabledClasses.add(lumberjack);
        }

        // Miner
        if (getConfig().getInt("settings.miner.enabled") == 1) {
            List<String> minerLore = getConfig().getStringList("settings.miner.description");
            ItemStack miner = createClassItem(Material.DIAMOND_PICKAXE, chatColor + "Miner", minerLore);
            enabledClasses.add(miner);
        }

        // Scout
        if (getConfig().getInt("settings.scout.enabled") == 1) {
            List<String> scoutLore = getConfig().getStringList("settings.scout.description");
            ItemStack scout = createClassItem(Material.BOW, chatColor + "Scout", scoutLore);
            enabledClasses.add(scout);
        }

        // Barbarian
        if (getConfig().getInt("settings.barbarian.enabled") == 1) {
            List<String> barbarianLore = getConfig().getStringList("settings.barbarian.description");
            ItemStack barbarian = createClassItem(Material.IRON_SWORD, chatColor + "Barbarian", barbarianLore);
            enabledClasses.add(barbarian);
        }

        // Center the enabled classes in the inventory
        int size = enabledClasses.size();
        int start = (9 - size) / 2;
        for (int i = 0; i < size; i++) {
            inv.setItem(start + i, enabledClasses.get(i));
        }

        player.openInventory(inv);


    }

    private ItemStack createClassItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }


    private void removeClassEffects(Player player, String currentClass) {
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Select Your Class")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) {
                return;
            }

            String className = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();

            String uuid = player.getUniqueId().toString();
            String costToChangeClass = getConfig().getString("settings.generic.costToChangeClass");
            String itemToChangeClass = getConfig().getString("settings.generic.itemToChangeClass").toUpperCase();

            Material itemType = Material.getMaterial(itemToChangeClass);
            int itemAmount = Integer.parseInt(costToChangeClass);

            if (itemType == null) {
                player.sendMessage("Invalid item type in config: " + itemToChangeClass);
                return;
            }

            if (playerClasses.containsKey(uuid)) {
                String currentClass = playerClasses.get(uuid);
                if (player.getInventory().containsAtLeast(new ItemStack(itemType), itemAmount)) {
                    player.getInventory().removeItem(new ItemStack(itemType, itemAmount));
                    player.sendMessage("You have paid " + itemAmount + " " + itemToChangeClass + " to reselect your class!");
                    removeClassEffects(player, currentClass); // Remove existing class-specific effects
                } else {
                    player.sendMessage("You need " + itemAmount + " " + itemToChangeClass + " to reselect your class!");
                    return;
                }
            }

            player.sendMessage("You have selected the " + className + " class!");
            playerClasses.put(uuid, className);
            savePlayerClasses();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String playerClass = playerClasses.get(uuid);

        player.sendMessage("Welcome back, " + playerClass);

        // Remove all existing potion effects
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

        // Add whatever potion effects that class has
        String effectName = getConfig().getString("settings." + playerClass + ".potionEffect");
        if (effectName != null && !effectName.isEmpty()) {
            PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
            if (effectType != null) {
                int effectLevel = getConfig().getInt("settings." + playerClass + ".potionLevel", 1) - 1;
                player.addPotionEffect(new PotionEffect(effectType, Integer.MAX_VALUE, effectLevel));
            }
        }

        int maxHealth = getConfig().getInt("settings." + playerClass.toLowerCase() + ".maxHealth");
        // Calculate the actual max health to set
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth); // Setting the new total max health

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        savePlayerClasses();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String playerClass = playerClasses.get(uuid);

        if (playerClass != null) {
            // Retrieve the chat color for the player's class from the config
            String classKey = playerClass.toLowerCase();
            String chatColorCode = getConfig().getString("settings." + classKey + ".chatColor");
            ChatColor chatColor = ChatColor.valueOf(chatColorCode.toUpperCase());

            // Modify the chat format to include the player's class with the specified color
            event.setFormat(ChatColor.GRAY + "[" + chatColor + playerClass + ChatColor.GRAY + "] " + ChatColor.RESET + "%s: %s");
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            String uuid = player.getUniqueId().toString();
            if (playerClasses.containsKey(uuid)) {
                if (playerClasses.get(uuid).equals("Warrior")) {
                    event.setDamage(event.getDamage() * 1.2); // 20% more damage
                } else if (playerClasses.get(uuid).equals("Scout") && player.getInventory().getItemInMainHand().getType() == Material.BOW) {
                    event.setDamage(event.getDamage() * 2); // Double damage with bows
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getKiller() instanceof Player) {
            Player player = entity.getKiller();
            String uuid = player.getUniqueId().toString();
            if (playerClasses.containsKey(uuid) && playerClasses.get(uuid).equals("Warrior")) {
                event.setDroppedExp((int) (event.getDroppedExp() * 1.2)); // 20% more XP from mobs
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (playerClasses.containsKey(uuid)) {
            String playerClass = playerClasses.get(uuid);
            ItemStack item = player.getInventory().getItemInMainHand();
            Material itemType = item.getType();

            // LUMBERJACK LOGIC
            if (playerClass.equals("Lumberjack")) {
                if ((event.getBlock().getType() == Material.SPRUCE_LOG
                        || event.getBlock().getType() == Material.OAK_LOG
                        || event.getBlock().getType() == Material.BIRCH_LOG
                        || event.getBlock().getType() == Material.JUNGLE_LOG
                        || event.getBlock().getType() == Material.ACACIA_LOG
                        || event.getBlock().getType() == Material.DARK_OAK_LOG
                        || event.getBlock().getType() == Material.MANGROVE_LOG
                        || event.getBlock().getType() == Material.CHERRY_LOG)
                        && (itemType == Material.STONE_AXE
                        || itemType == Material.WOODEN_AXE
                        || itemType == Material.IRON_AXE
                        || itemType == Material.GOLDEN_AXE
                        || itemType == Material.NETHERITE_AXE
                        || itemType == Material.DIAMOND_AXE)) {
                    TreeFeller(event.getBlock());
                }
                if (itemType.name().endsWith("_AXE")) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1)); // Strength II for 10 seconds
                }
            }

            // FARMER LOGIC
            if (playerClass.equals("Farmer")) {
                if ((event.getBlock().getType() == Material.WHEAT
                        || event.getBlock().getType() == Material.CARROTS
                        || event.getBlock().getType() == Material.POTATOES
                        || event.getBlock().getType() == Material.BEETROOTS
                        || event.getBlock().getType() == Material.NETHER_WART
                        || event.getBlock().getType() == Material.SUGAR_CANE
                        || event.getBlock().getType() == Material.MELON
                        || event.getBlock().getType() == Material.PUMPKIN)
                        && ((org.bukkit.block.data.Ageable) event.getBlock().getBlockData()).getAge() == 7) {
                    player.giveExp(getConfig().getInt("settings.farmer.cropXp"));
                }
            }

            // MINER LOGIC
            if (playerClass.equals("Miner")) {
                if (isHardUndergroundBlock(event.getBlock().getType())) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 200, 1)); // Haste II for 10 seconds
                }
            }
        }
    }

    private void TreeFeller(Block block) {
        int maxHeight = 256;  // Reasonable limit to prevent infinite loops
        while (block != null && block.getY() < maxHeight && isLog(block)) {
            block.breakNaturally();
            block = block.getRelative(0, 1, 0);  // Move to the block above
        }
    }

    private boolean isLog(Block block) {
        return block.getType() == Material.OAK_LOG || block.getType() == Material.SPRUCE_LOG ||
               block.getType() == Material.BIRCH_LOG || block.getType() == Material.JUNGLE_LOG ||
               block.getType() == Material.ACACIA_LOG || block.getType() == Material.DARK_OAK_LOG;
    }

    private boolean isHardUndergroundBlock(Material material) {
        return material == Material.STONE || material == Material.COBBLESTONE ||
               material == Material.NETHERRACK || material == Material.ANDESITE ||
               material == Material.GRANITE || material == Material.DIORITE ||
               material == Material.NETHER_BRICKS || material == Material.QUARTZ_BLOCK ||
               material == Material.BLACKSTONE || material == Material.BASALT ||
               material == Material.DEEPSLATE || material == Material.TUFF ||
               material == Material.COAL_ORE || material == Material.IRON_ORE ||
               material == Material.GOLD_ORE || material == Material.DIAMOND_ORE ||
               material == Material.EMERALD_ORE || material == Material.LAPIS_ORE ||
               material == Material.REDSTONE_ORE || material == Material.NETHER_GOLD_ORE ||
               material == Material.NETHER_QUARTZ_ORE;
    }
}
