import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.MessageListener;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.dreambot.api.utilities.Timer;

@ScriptManifest(author = "Xaklon", category = Category.MISC, description = "Runs Blast Furnace", name = "bfurns 5.0", version = 4.0)
public class Main extends AbstractScript {
	GameObject furnace;
	GameObject sink;
	GameObject bank;
	GameObject barDispenser;
	public int oresSmelted = 0;
	NPC foreman;
	WidgetChild ironClick;
	Timer t;
	Timer realTime;
	Tile conveyorTile = new Tile(1942, 4967);
	Boolean needsToCool = true;
	public int State = 0;
	/*
	 * 0 is smelting
	 * 1 is running 
	 * 2 is water bucket 
	 * 3 is waiting for fix
	 * 4 is banking
	 * 
	 * 
	 * conveyor belt id = 9100 
	 * tile = 1943, 4967 
	 * put-ore-on
	 * fill-bucket
	 * 
	 * SINK: 9143
	 * foreman:2923
	 * BANKCHEST: 26707
	 * bankdeposit: 6948
	 * bardispenser: 9092
	 */
	
	
	public void onStart(){
	getSkillTracker().start(Skill.SMITHING);

	realTime = new Timer();
	t = new Timer();
	payForeman();
	
	/*(	ironClick = getWidgets().getWidgetChild(28, 109);
		if(ironClick!= null){
			ironClick.interact();}*/
		
		
	}
	
	public void onPaint(Graphics g){
		g.drawString("" + t.elapsed(), 50, 50);
		g.drawString("Ores per hour: "+ realTime.getHourlyRate(oresSmelted), 50, 110);
		g.drawString("Ores: "+ oresSmelted, 50, 80);
		g.drawString("XP/hr: "+ getSkillTracker().getGainedExperiencePerHour(Skill.SMITHING), 50, 160);
		g.drawString("XP gained: "+ getSkillTracker().getGainedExperiencePerHour(Skill.SMITHING), 50, 120);


	}
	@Override
	public int onLoop() {
		if (getDialogues().inDialogue() && getDialogues().canContinue()) { getDialogues().clickContinue();}
		if(t.elapsed()>= 600000){ //599500){
			payForeman();
			State = 0;
			t.reset();
		}
		
		
		if(State ==0){
			Bank();
		}
		if(State == 1){
			walkOff();
		}
		if(State == 2){
			Smelt();
		}
		if(State ==3){
			coolAndCollect();
		}
		if(State ==4){
			barCollect();
		}
		
		
		return 0;
	}
	
	public void payForeman(){
		foreman = getNpcs().closest(2923);
		if(foreman!=null){
			foreman.interact("Pay");
			sleepUntil(() -> getDialogues().inDialogue(), 15000);
			getKeyboard().type("1");
			
		}
		
	}
	
	public void Bank(){
		log("bank");

		bank = getGameObjects().closest(26707);
		if (bank != null){
			
			sleep(1000,2000);
			bank.interact("Use");
			sleepUntil(() -> getBank().isOpen(), 15000);
			if(getInventory().contains(2351)){
				getBank().depositAll(2351);
				sleepUntil(() -> !getInventory().contains(2351), 15000);

			}
			
			
			sleep(3000,4000);
			//iron ore withdrawal
			if(!getInventory().contains(440)){
			getBank().withdrawAll(440);
			sleepUntil(() -> getInventory().contains(440), 15000);
			}
			sleep(1000,2000);
			getBank().close();
			sleepUntil(() -> !getBank().isOpen(), 15000);

			State = 1;
			log("bank DONE");
		}
	}
	
	public void walkOff(){
		log("WalkOff");
		sink = getGameObjects().closest(9143);
		if(getInventory().contains(1925)){
			
			getInventory().get(1925).useOn(sink);
			sleepUntil(() -> getInventory().contains(1929), 15000);
		}
		getCamera().rotateToEntity(furnace);
		sleep(1000,2000);
		if (getWalking().getDestinationDistance() <= Calculations.random(4, 7)) {
			getWalking().walk(new Tile(1942, 4967));
		}
		sleep(300, 500);
		if (conveyorTile.distance(getLocalPlayer()) <= Calculations.random(3,5)) {
			sleep(2000,3000);
			State = 2;
		}
	}
	
	public void Smelt(){
		furnace = getGameObjects().closest(9100);
		getCamera().rotateToEntity(furnace);
		if (furnace != null){
			furnace.interact("Put-ore-on");
			sleepUntil(() -> getDialogues().inDialogue(), 15000);
			getKeyboard().type("1");
		
			sleepUntil(() -> !getInventory().contains(440), 15000);
			oresSmelted+=26;
			State = 3;
			
		}
	}
	
	public void coolAndCollect(){
		needsToCool = true;
		barDispenser = getGameObjects().closest(9092);
		if(barDispenser!=null){
			if(needsToCool){
				getInventory().get(1929).useOn(barDispenser);
				//sleepUntil(() -> getLocalPlayer().distance(barDispenser), 15000);

				sleep(3000,5000);
			}
			
			sleep(1000,2000);
			State = 4;
			log("cool walk over");
			}
		}
	
	public void barCollect(){
		log("collecting");
		ironClick = getWidgets().getWidgetChild(28, 109);
		barDispenser = getGameObjects().closest(9092);
		if(barDispenser!=null && barDispenser.hasAction("Take")){
			barDispenser.interact("Take");
			sleep(1000,2000);
		}
		if(barDispenser!=null && barDispenser.hasAction("Search")){
			sleepUntil(() -> barDispenser.hasAction("Take"), 15000);
			barDispenser.interact("Take");
		}
		if(ironClick != null){
			log("widget not null");
			ironClick.interact();
			sleepUntil(() -> getInventory().contains(2351), 15000);
			getMouse().move(new Point(Calculations.random(482, 488),Calculations.random(40, 50)));
			getMouse().click();
			State = 0;
	}
	
	}
	
	@Override
	public void onMessage(Message m) {
		if (m.getMessage().contains("You don't need to cool the bars")) {
			needsToCool = false;
		
		}
	}
	
	
	public void runToFurnace(){
		
		
	}
	
	
	
	

}
