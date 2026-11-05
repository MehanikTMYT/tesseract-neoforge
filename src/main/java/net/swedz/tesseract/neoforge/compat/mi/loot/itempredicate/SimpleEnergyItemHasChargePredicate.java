package net.swedz.tesseract.neoforge.compat.mi.loot.itempredicate;

import com.mojang.serialization.Codec;
import net.swedz.tesseract.neoforge.compat.mi.api.SimpleEnergyItem;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.world.item.ItemStack;

public record SimpleEnergyItemHasChargePredicate() implements DataComponentPredicate
{
	public static final Codec<SimpleEnergyItemHasChargePredicate> CODEC = com.mojang.serialization.MapCodec.unit(SimpleEnergyItemHasChargePredicate::new).codec();

	@Override
	public boolean matches(net.minecraft.core.component.DataComponentGetter components)
	{
		return components.getOrDefault(aztech.modern_industrialization.MIComponents.ENERGY.get(), 0L) > 0;
	}
}
