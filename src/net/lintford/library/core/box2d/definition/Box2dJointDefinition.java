package net.lintford.library.core.box2d.definition;

import org.jbox2d.dynamics.joints.JointDef;

public class Box2dJointDefinition {

	// --------------------------------------
	// Variables
	// --------------------------------------

	public JointDef jointDef;
	
	// Joint data is stored directly in the JointDef instance above.

	public int bodyAIndex;
	public int bodyBIndex;

	public boolean collideConnected;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public Box2dJointDefinition() {

	}

}