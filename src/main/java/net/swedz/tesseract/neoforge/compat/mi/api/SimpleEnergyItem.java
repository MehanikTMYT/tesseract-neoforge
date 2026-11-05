package net.swedz.tesseract.neoforge.compat.mi.api;

import aztech.modern_industrialization.items.MIEnergyItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

/** Component-backed item energy, registered through the native NeoForge transaction API. */
public interface SimpleEnergyItem extends MIEnergyItem {
    DataComponentType<Long> getEnergyComponent();
    long getEnergyCapacity(ItemStack stack);
    long getEnergyMaxInput(ItemStack stack);
    long getEnergyMaxOutput(ItemStack stack);
    default long getStoredEnergy(ItemStack stack) { return stack.getOrDefault(getEnergyComponent(), 0L); }
    default void setStoredEnergy(ItemStack stack, long energy) { stack.set(getEnergyComponent(), energy); }
}
