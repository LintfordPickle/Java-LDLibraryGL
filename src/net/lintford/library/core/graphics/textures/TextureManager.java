package net.lintford.library.core.graphics.textures;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import net.lintford.library.ConstantsTable;
import net.lintford.library.core.debug.DebugManager;

public class TextureManager {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final Texture TEXTURE_NOT_FOUND = TextureManager.textureManager().loadTexture("NOT_FOUND", new int[] { 0xFF00FF, 0xFF00FF, 0xFF00FF, 0xFF00FF }, 2, 2);
	public static final Texture TEXTURE_WHITE = TextureManager.textureManager().loadTexture("WHITE", new int[] { 0xF, 0xF, 0xF, 0xF }, 2, 2);
	public static final Texture TEXTURE_BLACK = TextureManager.textureManager().loadTexture("BLACK", new int[] { 0x0, 0x0, 0x0, 0x0 }, 2, 2);

	/** A static texture which contains 'generic' icons which can be used for core components and debugging. */
	public static final Texture TEXTURE_CORE_UI = TextureManager.textureManager().loadTexture("CORE_UI", "/res/textures/core/core_ui.png", GL11.GL_NEAREST);

	// --------------------------------------
	// Variables
	// --------------------------------------

	private static TextureManager mTextureManager;
	private Map<String, Texture> mTextures;

	// --------------------------------------
	// Properties
	// --------------------------------------

	/** Returns an instance of the {@link TextureManager}. */
	public static TextureManager textureManager() {
		if (mTextureManager == null) {
			mTextureManager = new TextureManager();

		}

		return mTextureManager;
	}

	/** Returns the {@link Texture} with the given name. If no {@link Texture} by the given name is found, a default MAGENTA texture will be returned. */
	public Texture getTexture(String pName) {
		if (mTextures.containsKey(pName)) {
			return mTextures.get(pName);
		}

		// In case the requested texture is not found, then return a default MAGENTA texture.
		return TextureManager.TEXTURE_NOT_FOUND;

	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	private TextureManager() {
		mTextures = new HashMap<String, Texture>();
	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	public void unloadGLContent() {

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public Texture loadTexture(String pName, String pTextureLocation) {
		return loadTexture(pName, pTextureLocation, GL11.GL_NEAREST);
	}

	public Texture loadTexture(String pName, String pTextureLocation, int pFilter) {
		return loadTexture(pName, pTextureLocation, pFilter, false);

	}

	public Texture loadTexture(String pName, String pTextureLocation, int pFilter, boolean pReload) {
		if (pTextureLocation == null || pTextureLocation.length() == 0) {
			return null;
		}

		Texture lTex = null;

		if (mTextures.containsKey(pName)) {
			lTex = mTextures.get(pName);

			if (!pReload)
				return lTex;

			unloadTexture(lTex);

		}

		// create new texture
		if (pTextureLocation.charAt(0) == '/') {
			lTex = Texture.loadTextureFromResource(pName, pTextureLocation, pFilter);

		} else {
			lTex = Texture.loadTextureFromFile(pName, pTextureLocation, pFilter);

		}

		if (lTex != null) {
			mTextures.put(pName, lTex); // cache

		}

		return lTex;
	}

	public Texture loadTexture(String pName, int[] pColorData, int pWidth, int pHeight) {
		return loadTexture(pName, pColorData, pWidth, pHeight, GL11.GL_NEAREST);
	}

	public Texture loadTexture(String pName, int[] pColorData, int pWidth, int pHeight, int pFilter) {
		Texture lResult = null;
		if (mTextures.containsKey(pName)) {
			lResult = mTextures.get(pName);
		}

		if (lResult != null) {
			lResult.reload(pColorData, pWidth, pHeight);

			return lResult;
		} else {
			Texture lTex = Texture.createTexture(pName, pName, pColorData, pWidth, pHeight, pFilter);
			if (lTex != null) {
				// Can't reload from rgb data
				lTex.reloadable(false);
				mTextures.put(pName, lTex); // cache

			}

			return lTex;

		}

	}

	public boolean saveTextureToFile(int pWidth, int pHeight, int[] pData, String pFileLocation) {
		BufferedImage lImage = new BufferedImage(pWidth, pHeight, BufferedImage.TYPE_INT_ARGB);

		// Convert our ABGR to output ARGB
		int[] lTextureData = new int[pWidth * pHeight];
		for (int i = 0; i < pWidth * pHeight; i++) {
			int r = (pData[i] & 0xff000000) >> 24;
			int g = (pData[i] & 0xff0000) >> 16;
			int b = (pData[i] & 0xff00) >> 8;
			int a = (pData[i] & 0xff);

			lTextureData[i] = a << 24 | r << 16 | g << 8 | b;
		}

		lImage.setRGB(0, 0, pWidth, pHeight, lTextureData, 0, pWidth);

		File outputfile = new File(pFileLocation);
		try {
			ImageIO.write(lImage, "png", outputfile);
		} catch (IOException e) {
			// e.printStackTrace();
			return false;
		}

		return true;

	}

	public Texture createFontTexture(String pName, BufferedImage pImage) {
		return createFontTexture(pName, pImage, GL11.GL_NEAREST);
	}

	public Texture createFontTexture(String pName, BufferedImage pImage, int pFilter) {
		if (mTextures.containsKey(pName)) {
			return mTextures.get(pName);
		}

		Texture lTex = Texture.createTexture(pName, pName, pImage, pFilter);
		lTex.reloadable(false);
		mTextures.put(pName, lTex);

		return lTex;
	}

	public void reloadTextures() {
		if (ConstantsTable.getBooleanValueDef("DEBUG_APP", false)) {
			DebugManager.DEBUG_MANAGER.logger().v(getClass().getSimpleName(), "Reloading all modified files");

		}

		for (Texture lTexture : mTextures.values()) {
			if (lTexture != null) {
				lTexture.reload();
			}

		}

	}

	/** Unloads the speicifed texture, if applicable. */
	public void unloadTexture(Texture pTexture) {
		if (pTexture == null)
			return; // already lost reference

		if (mTextures.containsValue(pTexture)) {

			String lTextureName = pTexture.name();
			Texture.unloadTexture(pTexture);

			mTextures.remove(lTextureName);

		}

		Texture.unloadTexture(pTexture);
		pTexture = null;

		return;

	}

}