package net.lintford.library.core.graphics.sprites;

/** An interface for rendering sprites. A Sprite contains all information about the source rectangle for within a texture when rendering. */
public interface ISprite {

	/** get the source X of the sprite */
	public abstract float getX();

	/** get the source Y of the sprite */
	public abstract float getY();

	/** get the source width of the sprite */
	public abstract int getW();

	/** get the source height of the sprite */
	public abstract int getH();

	/** get the anchor point X */
	public default float getAX() {
		return getW() / 2;
	}

	/** get the anchor point Y */
	public default float getAY() {
		return getH() / 2;
	}

	/** get the pivot point X */
	public default float getPX() {
		return 0;
	}

	/** get the pivot point Y */
	public default float getPY() {
		return 0;
	}

}