package net.swedz.tesseract.neoforge.registry.common;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.items.ItemEnergyHandler;
import net.swedz.tesseract.neoforge.compat.mi.api.SimpleEnergyItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
public final class MICommonCapabitilies {
 public static <I extends Item & SimpleEnergyItem> void simpleEnergyItem(I item, RegisterCapabilitiesEvent event) {
  event.registerItem(EnergyApi.ITEM, (stack, access) -> new ItemEnergyHandler(access, item.getEnergyComponent(),
    (int)Math.min(Integer.MAX_VALUE, item.getEnergyMaxInput(stack)), (int)Math.min(Integer.MAX_VALUE, item.getEnergyMaxOutput(stack))) {
   @Override protected long getCapacity(ItemResource resource) { return item.getEnergyCapacity(resource.toStack()); }
  }, item);
 }
}
