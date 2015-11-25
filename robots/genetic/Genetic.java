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
package sample;


import robocode.*;

import java.awt.*;


/**
 * Genetic - A robot that drives randomly and
 * uses genetically programmed code for firing
 */
public class Genetic extends AdvancedRobot {
	boolean movingForward;
	private enum DriveState { INITIAL, LEFT_TURN, RIGHT_TURN }
	DriveState state;
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
			//do genetic stuff
		}
	}

	private void handleDriving() {
		//If the turning is finished, advance the state
		Condition turned = new TurnCompleteCondition(this);
		if(turned.test()) {
			switch(state) {
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
		state = INITIAL;
	}

	private void setLeftTurnState() {
		// Now we'll turn the other way...
		setTurnLeft(180);
		state = LEFT_TURN;
	}

	private void setRightTurnState() {
		// ... then the other way ...
		setTurnRight(180);
		state = RIGHT_TURN;
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
		fire(1);
	}

	/**
	 * onHitRobot:  Back up and use the genetic code
	 */
	public void onHitRobot(HitRobotEvent e) {
		// If we're moving the other robot, reverse!
		if (e.isMyFault()) {
			reverseDirection();
		}
	}

	/**
	* onBulletHit: Use the genetic code
	*/
	public void onBulletHit(BulletHitEvent e) {

	}

	/**
	* onBulletMissed: Use the genetic code
	*/
	public void onBulletMissed(BulletMissedEvent e) {

	}
	
}
