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

public class RolePlayers extends JavaPlugin implements CommandExecutor, Listener {
    private HashMap<UUID, String> playerClasses;

    @Override
    public void onEnable() {
        playerClasses = new HashMap<>();
        this.getCommand("class").setExecutor(this);
        this.getCommand("changeclass").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new AxeStrengthRunnable(this), 0L, 100L);
        getLogger().info("RolePlayers plugin enabled and HashMap initialized.");
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by players.");
            return true;
        }
        Player player = (Player) sender;

        if ("class".equalsIgnoreCase(cmd.getName())) {
            if (playerClasses.containsKey(player.getUniqueId())) {
                // Notify the player about the need to confirm class change
                player.sendMessage(ChatColor.YELLOW + "You already have a class. Changing classes costs 5 diamonds. Type /confirm to proceed.");
                return true;
            } else {
                // Open the class selection inventory for first-time class selection
                openClassSelectionInventory(player);
                return true;
            }
        } else if ("changeclass".equalsIgnoreCase(cmd.getName())) {
            // Handle the confirmation command
            if (playerClasses.containsKey(player.getUniqueId())) {
                if (player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), 5)) {
                    player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 5)); // Deduct diamonds
                    player.sendMessage(ChatColor.GREEN + "5 diamonds have been deducted from your inventory.");
                    openClassSelectionInventory(player); // Open inventory to choose a new class
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You need at least 5 diamonds to change your class.");
                    return true;
                }
            } else {
                player.sendMessage(ChatColor.RED + "You do not have a class to change!");
                return true;
            }
        }

        return false; // Return false if neither command is recognized
    }


    private void openClassSelectionInventory(Player player) {
        Inventory classSelection = Bukkit.createInventory(null, 9, "Select Your Class");
        classSelection.setItem(2, ClassManager.createItemWithName(Material.STONE_SWORD, ChatColor.GOLD + "Warrior"));
        classSelection.setItem(3, ClassManager.createItemWithName(Material.BOW, ChatColor.GRAY + "Scout"));
        classSelection.setItem(4, ClassManager.createItemWithName(Material.STONE_AXE, ChatColor.GREEN + "Lumberjack"));
        classSelection.setItem(5, ClassManager.createItemWithName(Material.STONE_PICKAXE, ChatColor.BLACK + "Miner"));
        classSelection.setItem(6, ClassManager.createItemWithName(Material.LEATHER_CHESTPLATE, ChatColor.RED + "Barbarian"));
        player.openInventory(classSelection);
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder != null || !event.getView().getTitle().equals("Select Your Class")) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        if(clickedItem.getType() == Material.LEATHER_CHESTPLATE && player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() < 21){
            addHearts(player, 4);
        }
        if(clickedItem.getType() != Material.LEATHER_CHESTPLATE){
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        }

        if(clickedItem.getType() != Material.BOW){
            player.removePotionEffect(PotionEffectType.SPEED);
        }

        else if(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() > 20 && clickedItem.getType() == Material.LEATHER_CHESTPLATE){
            player.sendMessage(ChatColor.RED + "You already have max health");
        }

        String itemName = clickedItem.getItemMeta().getDisplayName();
        playerClasses.put(player.getUniqueId(), itemName);
        
        ClassManager.assignClass(player, itemName);
        player.sendMessage(ChatColor.GREEN + "Class selected: " + itemName);
        player.closeInventory();  // Close the inventory after the selection
    }

    public void addHearts(Player player, int amount){
        double currentHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(currentHealth + amount);
    }


    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if ((event.getDamager() instanceof Player) || event.getDamager() instanceof Arrow) {
            //Scout does twice as much damage with a bow as normal players
            if(event.getDamager() instanceof Arrow){
                Arrow arrow = (Arrow) event.getDamager();
                Player player = (Player) arrow.getShooter();
                if(ClassManager.isScout(player) && arrow.getShooter() instanceof Player){
                    double originalDamage = event.getDamage();
                    double increasedDamage = originalDamage * 2;
                    event.setDamage(increasedDamage);
                }
            }

            //Warrior does 1.2 times as much damage as normal players
            Player player = (Player) event.getDamager();
            if (ClassManager.isWarrior(player)) {
                double originalDamage = event.getDamage();
                double increasedDamage = originalDamage * 1.20; // Calculate the increased damage
                event.setDamage(increasedDamage);
                // Send feedback to the player
            }
            return;
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        ItemStack item = player.getInventory().getItemInMainHand();
            switch (item.getType()) {
                case STONE_PICKAXE:
                case WOODEN_PICKAXE:
                case IRON_PICKAXE:
                case DIAMOND_PICKAXE:
                case GOLDEN_PICKAXE:
                case NETHERITE_PICKAXE:
                    if (isMinable(block.getType())) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 30, 1, true, false, true));
                    }
                    break;
                default:
                    break;
            }

        // Check if the block is a log and the player is a Lumberjack
        if (isLog(block.getType()) && ClassManager.isLumberjack(player)
        && item.getType() == Material.STONE_AXE
        || item.getType() == Material.WOODEN_AXE
        || item.getType() == Material.IRON_AXE
        || item.getType() == Material.GOLDEN_AXE
        || item.getType() == Material.NETHERITE_AXE
        || item.getType() == Material.DIAMOND_AXE) {
            fellTree(block);
            event.setCancelled(true);  // Cancel the event to handle tree felling in fellTree
        }
    }

    private boolean isLog(Material material) {
        switch (material) {
            case OAK_LOG:
            case SPRUCE_LOG:
            case BIRCH_LOG:
            case JUNGLE_LOG:
            case ACACIA_LOG:
            case DARK_OAK_LOG:
            case MANGROVE_LOG:
                return true;
            default:
                return false;
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();

        if (item == null || block == null) return;  // Ensure item and block are not null

        // Check if the action is LEFT_CLICK_BLOCK and player is a Miner
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && ClassManager.isMiner(player)) {
            // Check if the item is a pickaxe
            switch (item.getType()) {
                case STONE_PICKAXE:
                case WOODEN_PICKAXE:
                case IRON_PICKAXE:
                case DIAMOND_PICKAXE:
                case GOLDEN_PICKAXE:
                case NETHERITE_PICKAXE:
                    if (isMinable(block.getType())) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 30, 1, true, false, true));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isMinable(Material material) {
        switch (material) {
            case STONE:
            case COBBLESTONE:
            case MOSSY_COBBLESTONE:
            case ANDESITE:
            case POLISHED_ANDESITE:
            case DIORITE:
            case POLISHED_DIORITE:
            case GRANITE:
            case POLISHED_GRANITE:
            case SANDSTONE:
            case RED_SANDSTONE:
            case SMOOTH_RED_SANDSTONE:
            case BASALT:
            case POLISHED_BASALT:
            case SMOOTH_BASALT:
            case BLACKSTONE:
            case POLISHED_BLACKSTONE:
            case DEEPSLATE:
            case COBBLED_DEEPSLATE:
            case POLISHED_DEEPSLATE:
            case TUFF:
            case CALCITE:
            case CLAY:
            case COAL_ORE:
            case COPPER_ORE:
            case IRON_ORE:
            case REDSTONE_ORE:
            case EMERALD_ORE:
            case LAPIS_ORE:
            case DIAMOND_ORE:
            case NETHER_QUARTZ_ORE:
            case ANCIENT_DEBRIS:
            case NETHERRACK:
                return true;
            default:
                return false;
        }
    }

    

    private void fellTree(Block block) {
        int maxHeight = 256;  // Reasonable limit to prevent infinite loops
        while (block != null && block.getY() < maxHeight && isLog(block.getType())) {
            block.breakNaturally();
            block = block.getRelative(0, 1, 0);  // Move to the block above
        }
    }



}
