
import java.awt.Color;
import java.awt.Graphics;

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

@ScriptManifest(author = "Michael W", category = Category.MISC, description = "Blast Furnace B0t", name = "Blast Furnace", version = 4.0)
public class Main extends AbstractScript {
	// var dec
	GameObject bank;
	GameObject sink;
	GameObject furnace;
	GameObject dispenser;

	WidgetChild ironButton;
	WidgetChild closeButton;
	Boolean needsToPay = true;
	
	String[] statesList;
	String stringState;
	
	Timer realTime;
	
	int State;
	int chosenOreID = 440;
	int smeltedBars = 2351; // after ores have been smelted, fetch in a public enum
	int amountSmelted = 0;
	
	Tile preTile = new Tile(1938, 4967); // define later
	Tile conveyorTile = new Tile(1942, 4967);
	Tile dispenserTile = new Tile(1939, 4963);
	Tile nearbyBankTile = new Tile(1946,4959);
	
	public void onStart(){
		State = 0;
		realTime = new Timer();
	
	}
	@Override
	public int onLoop() {
		log("State is "+checkState());
		if (getSkills().getRealLevel(Skill.SMITHING) >= 60) {
			needsToPay = false;
		}
		if (State == 0) {
			bank();
		} else if (State == 1) {
			fillBucket();
		} else if (State == 2) {
			walkToPreTile();
		}  else if (State == 3) {
			smeltOre();
		} else if (State == 4) {
			walkToDispenser();
		} else if (State == 5) {
			pourWater();
		} else if (State == 6) {
			collectOre();
		} else if (State == 7) {
			walkNearBank();
		}
		
		return 0;
	}
	
	public String checkState(){
		if (State == 0) {
			stringState = "Bank";
		} else if (State == 1) {
			stringState = "Fill Water";
		} else if (State == 2) {
			stringState ="Walk 1";
		} else if (State == 3) {
			stringState = "Smelting";
		} else if (State == 4) {
			stringState = "Walk to collect";
		} else if (State == 5) {
			stringState ="Pour Water";
		} else if (State == 6) {
			stringState = "Collecting";
		} else if (State == 7) {
			stringState = "Walk to Bank";
		}
		return stringState;
		
	}
	
	public void onPaint(Graphics g){
		Color myColor = new Color(0, 0, 0, 122);
		g.setColor(myColor);
		g.fillRect(50, 40, 150, 150);
		g.setColor(Color.GREEN);
		g.drawString("State is: " + checkState(), 50, 50);
		g.drawString("Smelted: " + amountSmelted, 50, 80);
        g.drawString("Smelts/hr " + realTime.getHourlyRate(amountSmelted), 50, 110);

		g.drawString("XP/hr: " + getSkillTracker().getGainedExperiencePerHour(Skill.SMITHING), 50, 140);
        g.drawString("XP gained: " + getSkillTracker().getGainedExperience(Skill.SMITHING), 50, 170);
	}
	
	// State 0
	public void bank() {
		bank = getGameObjects().closest(26707);
		if (bank != null) {
			bank.interact();
			sleepUntil(() -> getBank().isOpen(), 15000);
			if (getInventory().contains(smeltedBars)) {
				getBank().depositAll(smeltedBars);
				sleepUntil(() -> !getInventory().contains(smeltedBars), 15000);
			}

			if (!getInventory().contains(chosenOreID)) {
				getBank().withdrawAll(chosenOreID);
				sleepUntil(() -> getInventory().contains(chosenOreID), 15000);
			}

			getBank().close();
			sleepUntil(() -> !getBank().isOpen(), 15000);
			if (getInventory().contains(chosenOreID)) {
				State = 1;
			}
		}
	}

	// State 2
	public void fillBucket() {
		if (getInventory().contains(1925)) {
			
				sink = getGameObjects().closest(9143);
				if (sink != null ) {
					getCamera().rotateToEntity(sink);
					sink.interact("Fill-bucket"); // change string later
					sleepUntil(() -> getInventory().contains(1929),15000);
				} 
					
		}
		if(getInventory().contains(1929)){
			State = 2;
		}
		else if(getInventory().contains(1925)){
			State = 1;
		}
	}

	public void walkToPreTile() {

		 if(getWalking().getDestinationDistance() <=
		  Calculations.random(1,2)){ 
			 getWalking().walk(preTile); 
			 }
		 if(getLocalPlayer().distance(preTile)<=1)
		 {
				furnace = getGameObjects().closest(9100);

				getCamera().rotateToEntity(furnace);
				sleep(Calculations.random(1100, 1500));
				State = 3;
		  }
		 
		
	}
/*
	public void walkToConveyor() {
		  if(getWalking().getDestinationDistance() <=
		  Calculations.random(2,3)){ getWalking().walk(conveyorTile); }
		  if(getLocalPlayer
		  ().distance(conveyorTile)<=Calculations.random(1,2)){ State = 4; }
		 
	
	} */

	public void smeltOre() {
		
		furnace = getGameObjects().closest(9100);
		getCamera().rotateToEntity(furnace);

		if (getInventory().contains(chosenOreID) && furnace != null) {
			furnace.interact("Put-ore-on");
			sleepUntil(() -> getDialogues().inDialogue(), 15000);
			getKeyboard().type("1");
			sleepUntil(() -> !getInventory().contains(chosenOreID), 15000);

		}
		if (!getInventory().contains(chosenOreID)) {
			amountSmelted += 27;
			State = 4;
		}
		else if (getInventory().contains(chosenOreID)){
			State = 3;
		}
	}

	public void walkToDispenser() {

		  if(getWalking().getDestinationDistance() <=
				  Calculations.random(2,3)){ getWalking().walk(dispenserTile); }
				  if(getLocalPlayer
				  ().distance(dispenserTile)==0){ State = 5; }
				 
	}

	public void pourWater() {
		dispenser = getGameObjects().closest(9092); //fix
		getCamera().rotateToEntity(dispenser);

		if (getInventory().contains(1929)) {
				getInventory().get(1929).useOn(dispenser);
				sleepUntil(() -> getInventory().contains(1925), Calculations.random(2300, 3200));
			}
		State = 6;
	
	}
	
	public void collectOre(){
		ironButton = getWidgets().getWidgetChild(28, 109);
		closeButton = getWidgets().getWidgetChild(28, 120);
		dispenser = getGameObjects().closest(9092); 
		if(dispenser != null && dispenser.hasAction("Take")){
			dispenser.interact("Take");
		}
		else if(dispenser != null && dispenser.hasAction("Search")){
			sleepUntil(() -> dispenser.hasAction("Take"),15000);
			dispenser.interact("Take");
		}
		sleepUntil(() -> ironButton.isVisible(), 15000);
		if(ironButton != null){
			ironButton.interact();
		}
		sleepUntil(() -> getInventory().contains(smeltedBars),15000);
		if(closeButton != null && closeButton.isVisible()){
			closeButton.interact();
		}
		sleepUntil(() -> closeButton.isHidden(),15000);
		if(getInventory().contains(smeltedBars)){
			State = 7;
		}
		else if(!getInventory().contains(smeltedBars)){
			State = 6;
		}
	}

	public void walkNearBank(){

		  if(getWalking().getDestinationDistance() <=
				  Calculations.random(2,3)){ getWalking().walk(nearbyBankTile); }
				  if(getLocalPlayer
				  ().distance(nearbyBankTile)<=Calculations.random(1,2)){ 
					  State = 0; 
					  }
				 
	}

	

}
