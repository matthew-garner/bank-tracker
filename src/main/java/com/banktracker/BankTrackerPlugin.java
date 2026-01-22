package com.banktracker;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemMapping;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;


@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class BankTrackerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	ItemManager itemManager;

	@Override
	protected void startUp() throws Exception
	{
		initailiseCSV();
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	//bank id: 95
	//inventory id: 93
	//equipment id: 94
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		int containerId = event.getContainerId();
		log.info("ItemContainerChanged: " + containerId);
		// Only care about bank, inventory, equipment
		if (containerId != InventoryID.BANK.getId()
				&& containerId != InventoryID.INVENTORY.getId()
				&& containerId != InventoryID.EQUIPMENT.getId())
		{
			return;
		}

		ItemContainer container = event.getItemContainer();
		if (container == null)
		{
			return;
		}

		boolean isBank = containerId == InventoryID.BANK.getId();

		for (Item item : container.getItems())
		{
			int itemId = item.getId();

			//Skip empty slots
			if (itemId == -1)
			{
				continue;
			}

			ItemComposition comp = itemManager.getItemComposition(itemId);

			// BANK-ONLY filters
			if (isBank) {
				if (comp.getPlaceholderTemplateId() != -1) {
					continue;
				}

				//Skip bank fillers
				if (itemId == ItemID.BANK_FILLER) {
					continue;
				}
			}

			//Skip invalid quantities
			if (item.getQuantity() <= 0)
			{
				continue;
			}

			//Canonicalize (maps untradeables → tradeable base)
			int canonicalItemId = itemManager.canonicalize(itemId);
			ItemComposition canonicalComp =
					itemManager.getItemComposition(canonicalItemId);

			//Coins: always include
			if (itemId == 995)
			{
				log.info("found coins!"+ canonicalComp.getName() + item.getQuantity());
				writeToCsv(
						itemId, "Coins",	item.getQuantity()
				);
				continue;
			}

			//Only skip if the *canonical* item is not tradeable
			if (!canonicalComp.isTradeable())
			{
				continue;
			}

			//Persist canonical item
			writeToCsv(canonicalItemId, canonicalComp.getName(), item.getQuantity()
			);
		}
	}
//		if (container != null)
//		{
//			int itemId = item.getId();
//			for (Item item : container.getItems())
//				// 1️⃣ Skip empty slots
//				if (itemId == -1)
//				{
//					continue;
//				}
//
//			// 2️⃣ Skip bank fillers (before canonicalize)
//			if (itemId == ItemID.BANK_FILLER)
//			{
//				continue;
//			}
//			{
//				String itemName = itemManager.getItemComposition(item.getId()).getName();
//				//if (itemManager.getItemComposition(item.getId()).getName().equals("Coins"))
//				if (item.getId() == 995)
//				{
//					writeToCsv(item.getId(), itemName, item.getQuantity());
//					log.info("item id: " + item.getId());
//				}
//				//log.info("Item id: {}", item);
//				if (item.getId() != -1 && itemManager.getItemComposition(item.getId()).isTradeable())
//				{
//					if (!Objects.equals(itemName, "Bank filler")){
//						//int itemPrice = itemManager.getItemPrice(item.getId()); //high alch price
////						log.info("Item ID: {}, Item name: {}, Item quantity {}", item.getId(), itemName, item.getQuantity());
//						writeToCsv(item.getId(), itemName, item.getQuantity());
//					}
//				}
//				if (!itemManager.getItemComposition(item.getId()).isTradeable())
//				{
//
//					int canonicalItemId = itemManager.canonicalize(itemId);
//					ItemComposition comp = itemManager.getItemComposition(canonicalItemId);
//
//					writeToCsv(canonicalItemId, comp.getName(), item.getQuantity());
//				}
//			}
//		}
//	}

	private void initailiseCSV() {
		String csvFilePath = "src/test/resources/output.csv";
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
			writer.write("Item ID,Item Name,Item Quantity");
			writer.newLine();
		} catch (IOException e) {
			log.error("Failed to write to CSV file", e);
		}
	}

	private void writeToCsv(int itemId, String itemName, int itemQuantity)
	{
		String csvFilePath = "src/test/resources/output.csv";
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath, true))) {
			writer.write(String.format("%d,%s,%d%n", itemId, itemName, itemQuantity));
		} catch (IOException e) {
			log.error("Failed to write to CSV file", e);
		}
	}

}