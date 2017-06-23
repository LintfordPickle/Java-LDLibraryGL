package net.ld.library.cellworld.controllers;

import net.ld.library.cellworld.EntityPool;
import net.ld.library.cellworld.collisions.IEntityCollider;
import net.ld.library.cellworld.collisions.IGridCollider;
import net.ld.library.cellworld.entities.CellEntity;
import net.ld.library.cellworld.entities.CircleCollider;
import net.ld.library.cellworld.entities.RectangleCollider;
import net.ld.library.core.maths.MathHelper;
import net.ld.library.core.time.GameTime;

/** Controller a single instance of {@link CellEntity}. */
public abstract class CirclePlatformerController {

	// -------------------------------------
	// Constants
	// -------------------------------------

	public static final float EPSILON = 0.001f;
	public static final float MOVEMENT_EPSILON = 0.3f;

	// -------------------------------------
	// Variables
	// -------------------------------------

	/** If a {@link IGridCollider} object is available, each of the {@link RectangleCollider}s in the {@code mEntityManager} will be checked for collisions against it. */
	protected IGridCollider mGridCollider;

	/** If a {@link IEntityCollider} object is available, each of the {@link RectangleCollider}s in the {@code mEntityManager} will be checked for collisions against it. */
	protected IEntityCollider<CellEntity> mEntityColliders;

	protected EntityPool<CellEntity> mEntityPool;

	public float gravity;
	public float frictionX;
	public float frictionY;

	// -------------------------------------
	// Properties
	// -------------------------------------

	/** The {@link CirclePlatformerController} is considered initialised if it holds a valid reference to an {@link EntityPool}. */
	public boolean isInitialised() {
		return mEntityPool != null;
	}

	public void setGridCollider(IGridCollider pGridCollider) {
		mGridCollider = pGridCollider;
	}

	public void setEntityCollider(IEntityCollider<CellEntity> pEntityCollider) {
		mEntityColliders = pEntityCollider;
	}

	// -------------------------------------
	// Constructor
	// -------------------------------------

	/** constructor, nothing to see. */
	public CirclePlatformerController() {
		frictionX = 0.96f;
		frictionY = 0.96f;
		
	}

	// -------------------------------------
	// Core-Methods
	// -------------------------------------

	/** Initialises the {@link CirclePlatformerController} with an {@link EntityPool}. All {@link RectangleCollider}s within the {@link EntityPool} will be updated. */
	public void initialise(EntityPool<CellEntity> pEntitypool) {
		mEntityPool = pEntitypool;

	}

	public void update(GameTime pGameTime) {
		if (!isInitialised())
			return;

		// Iterate through the circle entities in the EntityManager and update them
		final int MOB_COUNT = mEntityPool.entities().size();
		for (int i = 0; i < MOB_COUNT; i++) {
			final CellEntity CELL_ENTITY = (CellEntity) mEntityPool.entities().get(i);

			if (CELL_ENTITY == null)
				continue;

			if (CELL_ENTITY instanceof CircleCollider) {
				updateEntityPhysics(pGameTime, CELL_ENTITY);

				// TODO (John): Entity STATES can be derived here from the current properties of the object

			}

		}

	}

	// TODO (John): Remove some code pertaining to leftFacing and isOnGround (this should be added somewhere else).
	// TODO (John): Remove the hard-coded CELL_SIZE (this is dependent on the CellGridLevel).
	protected void updateEntityPhysics(GameTime pGameTime, CellEntity pCharacter) {
		if (pCharacter == null)
			return;

		final float CHARACTER_RADIUS = pCharacter.radius - 1.0f;

		final int CELL_SIZE = 64;
		float lDelta = (float) (pGameTime.elapseGameTime() / 1000.0f);

		int blockSize = 1 + (int) (CHARACTER_RADIUS / CELL_SIZE);

		// REPEL ENTITY CODE
		checkEntityCollisions(pGameTime, pCharacter);

		// X component
		pCharacter.rx += pCharacter.dx * lDelta;

		// TODO: Apply different friction depending on in air etc.
		pCharacter.dx *= frictionX;

		// Figure out, based on the width of the character, how much 'room' is left in the edge cells
		final float SIZE_REMAINING = (CHARACTER_RADIUS % CELL_SIZE) / CELL_SIZE;

		// Check collisions on the X-Axis
		for (int y = -blockSize; y < blockSize; y++) {

			// Because we potentially have world entities which are larger than a single cell, we need to check for level collisions which several blocks on each axis.
			// Furthermore, because entity sizes are not always multiples of CELL_SIZE, we need to check if the entity is even present in some of the neighboring cells before
			// proceeding with the collision checks.

			final float TILE_CENTER_Y = ((pCharacter.cy + y) * CELL_SIZE + CELL_SIZE / 2) + (MathHelper.clamp(-y, -1, 1) * CELL_SIZE / 2);
			final float CHARACTER_CENTER_Y = (pCharacter.yy);
			if (Math.abs(TILE_CENTER_Y - CHARACTER_CENTER_Y) >= CHARACTER_RADIUS)
				continue;

			if (pCharacter.dx < 0 && hasLevelCollision(pCharacter.cx - blockSize, pCharacter.cy + y) && pCharacter.rx < SIZE_REMAINING) {
				pCharacter.dx = 0;
				pCharacter.rx = SIZE_REMAINING;
			}
			if (pCharacter.dx > 0 && hasLevelCollision(pCharacter.cx + blockSize, pCharacter.cy + y) && pCharacter.rx > 1 - SIZE_REMAINING) {
				pCharacter.dx = 0;
				pCharacter.rx = 1 - SIZE_REMAINING;
			}

		}

		while (pCharacter.rx < 0) {
			pCharacter.cx--;
			pCharacter.rx++;
		}
		while (pCharacter.rx > 1) {
			pCharacter.cx++;
			pCharacter.rx--;
		}

		if (pCharacter.dx != 0) {
			if (pCharacter.dx < 0)
				pCharacter.isLeftFacing = true;

			else
				pCharacter.isLeftFacing = false;

		}

		// Y component
		pCharacter.dy += gravity*2 * lDelta;
		pCharacter.ry += pCharacter.dy * lDelta;

		// Check collisions on the Y-Axis
		boolean onFloorThisTurn = false;
		for (int x = -blockSize; x <= blockSize; x++) {

			// Because we potentially have world entities which are larger than a single cell, we need to check for level collisions which several blocks on each axis.
			// Furthermore, because entity sizes are not always multiples of CELL_SIZE, we need to check if the entity is even present in some of the neighboring cells before
			// proceeding with the collision checks.

			final float TILE_CENTER_X = ((pCharacter.cx + x) * CELL_SIZE + CELL_SIZE / 2) + (MathHelper.clamp(-x, -1, 1) * CELL_SIZE / 2);
			final float CHARACTER_CENTER_X = (pCharacter.xx);
			if (Math.abs(TILE_CENTER_X - CHARACTER_CENTER_X) >= CHARACTER_RADIUS)
				continue;

			// Collision with ceiling
			if (pCharacter.dy < 0 && hasLevelCollision(pCharacter.cx + x, pCharacter.cy - blockSize) && pCharacter.ry < SIZE_REMAINING) {
				pCharacter.dy = 0;
				pCharacter.ry = SIZE_REMAINING;
			}

			// Collision with ground
			if (pCharacter.dy > 0 && hasLevelCollision(pCharacter.cx + x, pCharacter.cy + blockSize) && pCharacter.ry > 1 - SIZE_REMAINING) {
				pCharacter.dy = 0;
				pCharacter.ry = 1 - SIZE_REMAINING;
				pCharacter.isOnGround = true;

				onFloorThisTurn = true;

			} else {
				if (!onFloorThisTurn) {
					pCharacter.isOnGround = false;

				}

			}

		}

		while (pCharacter.ry < 0) {
			pCharacter.cy--;
			pCharacter.ry++;
		}
		while (pCharacter.ry > 1) {
			pCharacter.cy++;
			pCharacter.ry--;
		}
		
		pCharacter.dy *= frictionY;

		// Update the final position of the particle (used for rendering sprites etc.)
		pCharacter.xx = (pCharacter.cx + pCharacter.rx) * CELL_SIZE;
		pCharacter.yy = (pCharacter.cy + pCharacter.ry) * CELL_SIZE;

	}

	/** If a valid {@link IEntityCollider} reference is available, we will use it to check for collision against the given CellEntity. */
	protected abstract void checkEntityCollisions(GameTime pGameTime, CellEntity pCellWorldEntity);

	/** If a valid {@link IGridCollider} reference is available, we will use it to check for collision against the given point. */
	protected abstract boolean hasLevelCollision(int pCellGridX, int pCellGridY);

}