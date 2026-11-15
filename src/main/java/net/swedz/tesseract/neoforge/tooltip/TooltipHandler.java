package net.swedz.tesseract.neoforge.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.swedz.tesseract.neoforge.Tesseract;
import net.swedz.tesseract.neoforge.proxy.Proxies;
import net.swedz.tesseract.neoforge.proxy.builtin.TesseractProxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class TooltipHandler
{
	// Readers iterate immutable snapshots; registrations serialize publication so none are lost.
	private static volatile List<TooltipAttachment> SORTED_TOOLTIPS = List.of();

	static synchronized void register(TooltipAttachment tooltip)
	{
		List<TooltipAttachment> sorted = new ArrayList<>(SORTED_TOOLTIPS);
		sorted.add(tooltip);
		Collections.sort(sorted);
		SORTED_TOOLTIPS = List.copyOf(sorted);
	}

	public static void attach(TooltipFlag flags, Item.TooltipContext context, ItemStack stack, List<Component> lines)
	{
		Item item = stack.getItem();
		if(item != null)
		{
			boolean hasPrintRequiredShift = false;
			for(TooltipAttachment tooltip : SORTED_TOOLTIPS)
			{
				Optional<List<? extends Component>> maybeComponents = tooltip.lines(flags, context, stack);
				if(!tooltip.requiresShift() || Proxies.get(TesseractProxy.class).hasShiftDown())
				{
					maybeComponents.ifPresent(lines::addAll);
				}
				else if(tooltip.requiresShift() && !hasPrintRequiredShift && maybeComponents.isPresent())
				{
					hasPrintRequiredShift = true;
					if(Proxies.get(net.swedz.tesseract.neoforge.compat.mi.proxy.TesseractMIProxy.class).anyShiftTooltipsAreFor(context, stack, item))
					{
						continue;
					}
					lines.add(Tesseract.text().tooltipsShiftRequired());
				}
			}
		}
	}
}
