package net.swedz.tesseract.neoforge.compat.mi.helper;

import aztech.modern_industrialization.api.energy.EnergyApi;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public final class ChargeInventoryHelper
{
	public static long charge(ItemStack stack, long maxEu, boolean simulate)
	{
		EnergyHandler energy = ItemAccess.forStack(stack).getCapability(EnergyApi.ITEM);
		if (energy == null) return 0;
        try (var tx = Transaction.openRoot()) {
            int inserted = energy.insert((int)Math.min(Integer.MAX_VALUE, Math.max(0, maxEu)), tx);
            if (!simulate) tx.commit();
            return inserted;
        }
	}

	public static long charge(Collection<ItemStack> items, long maxEu, boolean simulate)
	{
		long eu = 0;
		for(ItemStack stack : items)
		{
			long charged = charge(stack, Math.max(0, maxEu - eu), simulate);
			if(charged > 0)
			{
				eu += charged;
				if(eu == maxEu)
				{
					break;
				}
			}
		}
		return eu;
	}
}
