package net.swedz.tesseract.neoforge.compat.mi;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.swedz.tesseract.neoforge.Tesseract;
import net.swedz.tesseract.neoforge.compat.mi.loot.itempredicate.SimpleEnergyItemHasChargePredicate;

import java.util.function.Supplier;

public final class TesseractMILootConditions
{
	public static final class ItemSubPredicates
	{
		private static final DeferredRegister<DataComponentPredicate.Type<?>> ITEM_SUB_PREDICATE_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_PREDICATE_TYPE, Tesseract.ID);

		public static final Supplier<DataComponentPredicate.Type<SimpleEnergyItemHasChargePredicate>> SIMPLE_ENERGY_ITEM_HAS_CHARGE = create("simple_energy_item_has_charge", SimpleEnergyItemHasChargePredicate.CODEC);

		private static <T extends DataComponentPredicate> Supplier<DataComponentPredicate.Type<T>> create(String name, Codec<T> codec)
		{
			return ITEM_SUB_PREDICATE_TYPES.register(name, () -> new DataComponentPredicate.ConcreteType<>(codec));
		}
	}

	public static void init(IEventBus bus)
	{
		ItemSubPredicates.ITEM_SUB_PREDICATE_TYPES.register(bus);
	}
}
