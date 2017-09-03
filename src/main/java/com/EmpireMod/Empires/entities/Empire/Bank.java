package com.EmpireMod.Empires.entities.Empire;

import java.util.ArrayList;

import com.EmpireMod.Empires.Empires;
import com.EmpireMod.Empires.Configuration.Config;

public class Bank {

	private Empire empire;

	private int amount = 0;
	private int daysNotPaid = 0;

	public Bank(Empire empire) {
		this.empire = empire;
	}

	public boolean makePayment(int amount) {
		if (this.amount >= amount) {
			this.amount -= amount;
			return true;
		}
		return false;
	}

	public void payUpkeep() {
		int amount = getNextPaymentAmount();
		if (makePayment(amount)) {
			daysNotPaid = 0;
			empire.notifyEveryone(Empires.instance.LOCAL.getLocalization("Empires.notification.empire.upkeep"));
		} else {
			daysNotPaid++;
			empire.notifyEveryone(Empires.instance.LOCAL.getLocalization("Empires.notification.empire.upkeep.failed",
					Config.instance.upkeepEmpireDeletionDays.get() - daysNotPaid));
		}
	}

	public Empire getEmpire() {
		return empire;
	}

	public void setDaysNotPaid(int days) {
		this.daysNotPaid = days;
	}

	public int getDaysNotPaid() {
		return this.daysNotPaid;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getAmount() {
		return this.amount;
	}

	public void addAmount(int amount) {
		this.amount += amount;
	}

	public int getNextPaymentAmount() {
		return (Config.instance.costEmpireUpkeep.get()
				+ Config.instance.costAdditionalUpkeep.get() * empire.empireBlocksContainer.size()) * (1 + daysNotPaid);
	}

	public static class Container extends ArrayList<Bank> {

		public Bank get(Empire empire) {
			for (Bank bank : this) {
				if (bank.getEmpire() == empire) {
					return bank;
				}
			}
			return null;
		}

		public boolean contains(Empire empire) {
			for (Bank bank : this) {
				if (bank.getEmpire() == empire) {
					return true;
				}
			}
			return false;
		}
	}
}