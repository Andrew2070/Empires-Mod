package com.andrew2070.Empires.API.Economy;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.UserIdent;
import com.forgeessentials.api.economy.Wallet;
import com.andrew2070.Empires.API.Economy.IEconManager;

import net.minecraft.entity.player.EntityPlayer;

import java.util.Map;
import java.util.UUID;

public class ForgeessentialsEconomy implements IEconManager {
    private Wallet wallet;

    public ForgeessentialsEconomy(UUID uuid) {
        this.setPlayer(uuid);
    }

    public ForgeessentialsEconomy() {

    }

    @Override
    public void setPlayer(UUID uuid) {
        this.wallet = APIRegistry.economy.getWallet(UserIdent.get(uuid));
    }

    @Override
    public void addToWallet(int amountToAdd) {
        this.wallet.add(amountToAdd);
    }

    @Override
    public int getWallet() {
        return (int) this.wallet.get();
    }

    @Override
    public boolean removeFromWallet(int amountToSubtract) {
        return this.wallet.withdraw(amountToSubtract);
    }

    @Override
    public void setWallet(int setAmount, EntityPlayer player) {
        // TODO Find some way to support this?
    }

    @Override
    public String currency(int amount) {
        return APIRegistry.economy.currency(amount);
    }

    @Override
    public String getMoneyString() {
        return this.wallet.toString();
    }

    @Override
    public void save() {
        // TODO Does this need to be implemented?
    }

    @Override
    public Map<String, Integer> getItemTables() {
        return null;
    }
}