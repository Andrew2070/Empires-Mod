package EmpiresMod.entities.Signs;

import java.util.UUID;

import EmpiresMod.Empires;
import EmpiresMod.API.Chat.Component.ChatManager;
import EmpiresMod.Datasource.EmpiresUniverse;
import EmpiresMod.Localization.Localization;
import EmpiresMod.Proxies.EconomyProxy;
import EmpiresMod.Transformers.SignClassTransformer;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Plot;
import EmpiresMod.entities.Misc.Sign;
import EmpiresMod.entities.Misc.SignType;
import EmpiresMod.entities.Position.BlockPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;

public class SellSign extends Sign {
	private int price;
	private boolean restricted;
	private Citizen owner;
	private Plot plot;

	public SellSign(BlockPosition bp, int face, Citizen owner, int price, boolean restricted) {
		super(SellSignType.instance);
		this.bp = bp;
		this.price = price;
		this.restricted = restricted;
		this.plot = EmpiresUniverse.instance.plots.get(bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
		// Changed to EmpiresUniverse.instance from Empires.instance in above
		// lines^
		this.owner = owner;
		NBTTagCompound data = new NBTTagCompound();
		data.setString("Owner", owner.getUUID().toString());
		data.setInteger("Price", price);
		data.setBoolean("Restricted", restricted);
		this.data = data;
		createSignBlock(owner.getPlayer(), bp, face);
	}

	public SellSign(TileEntitySign te, NBTTagCompound signData) {
		super(SellSignType.instance);
		this.bp = new BlockPosition(te.xCoord, te.yCoord, te.zCoord, te.getWorldObj().provider.dimensionId);
		this.owner = EmpiresUniverse.instance.getOrMakeCitizen(UUID.fromString(signData.getString("Owner")));
		this.price = signData.getInteger("Price");
		this.restricted = signData.getBoolean("Restricted");
		this.plot = EmpiresUniverse.instance.plots.get(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord,
				te.zCoord);
	}

	// Changed to EmpiresUniverse.instance from Empires.instance in above lines^

	@Override
	public void onRightClick(EntityPlayer player) {
		Citizen citizen = EmpiresUniverse.instance.getOrMakeCitizen(player);
		if (restricted && !plot.getEmpire().citizensMap.containsKey(citizen)) {
			ChatManager.send(player, "Empires.cmd.err.notInEmpire", plot.getEmpire());
			return;
		}

		if (plot.ownersContainer.contains(citizen)) {
			ChatManager.send(player, "Empires.cmd.err.plot.sell.alreadyOwner");
			return;
		}

		if (!plot.ownersContainer.contains(owner)) {
			ChatManager.send(player, "Empires.notification.plot.buy.alreadySold", owner);
			return;
		}

		if (!plot.getEmpire().plotsContainer.canCitizenMakePlot(citizen)) {
			ChatManager.send(player, "Empires.cmd.err.plot.limit", plot.getEmpire().plotsContainer.getMaxPlots());
			return;
		}

		if (EconomyProxy.getEconomy().takeMoneyFromPlayer(citizen.getPlayer(), price)) {
			for (Citizen resInPlot : plot.ownersContainer) {
				ChatManager.send(resInPlot.getPlayer(), "Empires.notification.plot.buy.oldOwner", plot,
						EconomyProxy.getCurrency(price));
			}

			Citizen.Container citizensToRemove = new Citizen.Container();

			citizensToRemove.addAll(plot.membersContainer);
			citizensToRemove.addAll(plot.ownersContainer);

			for (Citizen resInPlot : citizensToRemove) {
				Empires.instance.datasource.unlinkCitizenFromPlot(resInPlot, plot);
			}

			if (!plot.getEmpire().citizensMap.containsKey(citizen)) {
				Empires.instance.datasource.linkCitizenToEmpire(citizen, plot.getEmpire(),
						plot.getEmpire().ranksContainer.getDefaultRank());
			}
			Empires.instance.datasource.linkCitizenToPlot(citizen, plot, true);
			ChatManager.send(player, "Empires.notification.plot.buy.newOwner", plot);
			plot.getEmpire().bank.addAmount(price);
			deleteSignBlock();
			plot.deleteSignBlocks(signType, player.worldObj);
		} else {
			ChatManager.send(player, "Empires.notification.plot.buy.failed", EconomyProxy.getCurrency(price));
		}
	}

	@Override
	protected String[] getText() {
		// REF: Refactor chat components to allow formatting for this type of
		// text
		return new String[] { Empires.instance.LOCAL.getLocalization("Empires.sign.sell.title").getUnformattedText(),
				Empires.instance.LOCAL.getLocalization("Empires.sign.sell.description.owner").getUnformattedText() + " "
						+ owner.getPlayerName(),
				Empires.instance.LOCAL.getLocalization("Empires.sign.sell.description.price").getUnformattedText()
						+ price,
				restricted
						? Empires.instance.LOCAL.getLocalization("Empires.sign.sell.description.restricted")
								.getUnformattedText()
						: "" };
	}

	@Override
	public void onShiftRightClick(EntityPlayer player) {
		if (player.getPersistentID().equals(owner.getUUID())) {
			deleteSignBlock();
		}
	}

	public Localization getLocal() {
		return Empires.instance.LOCAL;
	}

	public static class SellSignType extends SignType {
		public static final SellSignType instance = new SellSignType();

		@Override
		public String getTypeID() {
			return "Empires:SellSign";
		}

		@Override
		public Sign loadData(TileEntitySign tileEntity, NBTBase signData) {
			return new SellSign(tileEntity, (NBTTagCompound) signData);
		}

		@Override
		public boolean isTileValid(TileEntitySign te) {
			if (!te.signText[0].startsWith(Sign.IDENTIFIER)) {
				return false;
			}

			try {
				NBTTagCompound rootTag = SignClassTransformer.getEmpiresDataValue(te);
				if (rootTag == null)
					return false;

				if (!rootTag.getString("Type").equals(SellSignType.instance.getTypeID()))
					return false;

				NBTBase data = rootTag.getTag("Value");
				if (!(data instanceof NBTTagCompound))
					return false;

				NBTTagCompound signData = (NBTTagCompound) data;

				EmpiresUniverse.instance.getOrMakeCitizen(UUID.fromString(signData.getString("Owner")));
				return true;
			} catch (Exception ex) {
				return false;
			}
		}
	}
}