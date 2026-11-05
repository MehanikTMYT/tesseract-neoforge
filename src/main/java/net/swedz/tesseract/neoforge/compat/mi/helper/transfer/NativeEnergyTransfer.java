package net.swedz.tesseract.neoforge.compat.mi.helper.transfer;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
/** Atomic energy movement; simulation and insertion share an outer transaction. */
public final class NativeEnergyTransfer {
 public static long move(EnergyHandler source, EnergyHandler target, long maximum) {
  if (maximum <= 0 || source == target) return 0;
  try (var tx = Transaction.openRoot()) {
   int available;
   try (var simulation = Transaction.open(tx)) { available = source.extract((int)Math.min(Integer.MAX_VALUE, maximum), simulation); }
   int inserted = target.insert(available, tx);
   if (source.extract(inserted, tx) != inserted) return 0;
   tx.commit();
   return inserted;
  }
 }
}
