package net.lintford.library.core.graphics.particles.modifiers;

import net.lintford.library.ConstantsTable;
import net.lintford.library.core.graphics.particles.Particle;
import net.lintford.library.core.time.GameTime;

public class ParticleDragModifier implements IParticleModifier {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private final static float DRAG_CONSTANT = 0.75f;

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialise(Particle pParticle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(GameTime pGameTime) {

	}

	@Override
	public void updateParticle(Particle pParticle, GameTime pGameTime) {
		pParticle.dx *= DRAG_CONSTANT;
		pParticle.dy *= DRAG_CONSTANT;

		if (Math.abs(pParticle.dx) < ConstantsTable.EPSILON)
			pParticle.dx = 0;
		if (Math.abs(pParticle.dy) < 0.001f)
			pParticle.dy = 0;
	}

}