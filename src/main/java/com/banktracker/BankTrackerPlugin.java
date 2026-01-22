package com.banktracker;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class BankTrackerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	private BankTrackerPanel panel;
	private NavigationButton navButton;

	@Inject
	ItemManager itemManager;

	@Override
	protected void startUp() throws Exception
	{
		initializeCSV();
//		panel = new BankTrackerPanel(this);
//
//		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(BankTrackerPlugin.class, "panel_icon.png");
//
//		navButton = NavigationButton.builder()
//				.tooltip("Bank Value")
//				.priority(5)
//				.panel(panel)
//				.icon(icon)
//				.build();
//
//		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
	}

	//bank id: 95
	//inventory id: 93
	//equipment id: 94
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		for (Item item : event.getItemContainer().getItems())
		{
			if (itemManager.canonicalize(item.getId()) != item.getId() || item.getId() == -1)
			{
				continue;
			}
			int itemPrice = itemManager.getItemPrice(item.getId());
			ItemComposition itemDefinition = client.getItemDefinition(item.getId());

			writeToCsv(item.getId(),
					itemDefinition.getName(),
					item.getQuantity(),
					itemPrice);
		}
		log.info("bank dumping complete");

	}

	private static final Path CSV_PATH = Paths.get("src", "main", "resources", "output.csv");

	private void initializeCSV() throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_PATH.toFile()))) {
			writer.write("item_id,item_name,quantity,price");
			writer.newLine();
		} catch (IOException e) {
			log.error("Failed to write to CSV file", e);
		}
	}

	private void writeToCsv(int itemId, String name, int quantity, int price)
	{
		log.info("Writing CSV file");
		boolean exists = Files.exists(CSV_PATH);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_PATH.toFile(), true))) {
			writer.write(String.format("%d,%s,%d,%d", itemId, name, quantity, price));
			writer.newLine();
		} catch (IOException e) {
			log.error("Failed to write to CSV file", e);
		}
	}

}