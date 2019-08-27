/*
 * Copyright (c) 2018, Dillon <https://github.com/dillydill123>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.rcpouches;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import javax.inject.Inject;

@PluginDescriptor(
		name = "RC Pouches",
		description = "Left click pouches for runecrafting",
		tags = {"skilling", "runecrafting", "rc", "pouches"},
		enabledByDefault = false
)
public class RCPouchesPlugin extends Plugin
{

	private static final int FIRE_ALTAR = 10315;

	private static AltarMode altarMode;

	@Inject
	private Client client;

	@Inject
	private RCPouchesConfig config;

	@Override
	public void startUp()
	{
		altarMode = config.AltarMode();
	}

	@Provides
	public RCPouchesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RCPouchesConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("rcpouches"))
		{
			altarMode = config.AltarMode();
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		final String option = Text.removeTags(event.getOption()).toLowerCase();
		final String target = Text.removeTags(event.getTarget()).toLowerCase();

		swapRCPouchesMenuEntries(option, target);
		if (altarMode == AltarMode.LAVAS)
		{
			swapDuelingRingMenuEntries(option, target);
		}

	}

	private void swapRCPouchesMenuEntries(String option, String target)
	{
		if (!target.equals("small pouch") && !target.equals("medium pouch") &&
				!target.equals("large pouch") && !target.equals("giant pouch"))
		{
			return;
		}

		String leftClick = "fill";
		final int region = client.getLocalPlayer().getWorldLocation().getRegionID();
		final WorldPoint loc = client.getLocalPlayer().getWorldLocation();
		switch (altarMode)
		{
			case LAVAS:
				switch (region)
				{
					case FIRE_ALTAR:
						leftClick = "empty";
						break;
				}
				break;
			case ZMI:
				if (loc.getX() >= 3051 && loc.getX() <= 3067 &&
					loc.getY() >= 5572 && loc.getY() <= 5588)
				{
					leftClick = "empty";
				}
				break;
		}

		swap(leftClick, option, target, true);

	}

	private void swapDuelingRingMenuEntries(String option, String target)
	{
		if (!target.contains("ring of dueling"))
		{
			return;
		}

		String leftClick;
		final int region = client.getLocalPlayer().getWorldLocation().getRegionID();
		System.out.println(client.getLocalPlayer().getWorldLocation());
		switch (region)
		{
			case FIRE_ALTAR:
				leftClick = "castle wars";
				break;
			default:
				leftClick = "duel arena";
				break;
		}

		swap(leftClick, option, target, true);
	}

	private int searchIndex(MenuEntry[] entries, String option, String target, boolean strict)
	{
		for (int i = entries.length - 1; i >= 0; i--)
		{
			MenuEntry entry = entries[i];
			String entryOption = Text.removeTags(entry.getOption()).toLowerCase();
			String entryTarget = Text.removeTags(entry.getTarget()).toLowerCase();

			if (strict)
			{
				if (entryOption.equals(option) && entryTarget.equals(target))
				{
					return i;
				}
			}
			else
			{
				if (entryOption.contains(option.toLowerCase()) && entryTarget.equals(target))
				{
					return i;
				}
			}
		}

		return -1;
	}

	private void swap(String optionA, String optionB, String target, boolean strict)
	{
		MenuEntry[] entries = client.getMenuEntries();

		int idxA = searchIndex(entries, optionA, target, strict);
		int idxB = searchIndex(entries, optionB, target, strict);

		if (idxA >= 0 && idxB >= 0)
		{
			MenuEntry entry = entries[idxA];
			entries[idxA] = entries[idxB];
			entries[idxB] = entry;

			client.setMenuEntries(entries);
		}
	}
}
