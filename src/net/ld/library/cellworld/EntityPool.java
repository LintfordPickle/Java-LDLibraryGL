package net.ld.library.cellworld;

import java.util.ArrayList;
import java.util.List;

import net.ld.library.cellworld.collisions.IEntityCollider;
import net.ld.library.cellworld.entities.CellEntity;

/**
 * The {@link EntityPool} is a convenient stores for a collect of {@link CellEntity}s, for things like saving and loading.
 */
public class EntityPool<T extends CellEntity> implements IEntityCollider<T> {

	// -------------------------------------
	// Variables
	// -------------------------------------

	protected List<T> mEntities;

	// -------------------------------------
	// Properties
	// -------------------------------------

	/** Returns a list of entities currently registered in the world. */
	public List<T> entities() {
		return mEntities;
	}

	// -------------------------------------
	// Constructor
	// -------------------------------------

	/**
	 * 
	 */
	public EntityPool() {
		mEntities = new ArrayList<>(64);

	}

	// -------------------------------------
	// Core-Methods
	// -------------------------------------

	/** Called once after instantiation. */
	public void initialise() {

	}

	// -------------------------------------
	// Methods
	// -------------------------------------

	/**
	 * Adds the given {@link CellEntity} to the {@link EntityPool} and updates its reference to the world. true is returned if the object is successfully added, false is returned otherwise.
	 */
	public boolean addEntity(T pWorldEntity) {
		if (!mEntities.contains(pWorldEntity)) {
			mEntities.add(pWorldEntity);

			return true;

		}

		// Apparently this entity already exists in the world.
		return false;

	}

	/**
	 * Removes the given {@link CellEntity} from the world, if it has previously been registered.
	 */
	public void removeEntity(T pWorldEntity) {
		if (mEntities.contains(pWorldEntity)) {
			mEntities.remove(pWorldEntity);

		}

	}

	@Override
	public void checkEntityCollisions(CellEntity pEntity) {
		// 

	}

}