package arch.session;

import ship.*;
import map.*;
import goods.*;
import ship.modules.CargoBayModule;
import ship.modules.MannableShipModule;
import util.dataload.csv.*;
import util.*;
import events.*;
import characters.*;

import java.util.ArrayList;
import java.util.List;


public class MapSession extends Session {

	private final static int FUEL_COST = 180;
	private GridMap map;
	private PlayerShip p1;
	private ArrayList<GoodsForSale> goods;

	public MapSession(PlayerShip crewedPlayerShip) {
		super("MapSession");
		p1 = crewedPlayerShip;

		GridPoint start = new GridPoint(3, 6);
		map = GridMap.generateGridMap(11, 7);

		p1.initialiseMap(start, map);
		initMapAndGoodsList();
	}

	private final String xLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	@Override
	public void run() {


		while (true) {

			printCurrentLocation();

			String input = ConsoleInputHandler.getStringFromUser("");

			if (input.equalsIgnoreCase("scan")) {
				p1.scan();
			} else if (input.equalsIgnoreCase("trade")) {
				tradeSequence();
			} else if (input.equalsIgnoreCase("travel")) {
				travelSequence();
			} else if (input.equalsIgnoreCase("ship")) {
				p1.shipStatus();
			} else if (input.equalsIgnoreCase("cargo")) {
				printCargo();
			} else if (input.equalsIgnoreCase("fuel")) {
				fuelSequence();
			} else if (input.equalsIgnoreCase("crew")) {
				crewSequence();
			} else {
				consoleInformation(input);
			}
			sleep(1);
			System.out.println("");
		}
	}

	private void initMapAndGoodsList() {
		CSV planets = CSVReader.readCSV("planets");
		CSV goodsCSV = CSVReader.readCSV("goods");
		goods = new ArrayList<GoodsForSale>();

		for (int i = 1; i < goodsCSV.rows; i++) {
			ArrayList<String> values = goodsCSV.getZeroIndexedRow(i);
			goods.add(new GoodsForSale(Integer.parseInt(values.get(0)),
					values.get(1),
					Integer.parseInt(values.get(2)) == 0 ? false : true,
					Integer.parseInt(values.get(3))
			));
			//System.out.println("goods:" + values.toString());
		}

		for (int i = 1; i < planets.rows; i++) {
			ArrayList<String> planet = planets.getZeroIndexedRow(i);
			int id = Integer.parseInt(planet.get(0));
			String name = planet.get(1);
			int gridX = Integer.parseInt(planet.get(2));
			int gridY = Integer.parseInt(planet.get(3));
			int marketSize = Integer.parseInt(planet.get(4));
			map.populateGridSquare(new Planet(id,
					name,
					new GridPoint(gridX, gridY),
					marketSize));
			//System.out.println("added " + name);
		}

	}

	private void printCurrentLocation() {
		GridPoint l = p1.getLocation();
		GridSquare locationSquare = map.getSquareAt(l);
		if (locationSquare instanceof Planet) {
			Planet locationPlanet = (Planet) locationSquare;
			String locationName = locationPlanet.name;
			System.out.println(p1.name + " is at the planet " + locationName + " (" + xLetters.charAt(l.x) + "," + l.y + ")");
		} else {
			System.out.println(p1.name + " is at empty grid point " + xLetters.charAt(l.x) + "," + l.y);
		}
	}

	private void tradeSequence() {
		GridSquare location = map.getSquareAt(p1.getLocation());

		if (location instanceof Planet) {

			System.out.print("Buying or selling?: ");
			char tradeChoice = ConsoleInputHandler.getCharFromUser("");

			Planet planet = (Planet) location;

			if (tradeChoice == 'b' || tradeChoice == 'B') {

				//todo: refactor this into a function at Market
				ArrayList<GoodsForSale> availableGoods = planet.market.availableGoods;
				System.out.println("GOODS:");
				System.out.println(" Enter an index to buy Goods, or return");
				int goodsIndex = 0;
				for (GoodsForSale g : availableGoods) {
					int actualValueDiffFromBaseValue = g.baseValue - g.getPurchaseValue();
					String legal = g.legal ? "" : "[ILLEGAL]";
					String profit = actualValueDiffFromBaseValue < 0 ? "[+]" : "[-]";
					System.out.println(" " + (goodsIndex++) + " - " + g.name + " : " + g.getPurchaseValue() + " CREDS  " + profit + legal);
				}

				int goodsCount = availableGoods.size();
				int choice = ConsoleInputHandler.getIntInRangeFromUser(goodsCount);

				GoodsForSale selectedGoods = availableGoods.get(choice);
				int goodsValue = selectedGoods.getPurchaseValue();
				System.out.println(selectedGoods.name + " costs " + goodsValue + " per unit.");
				System.out.println("How many units would you like to buy?");
				int numberCanAfford = p1.getMoney() / goodsValue;
				System.out.println("--> You can afford " + numberCanAfford);


				int quantity = ConsoleInputHandler.getIntFromUser("");

				CargoBayModule playerCargo = p1.getCargoBay();
				int cargoSize = playerCargo.getFilledCapacity();
				int cargoMaxSize = playerCargo.getMaxCapacity();
				if (quantity <= numberCanAfford && quantity > 0) {
					if (cargoSize + quantity <= cargoMaxSize) {
						int totalCost = goodsValue * quantity;
						playerCargo.addCargo(new PurchasedGoods(selectedGoods, quantity, planet.gridPoint));
						p1.setMoney(p1.getMoney() - totalCost);
						System.out.println("Bought " + quantity + " " + selectedGoods.name + " for " + totalCost + " CREDS.");
					} else {
						System.out.println("Not enough cargo space.");
					}
				} else if (quantity == 0) {
					System.out.println("You buy nothing.");
				} else {
					System.out.println("You cannot afford that amount of " + selectedGoods.name);
				}
			}

			if (tradeChoice == 's' || tradeChoice == 'S') {
				System.out.println("CARGO:");
				CargoBayModule playerCargo = p1.getCargoBay();
				List<PurchasedGoods> cargo = playerCargo.getCargo();

				if (cargo.size() > 0) {
					int goodsIndex = 0;

					for (PurchasedGoods pg : cargo) {
						int localValueOfGoods = planet.market.getValueForSpecificGoods(pg);
						int profit = localValueOfGoods - pg.purchasedValue;
						String legal = pg.legal ? "" : "[ILLEGAL]";
						String profitString = profit == 0 ? "[=]" : profit < 0 ? "[-]" : "[+]";
						System.out.println(" " + (goodsIndex++) + " - " + pg.getQuantity() + "x +" + pg.name + " : " + localValueOfGoods + " CREDS  " + profitString + legal);
					}

					int choice = ConsoleInputHandler.getIntInRangeFromUser(cargo.size());

					PurchasedGoods selectedGoods = cargo.get(choice);
					int quantityOwned = selectedGoods.getQuantity();
					int localValueOfGoods = planet.market.getValueForSpecificGoods(selectedGoods);
					int profit = localValueOfGoods - selectedGoods.purchasedValue;

					System.out.println("How many units would you like to sell?");
					System.out.println("--> You can sell " + quantityOwned);

					if (profit > 0) {
						System.out.println("--> You will make a profit on this sale.");
					} else if (profit < 0) {
						System.out.println("--> You will NOT profit on this sale.");
					} else if (profit == 0) {
						System.out.println("--> You will break even on this sale.");
					}

					int quantitySold = ConsoleInputHandler.getIntFromUser("");

					if (quantitySold <= quantityOwned) {
						int totalMoneyMade = localValueOfGoods * quantitySold;
						p1.setMoney(p1.getMoney() + totalMoneyMade);
						if (quantitySold == quantityOwned) {
							playerCargo.removeCargo(choice);
						} else {
							playerCargo.removeCargo(choice, quantitySold);
						}
						System.out.println("Sold " + quantitySold + " " + selectedGoods.name + " for " + totalMoneyMade + " CREDS.");
					} else System.out.println("You don't have that much " + selectedGoods.name);
				} else System.out.println("No cargo to sell...");
			}
		} else {
			System.out.println("Cannot trade here.");
		}
	}

	private void travelSequence() {
		while (true) {
			System.out.println("Select a square to travel to: ('A-Z','0-9'):");
			int y = 2;
			char xChar = ConsoleInputHandler.getCharFromUser("x");
			y = ConsoleInputHandler.getIntFromUser("y");

			int x = xLetters.indexOf(xChar);


			GridPoint destination = new GridPoint(x, y);
			try {
				GridSquare destinationSquare = map.getSquareAt(destination);
				int distance = p1.getLocation().comparePoints(destination);

				boolean destinationIsAPlanet = destinationSquare instanceof Planet;

				String destinationString;
				if (destinationIsAPlanet) {
					Planet planet = (Planet) destinationSquare;
					destinationString = planet.name;
				} else {
					destinationString = "nowhere";
				}

				boolean canTravel = p1.travel(destination, distance);
				if (canTravel) {
					for (int jumps = 0; jumps < distance; jumps++) {
						double eventRoll = RNG.randZeroToOne();
						if (eventRoll <= 0.15) {
							EventRunner.run(new ShipwreckEvent(), p1);
						} else if (eventRoll >= 0.16 && eventRoll <= 0.3) {
							EventRunner.run(new BanditEvent(), p1);
						}
						System.out.println("You jump to the next sector.");
						System.out.println(distance - jumps + " sectors away.");
						sleep(1);
						printTwoRows();
					}
					System.out.println("You travel " + distance + " to reach " + destinationString);
				} else {
					System.out.println("You do not have enough fuel.");
				}
				break;
			} catch (IndexOutOfBoundsException ioobe) {
				System.out.println("Square out of bounds!");
			}
		}
	}

	private void printCargo() {
		CargoBayModule playerCargo = p1.getCargoBay();
		List<PurchasedGoods> cargo = playerCargo.getCargo();

		if (cargo.size() > 0) {
			int goodsIndex = 0;

			for (PurchasedGoods pg : cargo) {
				String legal = pg.legal ? "" : "[ILLEGAL]";
				int purchasedValue = pg.purchasedValue;
				System.out.println(" " + (goodsIndex++) + " - " + pg.getQuantity() + "x +" + pg.name + " : bought for " + purchasedValue + " CREDS  " + legal);
			}
		} else {
			System.out.println("Cargo hold is empty!");
		}
	}

	private void fuelSequence() {
		GridSquare location = map.getSquareAt(p1.getLocation());

		if (location instanceof Planet) {
			Planet planet = (Planet) location;

			System.out.println("Fuel costs 18 CREDS per unit.");
			System.out.println("How much do you want to buy?");
			System.out.println("--> Fuel status " + p1.getRemainingFuel() + "/" + p1.getFuelCapacity() + ".");

			int availableFuelSpace = p1.getFuelCapacity() - p1.getRemainingFuel();

			int quantityBought = ConsoleInputHandler.getIntFromUser("");
			int cost = quantityBought * FUEL_COST;
			if (quantityBought <= availableFuelSpace) {
				if (cost <= p1.getMoney()) {
					System.out.println();
					System.out.println("Are you sure you want to buy " + quantityBought + " fuel for " + cost + " CREDS? (Y/N)");
					char decision = ConsoleInputHandler.getCharFromUser("");
					if (decision == 'Y' || decision == 'y') {
						p1.setRemainingFuel(p1.getRemainingFuel() + quantityBought);
						System.out.println("You now have " + p1.getRemainingFuel() + "/" + p1.getFuelCapacity() + " fuel remaining.");
						int newBalance = p1.getMoney() - cost;
						System.out.println("CREDS " + p1.getMoney() + " --> " + newBalance);
						p1.setMoney(newBalance);
					}
				} else System.out.println("You don't have enough money.");
			} else System.out.println("You don't have space for that much fuel!");
		} else System.out.println("There is nowhere to refuel here.");
	}

	private void crewSequence() {
		char decision = 'n';
		while (true) {
			System.out.println("Would you like to move a crewmember? (Y/N)");
			decision = ConsoleInputHandler.getCharFromUser("");
			// TODO write a function to take a range of acceptable chars to make these checks shorter
			if (decision == 'Y' || decision == 'y' || decision == 'N' || decision == 'n') break;
		}
		if (decision == 'Y' || decision == 'y') {
			System.out.println("Which crewmember would you like to move?");
			printCrewAndMannedModule();
			ArrayList<Crewmember> crewmembers = p1.getCrew();
			int crewIndex = ConsoleInputHandler.getIntInRangeFromUser(crewmembers.size());
			Crewmember crewmemberToMove = crewmembers.get(crewIndex);
			ArrayList<MannableShipModule> mannableModules = p1.getMannableShipModulesAsList();
			if (mannableModules.size() <= 0) {
				System.out.println("There are no mannable modules");
			} else {
				System.out.println("Which module would you like " + crewmemberToMove.name + " to man?");
				MannableShipModule moduleToMan = ConsoleInputHandler.getUserChoiceFromList(mannableModules, "getName");
				moduleToMan.removeCrewmember();
				moduleToMan.assignCrewmember(crewmemberToMove);
			}
		}
		printCrewAndMannedModule();
	}

	private void printCrewAndMannedModule() {
		ArrayList<Crewmember> playerCrew = p1.getCrew();
		int i = 0;
		for (Crewmember member : playerCrew) {
			MannableShipModule mannedModule = p1.getModuleMannedBy(member);
			String mannedModuleName = mannedModule != null ? "Manning " + mannedModule.getName() : "Not manning a module.";
			System.out.println(i + " - " + member.name + " (" + member.crewmemberClass._className + ") " + mannedModuleName);
			i++;
		}
	}

	private void consoleInformation(String input) {
		System.out.println("Command \"" + input + "\" not recognised.");
		System.out.println("Available commands: [scan] [trade] [travel] [ship] [cargo] [fuel] [crew]");
	}

	private void sleep(int seconds) {
		int milliseconds = seconds * 1000;
		try {
			Thread.sleep(milliseconds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printTwoRows() {
		System.out.println();
		System.out.println();
	}

}