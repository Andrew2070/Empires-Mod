package com.EmpireMod.Empires.API.Economy;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Economy implementation for the Vault API
 */
public class VaultEconomy implements IEconManager {
	public static VaultEconMain econ;

	private OfflinePlayer player;

	public VaultEconomy(UUID uuid) {
		player = Bukkit.getServer().getOfflinePlayer(uuid);
	}

	public VaultEconomy() {
	}

	@Override
	public void setPlayer(UUID uuid) {
		player = Bukkit.getServer().getOfflinePlayer(uuid);
	}

	@Override
	public void addToWallet(int amountToAdd) {
		econ.depositPlayer(player, amountToAdd);
	}

	@Override
	public int getWallet() {
		return (int) econ.getBalance(player);
	}

	@Override
	public boolean removeFromWallet(int amountToSubtract) {
		return econ.withdrawPlayer(player, amountToSubtract).transactionSuccess();
	}

	@Override
	public void setWallet(int setAmount, EntityPlayer player) {
		// TODO Find some way to support this?
	}

	@Override
	public String currency(int amount) {
		return econ.format(amount);
	}

	@Override
	public String getMoneyString() {
		return currency(getWallet());
	}

	@Override
	public void save() {
	}

	@Override
	public Map<String, Integer> getItemTables() {
		return null;
	}
}