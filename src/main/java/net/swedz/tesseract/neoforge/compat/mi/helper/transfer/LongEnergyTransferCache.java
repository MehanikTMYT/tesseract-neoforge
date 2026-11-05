package net.swedz.tesseract.neoforge.compat.mi.helper.transfer;

import com.mojang.logging.LogUtils;
import net.swedz.tesseract.neoforge.compat.mi.helper.transfer.NativeEnergyTransfer;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.swedz.tesseract.neoforge.helper.transfer.TransferCache;
import org.slf4j.Logger;

import java.util.function.Supplier;

public class LongEnergyTransferCache extends TransferCache<EnergyHandler>
{
	private static final Logger LOGGER = LogUtils.getLogger();

	public LongEnergyTransferCache(BlockCapability<EnergyHandler, Direction> capability, Supplier<EnergyHandler> sourceHandler)
	{
		super(capability, sourceHandler);
	}

	public LongEnergyTransferCache(Supplier<EnergyHandler> sourceHandler)
	{
		this(Capabilities.Energy.BLOCK, sourceHandler);
	}

	public boolean autoExtract(Level level, BlockPos pos, Direction direction, long maxAmount)
	{
		EnergyHandler target = cache.output(level, pos, direction);
		if(target != null)
		{
			return NativeEnergyTransfer.move(this.sourceHandler(), target, maxAmount) > 0;
		}
		return false;
	}

	@Override
	public boolean autoExtract(Level level, BlockPos pos, Direction direction)
	{
		return this.autoExtract(level, pos, direction, Long.MAX_VALUE);
	}

	public boolean autoInsert(Level level, BlockPos pos, Direction direction, long maxAmount)
	{
		EnergyHandler target = cache.input(level, pos, direction);
		if(target != null)
		{
			return NativeEnergyTransfer.move(target, this.sourceHandler(), maxAmount) > 0;
		}
		return false;
	}

	@Override
	public boolean autoInsert(Level level, BlockPos pos, Direction direction)
	{
		return this.autoInsert(level, pos, direction, Long.MAX_VALUE);
	}
}
