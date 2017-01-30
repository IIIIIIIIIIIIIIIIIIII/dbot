import java.awt.Font;
import java.awt.Graphics;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.MessageListener;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.message.Message;

@ScriptManifest(author = "Xaklon", category = Category.MISC, description = "Buys, sells, profit$", name = "FLIPPER 5.0", version = 4.0)
public class Main extends AbstractScript {
	int current;
	Area zaffArea;
	int State = 0;
	NPC Zaff;
	NPC GrandExchangeClerk;

	
	int prePurchase = 0;
	int postPurchase = 0;
	int staffsGained = postPurchase - prePurchase;
	int totalStaffs = 0;
	
	GameObject bank;
	GameObject door;
	
	@Override
	public void onStart() {
		State = 6;
	 zaffArea = new Area(3204, 3437, 3201, 3432);
	}

	@SuppressWarnings("deprecation")
	@Override
	/*
	 * relevant Scr info
	 * 0 bZaff
	 * 1 hop
	 * 2 walkVWESTaction
	 * 3 bankAction
	 * 4 walkGE
	 * 5 geBANK interaction
	 * 6 walk to Zaff
	 * 
	 * 3205, 3437 NE Corner

		3200, 3431 SW Corner
	 * */
	public int onLoop() {

		if (State == 0) {
			buy();
			sleep(1000, 2000);
			if (getInventory().isFull() && getInventory().count("Coins") > 10000) {
				State = 2;
		
			} 
			else if (getInventory().count("Coins")<10000){
				State = 4;
			}
			else {
				State = 1;
			}
		}
		if (State == 1) {
			doTheHop();
			State = 0;
		}
		if (State == 2) {
			walkToBank();
		}
		if (State == 3) {
			bank();
		}
		if (State == 4) {
			GEWalk();
			
		}
		if (State == 5) {
			GESell();
			
		}
		if (State == 6) {
			ZaffWalk();
			
		}
		/*
		*/
		return 0;

	}

	public void doTheHop() {
		current = getClient().getCurrentWorld();

		if (getWorldHopper().quickHop(getWorld().getRealID())) {
			sleepUntil(() -> getClient().getCurrentWorld() != current, 15000);
			
			sleep(9000,12000);
		}
	}
	
	public void GEWalk(){
		getCamera().rotateToPitch(Calculations.random(30, 40));
		if (getWalking().getDestinationDistance() <= Calculations.random(3, 8)) {
			getWalking().walk(BankLocation.GRAND_EXCHANGE.getCenter());
		}
		sleep(300, 500);
		if (BankLocation.GRAND_EXCHANGE.getCenter().distance(getLocalPlayer()) <= 3) {
			sleep(2000,3000);
			State = 5;
		}
	}
	public void ZaffWalk(){
		getCamera().rotateToPitch(Calculations.random(30, 40));
		if (getWalking().getDestinationDistance() <= Calculations.random(3, 8)) {
			getWalking().walk(zaffArea.getCenter());
		}
		sleep(300, 500);
		if (!zaffArea.contains(getLocalPlayer()) && zaffArea.getCenter().distance(getLocalPlayer()) < 5){
			door = getGameObjects().closest("Door");
				if(door!=null && door.hasAction("Open")){
					door.interact("Open");
				}
		}
		if (zaffArea.contains(getLocalPlayer())) {
			sleep(2000,3000);
			State = 0;
		}
	}
	public void GESell(){
		bank = getGameObjects().closest("Grand Exchange booth");
		if(bank!=null){ 
			log("Banked.");
			getCamera().rotateToEntity(bank);
			bank.interact("Bank");
			sleep(3000,4000);
			getBank().depositAll("Battlestaff");
			getBank().deposit(1391, 27);
			sleepUntil(()->!getInventory().isFull(), 3000);
			sleep(3000,5000);
			getBank().setWithdrawMode(BankMode.NOTE);
			sleep(2300,3500);

			getBank().withdrawAll("Battlestaff");
			sleepUntil(()->getInventory().contains(1392), 3000);

			getBank().close();
			
			sleep(2000,3500);
			GrandExchangeClerk = getNpcs().closest("Grand Exchange Clerk");
			if (GrandExchangeClerk != null){
				GrandExchangeClerk.interact("Exchange");
				sleepUntil(()->!getGrandExchange().isOpen(),3000);
				getGrandExchange().sellItem("Battlestaff", getInventory().count(1392), 8750);
				sleepUntil(()->getGrandExchange().sellItem("Battlestaff", getInventory().count(1392), 8750),3000);
				getGrandExchange().collect();
				sleepUntil(()->getGrandExchange().collect(),3000);
				getGrandExchange().close();
				sleep(3000,4500);
			
			
			}
			State = 6;
	}
	}

	public void buy() {
		Zaff = getNpcs().closest("Zaff");
		if (Zaff.isOnScreen()) {
			getCamera().rotateToPitch(Calculations.random(32, 45));
			sleep(2000, 3400);
			getCamera().mouseRotateToEntity(Zaff);
			Zaff.interact("trade");
			log("zaf");
			sleep(900, 1700);
			if (getShop().isOpen() && getShop().count("Battlestaff") != 0) {
				prePurchase = getInventory().count(1391);
				if (Calculations.random(1, 2) == 1) {
					getShop().purchaseFive(1391);
				} else  {
					getShop().purchaseTen(1391);
				}
			

				sleep(1000, 2000);
				postPurchase = getInventory().count(1391);
				sleep(1000,2000);
				totalStaffs += (postPurchase-prePurchase);
				getShop().close();
				sleep(3000,4500);

				State = 1; // worldhop

			}
			else if(getShop().isOpen() && getShop().count("Battlestaff") == 0){
				sleep(3000,4500);
				getShop().close();
				sleep(3000,4500);

				State = 1;
			}
		}
	}

	public void walkToBank() {
		getCamera().rotateToPitch(Calculations.random(30, 40));
		if (getWalking().getDestinationDistance() <= Calculations.random(3, 8)) {
			getWalking().walk(BankLocation.VARROCK_WEST.getCenter());
		}
		sleep(300, 500);
		if (BankLocation.VARROCK_WEST.getCenter().distance(getLocalPlayer()) <= 3) {
			State = 3;
		}
	}
	public void onPaint(Graphics g){
			g.setFont(new Font("Times New Roman", Font.PLAIN, 16)); 

		    g.drawString("Amount Purchased: "+totalStaffs, 30, 150);
		    g.drawString("Profit: "+(totalStaffs*1400), 30, 180);
		    
	}

	public void bank(){
		bank = getGameObjects().closest("Bank booth");
		if(bank!=null){ 
			log("Banked.");
			getCamera().rotateToEntity(bank);
			bank.interact("Bank");
			sleep(3000,4000);
			getBank().depositAll("Battlestaff");
			getBank().deposit(1391, 27);
			sleepUntil(()->!getInventory().isFull(), 3000);
			sleep(3000,4000);
			getBank().close();
			sleep(3000,5000);
			State = 6;
		}
	}

	private World getWorld() {
		World e = getWorlds().getRandomWorld(w -> w != null && !w.isDeadmanMode() && !w.isF2P() && !w.isHighRisk()
				&& !w.isLastManStanding() && !w.isPVP() && w.isMembers() && !w.equals(getClient().getCurrentWorld())
				&& w.getID() != 353 && w.getID() != 366 && w.getID() != 373 && w.getID() != 349 && w.getID() != 361);
		return e;

	}
}
