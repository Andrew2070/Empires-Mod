package com.andrew2070.Empires.Proxies;


import com.andrew2070.Empires.API.Economy.Economy;

import com.andrew2070.Empires.Empires;

import com.andrew2070.Empires.Config.Config;

public class EconomyProxy {

    private static Economy economy = null;

    private EconomyProxy() {
    }

    public static void init() {
        economy = new Economy(Config.instance.costItemName.get());
        Empires.instance.LOG.info("Successfully initialized economy!");
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static boolean isItemEconomy() {
        if(Config.instance.costItemName.get().equals(Economy.CURRENCY_FORGE_ESSENTIALS)) {
            return false;
        } else if(Config.instance.costItemName.get().equals(Economy.CURRENCY_VAULT)) {
            return false;
        } else if(Config.instance.costItemName.get().startsWith(Economy.CURRENCY_CUSTOM)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a formatted currency string. For example: "32 Diamonds" or "15 $"
     */
    public static String getCurrency(int amount) {
        String currency = economy.getCurrency(amount);
        if(Character.isDigit(currency.charAt(0)) || Character.isDigit(currency.charAt(currency.length() - 1)))
            return currency;
        else
            return amount + " " + currency;
    }
}