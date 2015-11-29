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

	public Genetic() {
		parseGeneticCode();
	}

	/**
	 * run: Crazy's main run function
	 */
	public void run() {
		// Set colors
		setBodyColor(new Color(0, 200, 0));
		setGunColor(new Color(0, 150, 50));
		setRadarColor(new Color(0, 100, 100));
		setBulletColor(new Color(255, 255, 100));
		setScanColor(new Color(255, 200, 200));

		
		setInitialState();
		// Loop forever
		while (true) {
			handleDriving();
			mainCode.run();
		}
	}

	private void parseGeneticCode() {
		//test genetic code
		mainCode = getOr(testGunIsHot(), testGunIsHot(), getFire1());
		onScannedRobotCode = getFire1();
		onHitRobotCode = getFire1();
		onBulletHitCode = getFire1();
		onBulletMissedCode = getFire1();
	}

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
					setInitialState();
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

	/**
	* The genome implementation functions
	*/
	private Callable<Boolean> testGunIsHot() {
		return new Callable<Boolean>() {
		    public Boolean call() {
		    	if ( getGunHeat() > 0 ) {
					return Boolean.TRUE;
				}
				else {
					return Boolean.FALSE;
				}
		    }
		};
	}


	// Not sure what this means
	private boolean TestEnemyEnergy() {
		//TODO
		return true;
	}


	private boolean TestEnemyEnergy0() {
		return enemyEnergy == 0;
	}	

	private boolean TestEnemyEnergyBelow10() {
		return enemyEnergy < 10;
	}

	private boolean TestEnergyBelow10() {
		return getEnergy() < 10;
	}

	private boolean TestEnergyGreaterThanEnemys() {
		return getEnergy() > enemyEnergy;
	}

	private boolean TestEnergyLessThanEnemys() {
		return getEnergy() < enemyEnergy;
	}

	private boolean TestEnemyWithin10Ticks() {
		return enemyDistance / getVelocity() < 10;
	}	
	private boolean TestEnemyWithin20Ticks() {
		return enemyDistance / getVelocity() < 20;
	}	

	private boolean TestEnemyWithin50Ticks() {
		return enemyDistance / getVelocity() < 50;
	}


	// Not sure what this means	
	private boolean TestGunWithin5Ticks() {
		//TODO
		return true;
	}


	// We can set our own turning rate right?
	private boolean TestTurnToEnemyWithin10Ticks() {
		// I assume we use the max turning speed for gun
		return enemyBearing/20 < 10;
	}	


	private boolean TestTurnToEnemyWithin5TIcks() {
		// I assume we use the max turning speed for gun
		return enemyBearing/20 < 5;
	}	


	private Runnable getOr(Callable<Boolean> test1, Callable<Boolean> test2, Runnable action) {
		return new Runnable() {
		    public void run() {
			//	boolean test1bool = test1.test(Boolean.TRUE);
			//	boolean test2bool = test2.test(Boolean.TRUE);
				if(test1.call() || test2.call())
					action.run();
		    }
		};
	}

	private Runnable getFire1() {
		return new Runnable() {
		    public void run() {
		    	fire(1);
		    }
		};
	}

	private Runnable getFire2() {
		return new Runnable() {
		    public void run() {
		    	fire(2);
		    }
		};
	}

	private Runnable getFire3() {
		return new Runnable() {
		    public void run() {
		    	fire(3);
		    }
		};
	}

	
}
