package ship.modules;

import ship.weapons.*;

public class WeaponModule extends ShipModule {

    public final int maxWeaponPowerSupported;
    private ShipWeapon loadedWeapon;
    private int turnsTilWeaponReady;

    public WeaponModule(int maxWeaponPower) {
        maxWeaponPowerSupported = maxWeaponPower;
        loadedWeapon = null;
    }

    public void decrementTurnsTilWeaponReady() {
        turnsTilWeaponReady = turnsTilWeaponReady > 0 ?
            turnsTilWeaponReady-1:0;
    }

    public int getTurnsTilWeaponReady() {
        return turnsTilWeaponReady;
    }

    @Override
    public void printInformation() {
        System.out.println(" - WEAPON MODULE ["+name+"]");
        System.out.println("  + POWER ["+maxWeaponPowerSupported+"]");
    }

    public boolean setWeapon(ShipWeapon weapon) {
        if (maxWeaponPowerSupported >= weapon.requiredWeaponModulePower) {
            turnsTilWeaponReady = weapon.cooldown;
            loadedWeapon = weapon;
            System.out.println("Equipped " + weapon.name + ".");
            return true;
        } else {
            System.out.println("Couldn't equip " + weapon.name + ".");
            return false;
        }
    }

    public void removeWeapon() {
        loadedWeapon = null;
    }

    public ShipWeapon getWeapon() { return loadedWeapon; }

}
