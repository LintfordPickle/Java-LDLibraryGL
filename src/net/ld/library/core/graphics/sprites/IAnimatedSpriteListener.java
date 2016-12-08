package net.ld.library.core.graphics.sprites;

public interface IAnimatedSpriteListener {

	public abstract void onStarted(AnimatedSprite pSender);
	public abstract void onLooped(AnimatedSprite pSender);
	public abstract void onStopped(AnimatedSprite pSender);
	
}
