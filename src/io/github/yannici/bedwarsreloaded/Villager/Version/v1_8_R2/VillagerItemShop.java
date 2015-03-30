package io.github.yannici.bedwarsreloaded.Villager.Version.v1_8_R2;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.server.v1_8_R2.EntityHuman;
import net.minecraft.server.v1_8_R2.EntityVillager;
import net.minecraft.server.v1_8_R2.MerchantRecipe;
import net.minecraft.server.v1_8_R2.MerchantRecipeList;
import net.minecraft.server.v1_8_R2.StatisticList;

import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Utils;
import io.github.yannici.bedwarsreloaded.Game.Game;
import io.github.yannici.bedwarsreloaded.Villager.MerchantCategory;

public class VillagerItemShop {
    
    private Game game = null;
    private Player player = null;
    private MerchantCategory category = null;

    public VillagerItemShop(Game g, Player p, MerchantCategory category) {
        this.game = g;
        this.player = p;
        this.category = category;
    }
    
    private EntityVillager createVillager() {
        try {
            EntityVillager ev = new EntityVillager(((CraftWorld)this.game.getRegion().getWorld()).getHandle());
            Field careerField = EntityVillager.class.getDeclaredField("by");
            careerField.setAccessible(true);
            careerField.set(ev, Integer.valueOf(10));
            
            return ev;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    private EntityHuman getEntityHuman() {
        try {
            return ((CraftPlayer)this.player).getHandle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void openTrading() {
        // As task because of inventory issues
        new BukkitRunnable() {
            
            @Override
            public void run() {
                try {
                EntityVillager entityVillager = VillagerItemShop.this.createVillager();
                EntityHuman entityHuman = VillagerItemShop.this.getEntityHuman();
                
                // set location
                MerchantRecipeList recipeList = entityVillager.getOffers(entityHuman);
                recipeList.clear();
                
                for(io.github.yannici.bedwarsreloaded.Villager.VillagerTrade trade : VillagerItemShop.this.category.getOffers()) {
                    ItemStack reward = trade.getRewardItem();
                    Method colorable = Utils.getColorableMethod(reward.getType());
                    if(colorable != null) {
                        ItemMeta meta = reward.getItemMeta();
                        colorable.setAccessible(true);
                        colorable.invoke(meta, new Object[]{Game.getPlayerTeam(VillagerItemShop.this.player, VillagerItemShop.this.game).getColor().getColor()});
                        reward.setItemMeta(meta);
                    }
                    
                    if(!(trade.getHandle().getInstance() instanceof MerchantRecipe)) {
                        continue;
                    }
                    
                    MerchantRecipe recipe = (MerchantRecipe)trade.getHandle().getInstance();
                    recipe.a(1000);
                    recipeList.add(recipe);
                }

                entityVillager.a_(entityHuman);
                ((CraftPlayer)player).getHandle().openTrade(entityVillager);
                ((CraftPlayer)player).getHandle().b(StatisticList.F);
                
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.runTask(Main.getInstance());
    }

}