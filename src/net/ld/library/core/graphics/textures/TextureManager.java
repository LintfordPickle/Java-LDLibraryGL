package net.ld.library.core.graphics.textures;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

public class TextureManager {

	// --------------------------------------
	// Constants
	// --------------------------------------

	/** A static texture available to all library components. */
	public static final Texture CORE_TEXTURE = Texture.loadTextureFromResource("/res/textures/core_ui.png", GL11.GL_NEAREST);

	// --------------------------------------
	// Variables
	// --------------------------------------

	private static TextureManager mTextureManager;
	private Map<String, Texture> mTextures;

	// --------------------------------------
	// Properties
	// --------------------------------------

	/**
	 * Returns an instance of {@link TextureManager}, which can be used to load texture images.
	 */
	public static TextureManager textureManager() {
		if (mTextureManager == null) {
			mTextureManager = new TextureManager();
		}

		return mTextureManager;
	}

	/**
	 * Returns the texture with the given name, if it exists. null is returned otherwise.
	 */
	public Texture getTexture(String pName) {
		if (mTextures.containsKey(pName)) {
			return mTextures.get(pName);
		}

		return null;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	/**
	 * Creates a new instance of {@link TextureManager}. The TextureManager uses the singleton pattern, and only one instance exists at a time. Use textureManager() to retrieve an instance.
	 */
	private TextureManager() {
		mTextures = new HashMap<String, Texture>();
	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	/**
	 * loads a texture from the given filename and assigns it the given name. The {@link Texture} instance loaded is returned (can be null).
	 */
	public Texture loadTextureFromFile(String pName, String pTextureLocation) {
		return loadTextureFromFile(pName, pTextureLocation, GL11.GL_LINEAR);
	}

	/**
	 * loads a texture from the given filename and assigns it the given name. The {@link Texture} instance loaded is returned (can be null). pFilter applies the GL11 texture filter.
	 */
	public Texture loadTextureFromFile(String pName, String pTextureLocation, int pFilter) {
		if (mTextures.containsKey(pName)) {
			return mTextures.get(pName);
		}

		Texture lTex = Texture.loadTextureFromFile(pTextureLocation, pFilter);
		mTextures.put(pName, lTex); // cache

		return lTex;
	}

	/**
	 * loads a texture from the given resource name embedded in the jar and assigns it the given name. The {@link Texture} instance loaded is returned (can be null).
	 */
	public Texture loadTextureFromResource(String pName, String pTextureLocation) {
		return loadTextureFromResource(pName, pTextureLocation, GL11.GL_LINEAR);
	}

	/**
	 * loads a texture from the given resource name embedded in the jar and assigns it the given name. The {@link Texture} instance loaded is returned (can be null). pFilter applies the GL11 texture filter.
	 */
	public Texture loadTextureFromResource(String pName, String pTextureLocation, int pFilter) {
		if (mTextures.containsKey(pName)) {
			return mTextures.get(pName);
		}

		Texture lTex = Texture.loadTextureFromResource(pTextureLocation, pFilter);
		mTextures.put(pName, lTex); // cache

		return lTex;
	}

	public boolean saveTextureToFile(int pWidth, int pHeight, int[] pData, String pFileLocation) {
		BufferedImage lImage = new BufferedImage(pWidth, pHeight, BufferedImage.TYPE_INT_ARGB);

		// Convert our ABGR to output ARGB
		int[] lTextureData = new int[pWidth * pHeight];
		for (int i = 0; i < pWidth * pHeight; i++) {
			int a = (pData[i] & 0xff000000) >> 24;
			int b = (pData[i] & 0xff0000) >> 16;
			int g = (pData[i] & 0xff00) >> 8;
			int r = (pData[i] & 0xff);

			lTextureData[i] = a << 24 | r << 16 | g << 8 | b;
		}

		lImage.setRGB(0, 0, pWidth, pHeight, lTextureData, 0, pWidth);

		File outputfile = new File(pFileLocation);
		try {
			ImageIO.write(lImage, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;

	}
	
	/** Unloads the specified texture, if applicable. */
	public boolean unloadTexture(Texture pTexture) {
		if (pTexture == null)
			return false; // already lost reference

		if (mTextures.containsValue(pTexture)) {

			Texture.unloadTexture(pTexture);

			mTextures.remove(pTexture);

			return true;
		}

		return false;

	}

	public Texture createFontTexture(String pName, BufferedImage pImage) {
		return createFontTexture(pName, pImage, GL11.GL_NEAREST);
	}

	public Texture createFontTexture(String pName, BufferedImage pImage, int pFilter) {
		if (mTextures.containsKey(pName)) {
			return mTextures.get(pName);
		}

		Texture lTex = Texture.createTexture(pImage, pName, pFilter);
		lTex.reloadable(false);
		mTextures.put(pName, lTex);

		return lTex;
	}

	/** Forces reload of all previously loaded textures. */
	public void reloadTextures() {
		System.out.println("Reloading all textures ..");

		for (Texture lTexture : mTextures.values()) {
			if (lTexture != null) {
				if (!lTexture.reloadable())
					continue;
				lTexture.reload();
			}
		}
	}

}