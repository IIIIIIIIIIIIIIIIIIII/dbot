import java.awt.Color;
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
	Timer foremanTimer;
	Timer realTime;
	Tile conveyorPreTile = new Tile(1937, 4967);
	Tile conveyorTile = new Tile(1942, 4967);
	int tempStore = 0;
	int tempStore2; 
	Boolean needsToCool = true;
	public int State = 0;

	
	public void onStart(){
	getSkillTracker().start(Skill.SMITHING);

	realTime = new Timer();
	foremanTimer = new Timer();
	payForeman();
	
	/*(	ironClick = getWidgets().getWidgetChild(28, 109);
		if(ironClick!= null){
			ironClick.interact();}*/
		
		
	}
	
	public void onPaint(Graphics g){
		Color myColor = new Color(0, 0, 0, 150);
		Color greenColor = new Color(0, 239, 80, 150);

	    Font helvetica = new Font ("Helvetica", Font.BOLD, 13);  
	    g.setFont(helvetica);
		g.setColor(myColor);
		g.fillRect(50, 40, 100, 180);
		g.setColor(greenColor);

		g.drawString(realTime.formatTime(), 50, 55);

		g.drawString("Ores/hr "+ realTime.getHourlyRate(oresSmelted), 50, 110);
		
		g.drawString("Ores: "+ oresSmelted, 50, 80);
		g.drawString("XP/hr: "+ getSkillTracker().getGainedExperiencePerHour(Skill.SMITHING), 50, 140);
		g.drawString("XP gained: "+ getSkillTracker().getGainedExperience(Skill.SMITHING), 50, 170);
		g.drawString("State: "+ State, 50, 200);


	}
	
	@Override
	public int onLoop() {
		if (getDialogues().inDialogue() && getDialogues().canContinue()) { getDialogues().clickContinue();}
		
		if(foremanTimer.elapsed()>550000){
			tempStore = State;
			log("tempStore is equal to: "+tempStore);
			if(State == 5){
				getMouse().move(new Point(Calculations.random(482, 488),Calculations.random(40, 50)));
				getMouse().click();
			}
			if(State == 0){
				if(getBank().isOpen()){
				getMouse().move(new Point(Calculations.random(482, 492),Calculations.random(19, 29)));
				getMouse().click();
				}
			}
			State = 6;
			foremanTimer.reset();
		}
		
		if(State ==0){
			Bank();
		}
		if(State == 1){
			walkOff1();
		}
		if(State == 2){
			walkOff2();
		}
		if(State ==3){
			Smelt();
		}
		if(State ==4){
			coolAndCollect();
		}
		if(State ==5){
			
			barCollect();
			
		}
		if(State ==6){
			
			payForeman();
			State = tempStore;
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
			
			sleep(500,800);
			bank.interact("Use");
			
			sleepUntil(() -> getBank().isOpen(), 15000);
			if(getInventory().contains(2351)){
				getBank().depositAll(2351);
				sleepUntil(() -> !getInventory().contains(2351), 15000);

			}
			
			
			//iron ore withdrawal
			if(!getInventory().contains(440) || !getInventory().isFull()){
			getBank().withdrawAll(440);
			sleepUntil(() -> getInventory().contains(440), 15000);
			}
			getBank().close();
			sleepUntil(() -> !getBank().isOpen(), 15000);

			State = 1;
			log("bank DONE");
		}
	}
	
	public void walkOff1(){
		furnace = getGameObjects().closest(9100);

		if(getInventory().contains(2351) || !getInventory().contains(440)){
			State = 0;
		}
		log("WalkOff");
		sink = getGameObjects().closest(9143);
		if(getInventory().contains(1925)){
			
			getInventory().get(1925).useOn(sink);
			sleepUntil(() -> getInventory().contains(1929), 15000);
		}
		getCamera().rotateToTile(new Tile(1937, 4967));
		sleep(1000,2000);
		if (getWalking().getDestinationDistance() <= Calculations.random(1, 2)) {
			getWalking().walk(new Tile(1937, 4967));
		}
		sleep(1500, 2300);
		if (conveyorPreTile.distance(getLocalPlayer()) <= Calculations.random(3,5)) {
			State = 2;
		}
	}
	public void walkOff2(){
		getCamera().rotateToEntity(furnace);
		sleep(500,900);
		if (getWalking().getDestinationDistance() <= Calculations.random(4, 7)) {
			getWalking().walk(conveyorTile);
		}
		sleep(1000, 2000);
		if (conveyorTile.distance(getLocalPlayer()) <= Calculations.random(3,5)) {
			State = 3;
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
			State = 4;
			
		}
	}
	
	public void coolAndCollect(){
		furnace = getGameObjects().closest(9100);

		needsToCool = true;
		barDispenser = getGameObjects().closest(9092);
		if(barDispenser != null){
			if(needsToCool){
				getInventory().get(1929).useOn(barDispenser);
				sleepUntil(() -> getLocalPlayer().distance(barDispenser) <= 1, 15000);

			}
			
			State = 5;
			log("cool walk over");
			}
		}
	
	public void barCollect(){
		log("collecting");
		ironClick = getWidgets().getWidgetChild(28, 109);
		barDispenser = getGameObjects().closest(9092);
		if(getDialogues().inDialogue()){
			State = 4;
			sleep(2000,4000);
		}
		if(barDispenser!=null && barDispenser.hasAction("Take")){
			barDispenser.interact("Take");
			sleep(500,800);
		}
		if(barDispenser!=null && barDispenser.hasAction("Search")){
			getMouse().moveMouseOutsideScreen();
			sleepUntil(() -> barDispenser.hasAction("Take"), 15000);
			barDispenser.interact("Take");
		}
		if(ironClick != null){
			log("widget not null");
			ironClick.interact();
			if(!getInventory().contains(2351)){
				ironClick.interact();
			}
			sleepUntil(() -> getInventory().contains(2351), 15000);
			getMouse().move(new Point(Calculations.random(482, 488),Calculations.random(40, 50)));
			getMouse().click();
			
			if(!getInventory().contains(2351)){
				State = 5;
			}
			bank = getGameObjects().closest(26707);
			if (bank != null){
				
				bank.interact("Use");
			}
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
