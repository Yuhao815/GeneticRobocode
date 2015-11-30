/*******************************************************************************
 * Based on the crazy sample robot by:
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 * -------------original license below------------------------------------------
 * Copyright (c) 2001-2014 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 *******************************************************************************/
package genetic;


import robocode.*;
import java.util.function.Predicate;
import java.util.concurrent.Callable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.util.Scanner;
import java.util.regex.*;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import java.awt.*;

/**
 * Genetic - A robot that drives randomly and
 * uses genetically programmed code for firing
 */
public class FinalGenetic extends AdvancedRobot {
	Map testMap = new HashMap<String, Callable<Boolean>>();	
	boolean movingForward;
	private enum DriveState { INITIAL, LEFT_TURN, RIGHT_TURN }
	DriveState drivingState;
	Runnable mainCode;
	Runnable onScannedRobotCode;
	Runnable onHitRobotCode;
	Runnable onBulletHitCode;
	Runnable onBulletMissedCode;
	double enemyEnergy;
	double enemyDistance;
	double enemyBearing;
	boolean isAlive = true;
	//Bullet velocity = 20 - (3 * firepower)
	//0 to fix indexing
	int[] BulletVelocityForPower = {0, 17, 14, 11};

	/**
	 * run: Crazy's main run function
	 */
	public void run() {
		initializeTestMap();
		parseGeneticCode();
		// Set colors
		setBodyColor(new Color(0, 200, 0));
		setGunColor(new Color(0, 150, 50));
		setRadarColor(new Color(0, 100, 100));
		setBulletColor(new Color(255, 255, 100));
		setScanColor(new Color(255, 200, 200));

		setInitialState();
		while (isAlive) {
			handleDriving();
			mainCode.run();
			execute();
		}
	}

	
	private void parseGeneticCode() {
		
		List<String> commands = readGenome();
		
		// mainCode = And(TestEnemyWithin50TicksOfFire3(), TestEnemyWithin50TicksOfFire1(), Not(TestEnergyLessThanEnemys(), Or(TestEnergyLessThanEnemys(), TestTurnToEnemyWithin5Ticks(), Fire3())));
		// onScannedRobotCode = Fire1();
		// onHitRobotCode = Fire1();
		// onBulletHitCode = Fire1();
		// onBulletMissedCode = Fire1();

		mainCode = parseCommand(commands.get(0));
		onScannedRobotCode = parseCommand(commands.get(1));
		onHitRobotCode = parseCommand(commands.get(2));
		onBulletHitCode = parseCommand(commands.get(3));
		onBulletMissedCode = parseCommand(commands.get(4));
	}

	private List<String> readGenome() {
		File file = new File("finalGenome.txt");
		List<String> subtrees;
		try{
			Scanner genomeIn = new Scanner(file);

			// Pattern genomePattern = Pattern.compile("Root\\((.*),(.*),(.*),(.*),(.*)\\)"); //"\\w*Root\\w*\\(\\w*(.*),\\w*(.*)\\w*,\\w*(.*)\\w*,\\w*(.*)\\w*,\\w*(.*)\\w*\\)\\w*"
			// Matcher genomeMatcher = genomePattern.matcher(genomeIn.nextLine());
			// genomeMatcher.matches();

			subtrees = getParameters(genomeIn.nextLine());

			// subtrees.add(genomeMatcher.group(1));
			// subtrees.add(genomeMatcher.group(2));
			// subtrees.add(genomeMatcher.group(3));
			// subtrees.add(genomeMatcher.group(4));
			// subtrees.add(genomeMatcher.group(5));

			genomeIn.close();
			
			PrintWriter writer = new PrintWriter("genome-parse.txt", "UTF-8");
			writer.println("Testing");
			for(String subtree : subtrees) {
				writer.println(subtree);
			}
			writer.flush();
			writer.close();
		}
		catch(FileNotFoundException e){ subtrees = new ArrayList<String>(); }
		catch(UnsupportedEncodingException e){ subtrees = new ArrayList<String>(); }

		return subtrees;
	}

//Robot helper functions
	private void handleDriving() {
		//If the turning is finished, advance the state
		Condition turned = new TurnCompleteCondition(this);
		if(turned.test()) {
			switch(drivingState) {
				case INITIAL:
					setLeftTurnState();
					break;
				case LEFT_TURN:
					setRightTurnState();
					break;
				case RIGHT_TURN:
					setLeftTurnState();
					break;
			}
		}
	}

	private void setInitialState() {
		// Tell the game we will want to move ahead 40000 -- some large number
		setAhead(40000);
		movingForward = true;
		// Tell the game we will want to turn right 90
		setTurnRight(90);
		drivingState = DriveState.INITIAL;
	}

	private void setLeftTurnState() {
		// Now we'll turn the other way...
		setTurnLeft(180);
		drivingState = DriveState.LEFT_TURN;
	}

	private void setRightTurnState() {
		// ... then the other way ...
		setTurnRight(180);
		drivingState = DriveState.RIGHT_TURN;
	}

	public void onRoundEnded(RoundEndedEvent e) {
		isAlive = false;
	}
	
	/**
	 * onHitWall:  Handle collision with wall.
	 */
	public void onHitWall(HitWallEvent e) {
		// Bounce off!
		reverseDirection();
	}

	/**
	 * reverseDirection:  Switch from ahead to back & vice versa
	 */
	public void reverseDirection() {
		if (movingForward) {
			setBack(40000);
			movingForward = false;
		} else {
			setAhead(40000);
			movingForward = true;
		}
	}

	/**
	 * onScannedRobot: Use the genetic code
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		enemyEnergy = e.getEnergy();
		enemyDistance = e.getDistance();
		enemyBearing = e.getBearing();
		onScannedRobotCode.run();
	}

	/**
	 * onHitRobot:  Back up and use the genetic code
	 */
	public void onHitRobot(HitRobotEvent e) {
		// If we're moving the other robot, reverse!
		if (e.isMyFault()) {
			reverseDirection();
		}
		onHitRobotCode.run();
	}

	/**
	* onBulletHit: Use the genetic code
	*/
	public void onBulletHit(BulletHitEvent e) {
		onBulletHitCode.run();
	}

	/**
	* onBulletMissed: Use the genetic code
	*/
	public void onBulletMissed(BulletMissedEvent e) {
		onBulletMissedCode.run();
	}
	
//Actions
	private Runnable Fire1() {
		return new Runnable() {
		    public void run() {
		    	fire(1);
		    }
		};
	}

	private Runnable Fire2() {
		return new Runnable() {
		    public void run() {
		    	fire(2);
		    }
		};
	}
	
	private Runnable Fire3() {
		return new Runnable() {
		    public void run() {
		    	fire(3);
		    }
		};
	}
	
	private Runnable TurnGunToEnemy() {
		return new Runnable() {
		    public void run() {
		    	turnGunRight( enemyBearing );
		    }
		};
	}
	
	private Runnable TurnGunRight5() {
		return new Runnable() {
		    public void run() {
		    	turnGunRight(5.0);
		    }
		};
	}
	
	private Runnable TurnGunRight10() {
		return new Runnable() {
		    public void run() {
		    	turnGunRight(10.0);
		    }
		};
	}
	
	private Runnable TurnGunLeft5() {
		return new Runnable() {
		    public void run() {
		    	turnGunLeft(5.0);
		    }
		};
	}
	
	private Runnable TurnGunLeft10() {
		return new Runnable() {
		    public void run() {
		    	turnGunLeft(10.0);
		    }
		};
	}

//Tests
	/**
	* The genome implementation functions
	*/	
	private Callable<Boolean> TestEnemyEnergy0() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyEnergy == 0 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}	

	private Callable<Boolean> TestEnemyEnergyBelow10() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyEnergy < 10 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
		
	}

	private Callable<Boolean> TestEnergyBelow10() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( getEnergy() < 10 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}

	private Callable<Boolean> TestEnergyGreaterThanEnemys() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( getEnergy() > enemyEnergy ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}

	private Callable<Boolean> TestEnergyLessThanEnemys() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( getEnergy() < enemyEnergy ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestEnemyWithin5Ticks() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / getVelocity() < 5 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}

	private Callable<Boolean> TestEnemyWithin10Ticks() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / getVelocity() < 10 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}	
	
	private Callable<Boolean> TestEnemyWithin20Ticks() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / getVelocity() < 20 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}	

	private Callable<Boolean> TestEnemyWithin50Ticks() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / getVelocity() < 50 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestEnemyWithin5TicksOfFire1() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / BulletVelocityForPower[1] < 5 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestEnemyWithin5TicksOfFire2() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / BulletVelocityForPower[2] < 5 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestEnemyWithin5TicksOfFire3() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / BulletVelocityForPower[3] < 5 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestEnemyWithin10TicksOfFire1() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / BulletVelocityForPower[1] < 10 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestEnemyWithin10TicksOfFire2() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / BulletVelocityForPower[2] < 10 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestEnemyWithin10TicksOfFire3() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / BulletVelocityForPower[3] < 10 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestEnemyWithin20TicksOfFire1() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / BulletVelocityForPower[1] < 20 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestEnemyWithin20TicksOfFire2() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / BulletVelocityForPower[2] < 20 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestEnemyWithin20TicksOfFire3() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / BulletVelocityForPower[3] < 20 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestEnemyWithin50TicksOfFire1() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / BulletVelocityForPower[1] < 50 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestEnemyWithin50TicksOfFire2() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / BulletVelocityForPower[2] < 50 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestEnemyWithin50TicksOfFire3() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( enemyDistance / BulletVelocityForPower[3] < 50 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestGunIsHot(){
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
					if ( getGunHeat() > 0 ) {
						return Boolean.TRUE;
					}
					else {
						return Boolean.FALSE;
					}
		    }
		};
	}
	
	private Callable<Boolean> TestGunWithin5Ticks() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
				if ( getGunHeat() <= .5 ) {
					return Boolean.TRUE;
				}
				else {
					return Boolean.FALSE;
				}
		    }
		};
	}
	
	private Callable<Boolean> TestTurnToEnemyWithin10Ticks() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
				if ( enemyBearing <= 200 ) {
					return Boolean.TRUE;
				}
				else {
					return Boolean.FALSE;
				}
		    }
		};
	}	

	private Callable<Boolean> TestTurnToEnemyWithin5Ticks() {
		return new Callable<Boolean>() {
		    public Boolean call() throws Exception {
				if ( enemyBearing <= 100 ) {
					return Boolean.TRUE;
				}
				else {
					return Boolean.FALSE;
				}
		    }
		};
	}	
	
//Logic Functions
	private Runnable Or(final Callable<Boolean> test1, final Callable<Boolean> test2, final Runnable action) {
		return new Runnable() {
		    public  void run() {
				try{
				if(test1.call() || test2.call())
					action.run();
				}
				catch (Exception e){}
		    }
		};
	}
	
	private Runnable And(final Callable<Boolean> test1, final Callable<Boolean> test2, final Runnable action) {
		return new Runnable() {
		    public  void run() {
				try{
				if(test1.call() && test2.call())
					action.run();
				}
				catch (Exception e){}
		    }
		};
	}
	
	private Runnable Not(final Callable<Boolean> test, final Runnable action) {
		return new Runnable() {
		    public  void run() {
				try{
				if(!test.call())
					action.run();
				}
				catch (Exception e){}
		    }
		};
	}
	
	private Runnable If_Then(final Callable<Boolean> test, final Runnable action) {
		return new Runnable() {
		    public  void run() {
				try{
				if(test.call())
					action.run();
				}
				catch (Exception e){}
		    }
		};
	}
	
	private String getCommandName(String commandText) {
		return commandText.split("\\(",  2)[0].trim();
	}

	private List<String> getParameters(String commandText) {
		int parenDepth = 0;
		StringBuilder param = new StringBuilder();
		List<String> parameters = new ArrayList<String>();

		for (int i = 0; i < commandText.length(); i++){
    		char c = commandText.charAt(i);
    		if(c == '(')
    			parenDepth++;
    		else if(c == ')')
    			parenDepth--;

    		if((c == ',' || c == ')') && parenDepth == 1 && param.length() > 0) {
    			if(c == ')')
    				param.append(c);
    			parameters.add(param.toString());
    			param.delete(0, param.length());
    		} 

    		if(parenDepth == 1 && (c == '(' || c == ')' || c == ',' ))
    			continue;
    		if(parenDepth > 0)
    			param.append(c);
		}

		return parameters;
	}

	private Runnable parseCommand(String command) {
		
		String commandName = getCommandName(command);
		List<String> parameters = getParameters(command);
		
		if (commandName.equals("Or"))
			return Or(parseTest(parameters.get(0)), parseTest(parameters.get(1)), parseCommand(parameters.get(2)));
		else if (commandName.equals("And"))
			return And(parseTest(parameters.get(0)), parseTest(parameters.get(1)), parseCommand(parameters.get(2)));
		else if (commandName.equals("Not"))
			return Not(parseTest(parameters.get(0)), parseCommand(parameters.get(1)));
		else if (commandName.equals("If_Then"))
			return If_Then(parseTest(parameters.get(0)), parseCommand(parameters.get(1)));
		else if (commandName.equals("Fire1"))
			return Fire1();
		else if (commandName.equals("Fire2"))
			return Fire2();
		else if (commandName.equals("Fire3"))
			return Fire3();
		else if (commandName.equals("TurnGunToEnemy"))
			return TurnGunToEnemy();
		else if (commandName.equals("TurnGunLeft5"))
			return TurnGunLeft5();
		else if (commandName.equals("TurnGunLeft10"))
			return TurnGunLeft10();
		else if (commandName.equals("TurnGunRight5"))
			return TurnGunRight5();
		else if (commandName.equals("TurnGunRight10"))
			return TurnGunRight10();

		// return new Runnable() {
		//     public  void run() {}
		// };
		return Fire3(); //or throw parse exception?
	}

	private Callable<Boolean> parseTest(String command) {
		String commandName = getCommandName(command);
		if(testMap.containsKey(commandName)) {
			return (Callable<Boolean>)testMap.get(commandName);
		}

		return TestGunIsHot(); //or throw exception?
	}
	
	
	private void initializeTestMap(){	
		testMap.put("TestEnemyEnergy0", TestEnemyEnergy0());
		testMap.put("TestEnemyEnergyBelow10", TestEnemyEnergyBelow10());
		testMap.put("TestEnergyBelow10", TestEnergyBelow10());
		testMap.put("TestEnergyGreaterThanEnemys", TestEnergyGreaterThanEnemys());
		testMap.put("TestEnergyLessThanEnemys", TestEnergyLessThanEnemys());
		testMap.put("TestEnemyWithin5Ticks", TestEnemyWithin5Ticks());
		testMap.put("TestEnemyWithin10Ticks", TestEnemyWithin10Ticks());
		testMap.put("TestEnemyWithin20Ticks", TestEnemyWithin20Ticks());
		testMap.put("TestEnemyWithin50Ticks", TestEnemyWithin50Ticks());
		testMap.put("TestEnemyWithin5TicksOfFire1", TestEnemyWithin5TicksOfFire1());
		testMap.put("TestEnemyWithin5TicksOfFire2", TestEnemyWithin5TicksOfFire2());
		testMap.put("TestEnemyWithin5TicksOfFire3", TestEnemyWithin5TicksOfFire3());
		testMap.put("TestEnemyWithin10TicksOfFire1", TestEnemyWithin10TicksOfFire1());
		testMap.put("TestEnemyWithin10TicksOfFire2", TestEnemyWithin10TicksOfFire2());
		testMap.put("TestEnemyWithin10TicksOfFire3", TestEnemyWithin10TicksOfFire3());
		testMap.put("TestEnemyWithin20TicksOfFire1", TestEnemyWithin20TicksOfFire1());
		testMap.put("TestEnemyWithin20TicksOfFire2", TestEnemyWithin20TicksOfFire2());
		testMap.put("TestEnemyWithin20TicksOfFire3", TestEnemyWithin20TicksOfFire3());
		testMap.put("TestEnemyWithin50TicksOfFire1", TestEnemyWithin50TicksOfFire1());
		testMap.put("TestEnemyWithin50TicksOfFire2", TestEnemyWithin50TicksOfFire2());
		testMap.put("TestEnemyWithin50TicksOfFire3", TestEnemyWithin50TicksOfFire3());
		testMap.put("TestGunIsHot", TestGunIsHot());
		testMap.put("TestGunWithin5Ticks", TestGunWithin5Ticks());
		testMap.put("TestTurnToEnemyWithin10Ticks", TestTurnToEnemyWithin10Ticks());
		testMap.put("TestTurnToEnemyWithin5Ticks", TestTurnToEnemyWithin5Ticks());
	}
}