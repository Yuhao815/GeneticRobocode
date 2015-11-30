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
public class Genetic extends AdvancedRobot {
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
		
		mainCode = Or(TestGunIsHot(), TestGunIsHot(), Fire1());
		onScannedRobotCode = Fire1();
		onHitRobotCode = Fire1();
		onBulletHitCode = Fire1();
		onBulletMissedCode = Fire1();
	}

	private List<String> readGenome() {
		File file = new File("genome.txt");
		List<String> subtrees = new ArrayList<String>();
		try{
			Scanner genomeIn = new Scanner(file);

			Pattern genomePattern = Pattern.compile("Root\\((.*),(.*),(.*),(.*),(.*)\\)"); //"\\w*Root\\w*\\(\\w*(.*),\\w*(.*)\\w*,\\w*(.*)\\w*,\\w*(.*)\\w*,\\w*(.*)\\w*\\)\\w*"
			Matcher genomeMatcher = genomePattern.matcher(genomeIn.nextLine());
			genomeMatcher.matches();

			subtrees.add(genomeMatcher.group(1));
			subtrees.add(genomeMatcher.group(2));
			subtrees.add(genomeMatcher.group(3));
			subtrees.add(genomeMatcher.group(4));
			subtrees.add(genomeMatcher.group(5));

			genomeIn.close();
			
			PrintWriter writer = new PrintWriter("C:\\Users\\sam\\Desktop\\Github\\GeneticRobocode\\genome-parse.txt", "UTF-8");
			writer.println("Testing");
			for(String subtree : subtrees) {
				writer.println(subtree);
			}
			writer.flush();
			writer.close();
		}
		catch(FileNotFoundException e){}
		catch(UnsupportedEncodingException e){}

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
	
}
