package net.lintford.library.core.graphics.particles.modifiers;

import net.lintford.library.core.collisions.IGridCollider;
import net.lintford.library.core.graphics.particles.Particle;
import net.lintford.library.core.time.GameTime;

/** Particles collide with ground */
public class ParticleGroundColisionModifier implements IParticleModifier {

	// --------------------------------------
	// Variables
	// --------------------------------------

	IGridCollider mIGridCollider;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public ParticleGroundColisionModifier(IGridCollider pIGridCollider) {
		mIGridCollider = pIGridCollider;
	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialise(Particle pParticle) {

	}

	@Override
	public void update(GameTime pGameTime) {

	}

	@Override
	public void updateParticle(Particle pParticle, GameTime pGameTime) {
		// TODO: unimplemented method
	}
}