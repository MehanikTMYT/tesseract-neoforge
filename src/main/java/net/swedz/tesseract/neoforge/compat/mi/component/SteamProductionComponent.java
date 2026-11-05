package net.swedz.tesseract.neoforge.compat.mi.component;

import aztech.modern_industrialization.inventory.MIFluidStorage;
import aztech.modern_industrialization.machines.MachineComponent;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.function.Supplier;

public final class SteamProductionComponent implements MachineComponent.ServerOnly
{
	private final MIFluidStorage fluidStorage;

	private final List<Fluid>  waterTypes;
	private final FluidResource steamKey;

	private final Supplier<Long>    maxEuProduction;
	private final Supplier<Integer> waterToSteam;

	private final Reference2LongMap<Fluid> steamBuffer = new Reference2LongOpenHashMap<>();

	public SteamProductionComponent(MIFluidStorage fluidStorage, List<Fluid> waterTypes, FluidResource steamKey, Supplier<Long> maxEuProduction, Supplier<Integer> waterToSteam)
	{
		this.fluidStorage = fluidStorage;
		this.waterTypes = waterTypes;
		this.steamKey = steamKey;
		this.maxEuProduction = maxEuProduction;
		this.waterToSteam = waterToSteam;
	}

	public FluidResource findWater()
	{
        for(int slot=0; slot<fluidStorage.size(); slot++) {
            FluidResource fluid=fluidStorage.getResource(slot);
            if (!fluid.isEmpty() && waterTypes.contains(fluid.getFluid())) return fluid;
        }
		return FluidResource.EMPTY;
	}

	public FluidResource tryMakeSteam()
	{
		int waterToSteamRate = this.waterToSteam.get();

		FluidResource waterFluid = this.findWater();
		if(waterFluid.isEmpty())
		{
			return waterFluid;
		}

		long steamToProduce = maxEuProduction.get();
		try (Transaction transaction = Transaction.openRoot())
		{
			long steamProducedSimulation;
			try (Transaction simulation = Transaction.open(transaction))
			{
				steamProducedSimulation = fluidStorage.insert(steamKey, (int)Math.min(Integer.MAX_VALUE, steamToProduce), simulation);
			}

			if(steamProducedSimulation > 0)
			{
				long waterToConsume = (steamProducedSimulation - steamBuffer.getLong(steamKey.getFluid()) + waterToSteamRate - 1) / waterToSteamRate;
				long waterConsumed = fluidStorage.extract(waterFluid, (int)Math.min(Integer.MAX_VALUE, waterToConsume), transaction);
				steamBuffer.mergeLong(steamKey.getFluid(), waterConsumed * waterToSteamRate, Long::sum);

				long steamProduced = fluidStorage.insert(FluidResource.of(steamKey.getFluid()), (int)Math.min(Integer.MAX_VALUE, Math.min(steamToProduce, steamBuffer.getLong(steamKey.getFluid()))), transaction);
				steamBuffer.mergeLong(steamKey.getFluid(), -steamProduced, Long::sum);

				transaction.commit();
			}
		}

		return waterFluid;
	}

	@Override
	public void writeNbt(ValueOutput tag)
	{
		CompoundTag buffer = new CompoundTag();
		for(Reference2LongMap.Entry<Fluid> entry : steamBuffer.reference2LongEntrySet())
		{
			if(entry.getLongValue() != 0)
			{
				buffer.putLong(entry.getKey().toString(), entry.getLongValue());
			}
		}
		tag.store("steamBuffer", CompoundTag.CODEC, buffer);
	}

	@Override
	public void readNbt(ValueInput tag, boolean isUpgradingMachine)
	{
		CompoundTag buffer = tag.read("steamBuffer", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		for(String key : buffer.keySet())
		{
			Fluid fluid = BuiltInRegistries.FLUID.getValue(Identifier.tryParse(key));
			if(fluid != Fluids.EMPTY)
			{
				steamBuffer.put(fluid, buffer.getLongOr(key, 0L));
			}
		}
	}
}
