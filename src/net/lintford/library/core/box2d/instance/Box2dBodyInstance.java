package net.lintford.library.core.box2d.instance;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import net.lintford.library.ConstantsPhysics;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.box2d.BasePhysicsData;
import net.lintford.library.core.box2d.definition.Box2dBodyDefinition;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.entity.PooledBaseData;

public class Box2dBodyInstance extends PooledBaseData {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = -7217114568668036265L;

	// --------------------------------------
	// Variables
	// --------------------------------------

	public transient Body mBody;
	public BasePhysicsData bodyPhysicsData;

	public String name;
	public int uid;
	public int bodyTypeIndex;
	public Vec2 worldPosition;
	public Vec2 localPosition;
	public float worldAngle;
	public float localAngle;
	public Vec2 linearVelocity;
	public float angularVelocity;
	public float linearDamping;
	public float angularDamping;
	public float gravityScale = 1f;

	public boolean allowSleep = true;
	public boolean awake = true;
	public boolean fixedRotation = false;
	public boolean bullet = true;
	public boolean active = true;

	// Mass data
	public float mass;
	public Vec2 massCenter;
	public float massI;

	public Box2dFixtureInstance[] mFixtures;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public Box2dBodyInstance(int pPoolUid) {
		super(pPoolUid);

		localPosition = new Vec2();
		worldPosition = new Vec2();
		linearVelocity = new Vec2();
		massCenter = new Vec2();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(Object pParent) {
		super.initialize(pParent);

		localPosition = new Vec2();
		worldPosition = new Vec2();
		linearVelocity = new Vec2();
		massCenter = new Vec2();

	}

	public void savePhysics() {
		if (mBody == null)
			return; // nothing to save

		// Get the state information so it can be serialized
		switch (mBody.m_type) {
		case STATIC:
			this.bodyTypeIndex = Box2dBodyDefinition.BODY_TYPE_INDEX_STATIC;
			break;
		case KINEMATIC:
			this.bodyTypeIndex = Box2dBodyDefinition.BODY_TYPE_INDEX_KINEMATIC;
			break;
		case DYNAMIC:
			this.bodyTypeIndex = Box2dBodyDefinition.BODY_TYPE_INDEX_DYNAMIC;
			break;
		}

		this.linearVelocity.set(mBody.getLinearVelocity());

		this.localAngle = mBody.getAngle();
		this.angularVelocity = mBody.getAngularVelocity();
		this.linearDamping = mBody.getLinearDamping();
		this.angularDamping = mBody.getAngularDamping();
		this.gravityScale = mBody.getGravityScale();

		this.allowSleep = mBody.isSleepingAllowed();
		this.awake = mBody.isAwake();
		this.fixedRotation = mBody.isFixedRotation();
		this.bullet = mBody.isBullet();
		this.active = mBody.isActive();

		// iterate over the fixtures
		final int lFixtureCount = mFixtures.length;
		for (int j = 0; j < lFixtureCount; j++) {
			Box2dFixtureInstance lFixtureInstance = mFixtures[j];

			lFixtureInstance.savePhysics();

		}

	}

	public void loadPhysics(World pWorld) {
		BodyDef lBodyDef = new BodyDef();

		switch (bodyTypeIndex) {
		case Box2dBodyDefinition.BODY_TYPE_INDEX_STATIC:
			lBodyDef.type = BodyType.STATIC;
			break;
		case Box2dBodyDefinition.BODY_TYPE_INDEX_KINEMATIC:
			lBodyDef.type = BodyType.KINEMATIC;
			break;
		case Box2dBodyDefinition.BODY_TYPE_INDEX_DYNAMIC:
			lBodyDef.type = BodyType.DYNAMIC;
			break;
		}

		lBodyDef.position.set(ConstantsPhysics.toUnits(worldPosition.x), ConstantsPhysics.toUnits(worldPosition.y));
		lBodyDef.linearVelocity.set(linearVelocity.x, linearVelocity.y);

		lBodyDef.angle = worldAngle;
		lBodyDef.angularVelocity = angularVelocity;
		lBodyDef.linearDamping = linearDamping;
		lBodyDef.angularDamping = angularDamping;
		lBodyDef.gravityScale = gravityScale;

		lBodyDef.allowSleep = false;
		lBodyDef.awake = true;
		lBodyDef.fixedRotation = fixedRotation;
		lBodyDef.bullet = bullet;
		lBodyDef.active = true;

		mBody = pWorld.createBody(lBodyDef);

		if (bodyPhysicsData != null)
			mBody.setUserData(bodyPhysicsData);

		// iterate over the fixtures
		final int lFixtureCount = mFixtures.length;
		for (int i = 0; i < lFixtureCount; i++) {
			Box2dFixtureInstance lFixtureInstance = mFixtures[i];

			lFixtureInstance.loadPhysics(pWorld, mBody);

		}

	}

	public void unloadPhysics() {
		if (mBody == null)
			return;

		final var lBox2dWorld = mBody.m_world;
		if (lBox2dWorld == null)
			return;

		// Destroy all fixtures on this body
		final int lFixtureCount = mFixtures.length;
		for (int i = 0; i < lFixtureCount; i++) {
			final var lBox2dFixtureInstance = mFixtures[i];
			if (lBox2dFixtureInstance.mFixture != null)
				mBody.destroyFixture(lBox2dFixtureInstance.mFixture);
			lBox2dFixtureInstance.mFixture = null;

		}

		if (mBody.getUserData() != null) {
			Debug.debugManager().logger().w(getClass().getSimpleName(), "JBox2dBodyInstance was unloaded without removing the userdata. Typeof (" + mBody.getUserData().toString() + ")");
			mBody.setUserData(null);

		}

		lBox2dWorld.destroyBody(mBody);
		mBody = null;

	}

	public void update(LintfordCore pCore) {

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void setActive(boolean pNewValue) {
		active = pNewValue;

		if (mBody != null) {
			mBody.setActive(pNewValue);

		}

	}

	public void setFixedRotation(boolean pIsFixedRotation) {
		fixedRotation = pIsFixedRotation;

		if (mBody != null) {
			mBody.setFixedRotation(pIsFixedRotation);

		}

	}

	public void setIsBullet(boolean pIsBullet) {
		bullet = pIsBullet;

		if (mBody != null) {
			mBody.setBullet(pIsBullet);

		}

	}

	@Override
	public boolean isAssigned() {
		// TODO Auto-generated method stub
		return false;
	}

}
