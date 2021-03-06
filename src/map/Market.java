package map;

import goods.*;
import util.rng.RNG;
import util.dataload.csv.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Market {

	public final int marketSize;
	public final ArrayList<GoodsForSale> availableGoods;
	public final ArrayList<Integer> probabilities;
	public final int planetID;

	public static Market getMarket(int marketSize, int planetID) {
		return new Market(marketSize, planetID);
	}

	public int getValueForSpecificGoods(PurchasedGoods g) {
		return g.calculateGoodsLocalValue(probabilities.get(g.id));
	}

	private Market(int pMarketSize, int planetId) {
		marketSize = pMarketSize;
		availableGoods = new ArrayList<GoodsForSale>();
		probabilities = new ArrayList<Integer>();
		planetID = planetId;
		setProbabilities();
		generateGoods();
	}

	private void setProbabilities() {
		CSV planetMarket = CSVReader.readCSV("planet_markets");
		ArrayList<String> values = planetMarket.getZeroIndexedRow(planetID + 1);
		for (int i = 1; i < values.size(); i++) {
			probabilities.add(Integer.parseInt(values.get(i)));
		}
	}

	private void generateGoods() {
		int probabilityTotal = getProbabilityTotal();
		boolean[] selectedGoods = new boolean[probabilities.size()];
		Arrays.fill(selectedGoods, false);
		for (int i = 0; i < marketSize; i++) {
			boolean goodsNotAlreadyChosen = false;
			int cycles = 0;
			while (!goodsNotAlreadyChosen) {
				RNG rand = new RNG();
				int random = rand.randInt(0, probabilityTotal - 1);
				//System.out.println("Random: " + random);
				int goodsIndex = getGood(random, probabilityTotal);
				if (selectedGoods[goodsIndex] == false) {
					availableGoods.add(GoodsList.GOODS[goodsIndex].generateGoodsMarketValue(probabilities.get(goodsIndex)));
					selectedGoods[goodsIndex] = true;
					goodsNotAlreadyChosen = true;
					break;
				}
				cycles++;
				//System.out.println("We've already seen " + GoodsList.GOODS[goodsIndex].name + ". Trying again!");
			}
			//System.out.println(cycles);
		}
	}

	private int getProbabilityTotal() {
		int probabilityTotal = 0;
		for (Integer i : probabilities) probabilityTotal += i;
		return probabilityTotal;
	}

	private int getGood(int random, int totalProb) {
		int total = -1;
		for (int i = 0; i < probabilities.size(); i++) {
			total = total + probabilities.get(i);
			//System.out.println("Current total: " + total + ". We're at " + GoodsList.GOODS[i].name);
			if (random <= total) {
				return i;
			}
		}
		return total - 1; // Shouldn't get here!
	}

}
