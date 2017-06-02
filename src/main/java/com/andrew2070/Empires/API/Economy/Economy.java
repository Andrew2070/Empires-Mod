package com.andrew2070.Empires.API.Economy;


import cpw.mods.fml.common.Loader;
import com.andrew2070.Empires.Empires;
import com.andrew2070.Empires.exceptions.EconomyException;
import com.andrew2070.Empires.utils.ClassUtils;
import com.andrew2070.Empires.utils.ItemUtils;
import com.andrew2070.Empires.utils.PlayerUtils;


import com.andrew2070.Empires.API.Economy.ForgeessentialsEconomy;
import com.andrew2070.Empires.API.Economy.BukkitCompat;



import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public class Economy {
    public static final String CURRENCY_VAULT = "$Vault";
    public static final String CURRENCY_FORGE_ESSENTIALS = "$ForgeEssentials";
    public static final String CURRENCY_CUSTOM = "$Custom:";

    private String costItemName;
    private Class<? extends IEconManager> econManagerClass;

    public Economy(String costItemName) {
        this.costItemName = costItemName;
        if(costItemName.equals(CURRENCY_VAULT)) {
            if (ClassUtils.isBukkitLoaded()) {
                econManagerClass = BukkitCompat.initEconomy();
            }
            if(econManagerClass == null)
                throw new EconomyException("Failed to initialize Vault economy!");
        } else if(costItemName.equals(CURRENCY_FORGE_ESSENTIALS)) {
            if(Loader.isModLoaded("ForgeEssentials"))
                econManagerClass = ForgeessentialsEconomy.class;
            if(econManagerClass == null)
                throw new EconomyException("Failed to initialize ForgeEssentials economy!");
        } else if(costItemName.startsWith(CURRENCY_CUSTOM)) {
            try{
                econManagerClass = Class.forName(costItemName.substring(CURRENCY_CUSTOM.length())).asSubclass(IEconManager.class);
            }
            catch (Exception e){
                throw new EconomyException("Failed to initialize custom economy!", e);
            }
        }
    }

    public IEconManager economyManagerForUUID(UUID uuid) {
        if (econManagerClass == null) {
            return null;
        }
        try {
            IEconManager manager = econManagerClass.newInstance();
            manager.setPlayer(uuid);
            return manager;
        } catch(Exception ex) {
            Empires.instance.LOG.info("Failed to create IEconManager", ex);
        }

        return null; // Hopefully this doesn't break things...
    }

    /**
     * Takes the amount of money specified.
     * Returns false if player doesn't have the money necessary
     */
    public boolean takeMoneyFromPlayer(EntityPlayer player, int amount) {
        if(costItemName.equals(CURRENCY_FORGE_ESSENTIALS) || costItemName.equals(CURRENCY_VAULT) || costItemName.startsWith(CURRENCY_CUSTOM)) {
            IEconManager eco = economyManagerForUUID(player.getUniqueID());
            if (eco == null)
                return false;
            int wallet = eco.getWallet();
            if (wallet >= amount) {
                eco.removeFromWallet(amount);
                return true;
            }
            return false;
        } else {
            return PlayerUtils.takeItemFromPlayer(player, costItemName, amount);
        }
    }

    /**
     * Takes the amount of money specified.
     * Returns false if player doesn't have the money necessary
     */
    public void giveMoneyToPlayer(EntityPlayer player, int amount) {
        if (costItemName.equals(CURRENCY_FORGE_ESSENTIALS) || costItemName.equals(CURRENCY_VAULT) || costItemName.startsWith(CURRENCY_CUSTOM)) {
            IEconManager eco = economyManagerForUUID(player.getUniqueID());
            if (eco == null)
                return;
            eco.addToWallet(amount);
        } else {
            PlayerUtils.giveItemToPlayer(player, costItemName, amount);
        }
    }

    /**
     * Gets the currency string currently used.
     */
    public String getCurrency(int amount) {
        if(costItemName.equals(CURRENCY_FORGE_ESSENTIALS) || costItemName.equals(CURRENCY_VAULT) || costItemName.startsWith(CURRENCY_CUSTOM)) {
            if (econManagerClass == null) {
                return null;
            }
            try {
                IEconManager manager = econManagerClass.newInstance();
                return manager.currency(amount);
            } catch(Exception ex) {
                Empires.instance.LOG.info("Failed to create IEconManager", ex);
            }
            return "$";

        } else {
            return ItemUtils.itemStackFromName(costItemName).getDisplayName() + (amount == 1 ? "" : "s");
        }
    }
}