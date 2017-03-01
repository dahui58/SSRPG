package ship;

import map.GridMap;
import map.GridPoint;
import characters.Crewmember;
import ship.modules.*;

import java.util.ArrayList;

public class PlayerShip extends Ship {

	private int fuelCapacity;
	private int remainingFuel;
	private int scannerStrength;
	private Scanner scanner;
	//private ArrayList<Crewmember> crew;
	private GridPoint location;
	private int money;

	public void setMoney(int m) {
		money = m;
	}

	public int getMoney() {
		return money;
	}

	private PlayerShip(PlayerShipBuilder builder) {
		super(builder);
		fuelCapacity = builder.fuelCapacity;
		remainingFuel = fuelCapacity;
		scannerStrength = builder.scannerStrength;
		money = builder.money;
	}

	public PlayerShip(String pName, ShipModules modules, int fuel, int crewCap) {
		super(pName, modules);
		fuelCapacity = fuel;
		remainingFuel = fuel;
		crewCapacity = crewCap;
		crew = new ArrayList<>();
	}

	public void initialiseCrew(ArrayList<Crewmember> newCrew) {
		crew = newCrew;
	}

	public void initialiseMap(GridPoint startLocation, GridMap map) {
		location = startLocation;
		scanner = Scanner.getScanner(scannerStrength, map, startLocation);
	}

	public GridPoint getLocation() {
		return location;
	}

	public void setLocation(GridPoint gridPoint) {
		location = gridPoint;
		scanner.setShipLocation(gridPoint);
	}

	public void shipStatus() {
		CargoBayModule cargo = (CargoBayModule) modules.getShipModule(CargoBayModule.class);
		System.out.println("PlayerShip status:");
		System.out.println(crew.size() + "/" + crewCapacity + " crew");
		System.out.println(" CREDS total: " + money);
		System.out.println(" Remaining Fuel: " + remainingFuel + "/" + fuelCapacity);
		// TODO: cargo bay is now a module, so need to put this in the module print information function
		System.out.println(" Cargo Bay: " + cargo.getFilledCapacity() + " units out of " + cargo.getMaxCapacity());
		System.out.println(" Modules: ");
		((EngineModule) modules.getShipModule(EngineModule.class)).printInformation();
		((ShieldModule) modules.getShipModule(ShieldModule.class)).printInformation();
	}

	public boolean travel(GridPoint gridPoint, int distance) {
		location = gridPoint;
		scanner.setShipLocation(gridPoint);
		int fuelCost = distance * ((EngineModule) modules.getShipModule(EngineModule.class)).fuelEfficiency;
		if (fuelCost <= remainingFuel) {
			System.out.println("Used " + fuelCost + " fuel.");
			remainingFuel = remainingFuel - fuelCost;
			return true;
		}
		return false;
	}

	public void scan() {
		scanner.scan();
	}

	public int getFuelCapacity() {
		return fuelCapacity;
	}

	public int getRemainingFuel() {
		return remainingFuel;
	}

	public void setRemainingFuel(int newFuel) {
		remainingFuel = newFuel;
	}

	public ArrayList<MannableShipModule> getMannableShipModulesAsList() {
		return modules.getMannableModulesAsList();
	}

	public static class PlayerShipBuilder extends Ship.GenericShipBuilder<PlayerShipBuilder> {

		public PlayerShipBuilder(String name, int maxCombinedModulePower) {
			super(name, maxCombinedModulePower);
		}

		protected int fuelCapacity = 100;
		protected int scannerStrength = 7;
		protected int money = 20000;

		public PlayerShipBuilder fuelCapacity(int maxFuelCapacity) {
			this.fuelCapacity = maxFuelCapacity;
			return this;
		}

		public PlayerShipBuilder scannerStrength(int scannerStrength) {
			this.scannerStrength = scannerStrength;
			return this;
		}

		public PlayerShipBuilder money(int money) {
			this.money = money;
			return this;
		}

		public PlayerShip build() {
			return new PlayerShip(this);
		}

	}

}