
package net.lintford.library.renderers.windows;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.camera.ICamera;
import net.lintford.library.core.graphics.ResourceManager;
import net.lintford.library.core.graphics.fonts.FontManager.FontUnit;
import net.lintford.library.core.graphics.textures.TextureManager;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatch;
import net.lintford.library.core.maths.Rectangle;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;
import net.lintford.library.renderers.windows.components.IScrollBarArea;
import net.lintford.library.renderers.windows.components.ScrollBar;
import net.lintford.library.renderers.windows.components.ScrollBarContentRectangle;
import net.lintford.library.renderers.windows.components.UIWidget;

public class UIWindow extends BaseRenderer implements IScrollBarArea, UIWindowChangeListener {

	// --------------------------------------
	// Constants
	// --------------------------------------

	protected static final float Z_DEPTH = -1.0f;

	// The default sie of the title bar for a window
	protected static final float DEFAULT_TITLEBAR_HEIGHT = 32;

	// The padding between the windows and the edge of the UI bounds.
	protected static final float SCREEN_PADDING = 100;

	// The padding between windows in the UI
	protected static final float WINDOW_PADDING = 10;

	// The padding between the window and the window content (displayed in the window)
	public static final float WINDOW_CONTENT_PADDING_X = 10;
	public static final float WINDOW_CONTENT_PADDING_Y = 10;

	// --------------------------------------
	// Variables
	// --------------------------------------

	protected List<UIWidget> mComponents;

	protected String mWindowTitle;
	protected boolean mIsOpen;
	protected ScrollBar mScrollBar;

	protected boolean mMouseDownLastUpdate;

	// This is the area within which any scrollable content will be displayed. Scrollbars are only visible if the
	// height of the mContentDisplayArea is smaller than the height of the mContentRectangle (below).
	protected UIRectangle mContentDisplayArea;

	// This is the area that the content would take up, if not limited by the window bounds (i.e. the area of the 'content' visualisation).
	protected ScrollBarContentRectangle mContentRectangle;

	protected boolean mUIInputFromUIManager;
	protected boolean mIsWindowMoveable;
	protected boolean mIsWindowMoving;
	protected float dx, dy;
	protected float mWindowAlpha;
	protected float mYScrollVal;

	// Window icons are loaded from the UI_TEXTURE_NAME. If this is null, no icon
	// is displayed
	protected UIRectangle mIconSrcRectangle;

	/** Stores the window area of this renderer window */
	protected UIRectangle mWindowArea;

	/** If true, this base renderer consumes input and ends the handleInput invocation chain. */
	protected boolean mExclusiveHandleInput;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public boolean exclusiveHandleInput() {
		return mExclusiveHandleInput;
	}

	/** @return true if this window is open, false if closed or minimised. */
	public boolean isOpen() {
		return mIsOpen;
	}

	public void isOpen(boolean pNewValue) {
		mIsOpen = pNewValue;
	}

	public String windowTitle() {
		return mWindowTitle;
	}

	public void windowTitle(String pNewTitle) {
		mWindowTitle = pNewTitle;
	}

	public float getTitleBarHeight() {
		return DEFAULT_TITLEBAR_HEIGHT * mRendererManager.getUIScale();
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public UIWindow(final RendererManager pRendererManager, final String pRendererName, final int pGroupID) {
		super(pRendererManager, pRendererName, pGroupID);

		mComponents = new ArrayList<>();

		mWindowArea = new UIRectangle();
		mIconSrcRectangle = new UIRectangle();
		mContentDisplayArea = new UIRectangle();

		// Set some sane defaults
		mWindowArea.x = 10;
		mWindowArea.y = 10;
		mWindowArea.width = 320;
		mWindowArea.height = 240;

		mWindowAlpha = 1.0f;

		mContentRectangle = new ScrollBarContentRectangle(this);
		mScrollBar = new ScrollBar(this, mContentRectangle);

		mContentRectangle.x = mWindowArea.x;
		mContentRectangle.y = mWindowArea.y + DEFAULT_TITLEBAR_HEIGHT;
		mContentRectangle.width = 0;
		mContentRectangle.height = 0;

		// sane default
		mWindowTitle = "<unnamed>";

		mIsWindowMoveable = false;
		mUIInputFromUIManager = true; // UIManager will call HandleInput (as oppose to some other controller)
	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	public void loadGLContent(ResourceManager pResourceManager) {
		mContentDisplayArea.y = mWindowArea.y + getTitleBarHeight();
		mContentDisplayArea.height = mWindowArea.height - +getTitleBarHeight();

		mIsLoaded = true;

	}

	public void unloadGLContent() {

	}

	public boolean handleInput(LintfordCore pCore) {
		if (!isOpen())
			return false;

		final float lMouseScreenSpaceX = pCore.HUD().getMouseWorldSpaceX();
		final float lMouseScreenSpaceY = pCore.HUD().getMouseWorldSpaceY();

		// First check if the scroll bar has been used
		if (mScrollBar.handleInput(pCore)) {
			return true;
		}

		// Update the window components
		final int lComponentCount = mComponents.size();
		for (int i = 0; i < lComponentCount; i++) {
			if (mComponents.get(i).handleInput(pCore)) {
				return true;

			}

		}

		if (mIsWindowMoving) {
			// check if user has stopped dragging the window (worst case we skip this frame)
			if (!pCore.input().mouseLeftClick()) {
				mIsWindowMoving = false;
				mMouseDownLastUpdate = false;
				return false;
			}

			// FIXME: Check history
			mWindowArea.x += (pCore.input().mouseWindowCoords().x - dx);
			mWindowArea.y += (pCore.input().mouseWindowCoords().y - dy);

			// update the delta
			dx = pCore.input().mouseWindowCoords().x;
			dy = pCore.input().mouseWindowCoords().y;

			return true;

		}

		// one problem ..

		// 2. window captures mouse clicks even if not dragging

		if (mIsWindowMoveable && !mIsWindowMoving && mWindowArea.intersects(pCore.HUD().getMouseCameraSpace())) {

			// Only acquire lock when we are ready to move ...
			if (pCore.input().tryAquireLeftClickOwnership(hashCode())) {
				if (!mMouseDownLastUpdate) {
					mMouseDownLastUpdate = true;
					dx = pCore.input().mouseWindowCoords().x;
					dy = pCore.input().mouseWindowCoords().y;

				}

				float nx = pCore.input().mouseWindowCoords().x;
				float ny = pCore.input().mouseWindowCoords().y;

				final int MINIMUM_TOLERENCE = 3;

				if (Math.abs(nx - dx) > MINIMUM_TOLERENCE || Math.abs(ny - dy) > MINIMUM_TOLERENCE) {
					// Now we can try to acquire the lock, and if we get it, start dragging the window
					if (pCore.input().tryAquireLeftClickOwnership(hashCode())) {
						mIsWindowMoving = true;

					}

				}

			}

		}

		if (!pCore.input().mouseLeftClick()) {
			mIsWindowMoving = false;
			mMouseDownLastUpdate = false;
		}

		// If the mouse was clicked within the window, then we need to process the click anyway
		if (mWindowArea.intersects(lMouseScreenSpaceX, lMouseScreenSpaceY)) {
			return pCore.input().tryAquireLeftClickOwnership(hashCode());

		}

		return false;

	}

	public void update(LintfordCore pCore) {
		if (!isOpen())
			return;

		mScrollBar.update(pCore);

		// Update the window components
		final int lComponentCount = mComponents.size();
		for (int i = 0; i < lComponentCount; i++) {
			mComponents.get(i).update(pCore);
		}

	}

	@Override
	public void draw(LintfordCore pCore) {
		if (!isOpen())
			return;

		updateWindowPosition(pCore.HUD());

		mWindowAlpha = 0.95f;

		final TextureBatch SPRITE_BATCH_UI = mRendererManager.uiSpriteBatch();

		// Draw the window background
		SPRITE_BATCH_UI.begin(pCore.HUD());
		SPRITE_BATCH_UI.draw(96, 0, 32, 32, mWindowArea.x, mWindowArea.y + getTitleBarHeight() + 5, Z_DEPTH, mWindowArea.width, mWindowArea.height - getTitleBarHeight() - 5, 1f, 1f, 1f, 0.7f, 0f, 0f, 0f, 1f, 1f, TextureManager.TEXTURE_CORE_UI);
		SPRITE_BATCH_UI.end();

		// Draw the title bar
		SPRITE_BATCH_UI.begin(pCore.HUD());
		SPRITE_BATCH_UI.draw(32, 0, 32, 32, mWindowArea.x, mWindowArea.y, Z_DEPTH, mWindowArea.width, getTitleBarHeight(), 1f, 1f, 1f, mWindowAlpha, 0f, 0f, 0f, 1f, 1f, TextureManager.TEXTURE_CORE_UI);

		float lTitleX = mWindowArea.x + WINDOW_CONTENT_PADDING_X;
		float lTitleY = mWindowArea.y;

		// Render the icons from the game ui texture
		if (mIconSrcRectangle != null && !mIconSrcRectangle.isEmpty()) {
			SPRITE_BATCH_UI.draw(mIconSrcRectangle.x, mIconSrcRectangle.y, mIconSrcRectangle.width, mIconSrcRectangle.height, lTitleX, lTitleY, Z_DEPTH, getTitleBarHeight(), getTitleBarHeight(), 1f, 1f, 1f, mWindowAlpha, 0f, 0f, 0f, 1f, 1f,
					TextureManager.TEXTURE_CORE_UI);

			lTitleX += 32 + WINDOW_CONTENT_PADDING_X;

		}

		SPRITE_BATCH_UI.end();

		// Draw the window title
		FontUnit lTitleFontUnit = mRendererManager.titleFont();
		lTitleFontUnit.begin(pCore.HUD());
		lTitleFontUnit.draw(mWindowTitle, lTitleX, lTitleY + 2, Z_DEPTH, 1f, 1f, 1f, 1f, 1f, -1);
		lTitleFontUnit.end();

		if (mContentRectangle.height - windowArea().height > 0) {
			mScrollBar.draw(pCore, SPRITE_BATCH_UI, Z_DEPTH);

		}

		// Draw the window components
		final int lComponentCount = mComponents.size();
		for (int i = 0; i < lComponentCount; i++) {
			mComponents.get(i).draw(pCore);

		}

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void updateWindowPosition(ICamera pHUD) {
		final Rectangle HUD_BOUNDING_RECT = pHUD.boundingRectangle();

		if (HUD_BOUNDING_RECT == null) {
			System.err.println("Cannot update window position - RendererManager.UIManager.HUDCamera not set to an instance of an HUD object!");
			return;
		}

		mWindowArea.x = HUD_BOUNDING_RECT.left() + SCREEN_PADDING;
		mWindowArea.y = HUD_BOUNDING_RECT.top() + 50;
		mWindowArea.width = HUD_BOUNDING_RECT.width * 0.5f - WINDOW_PADDING - SCREEN_PADDING;
		mWindowArea.height = HUD_BOUNDING_RECT.height / 2 - WINDOW_PADDING - 50;

	}

	public void keepWindowOnScreen(ICamera pHUD) {

	}

	// --------------------------------------
	// IScrolBarArea Inherited Methods
	// --------------------------------------

	@Override
	public float currentYPos() {
		return mYScrollVal;
	}

	@Override
	public void RelCurrentYPos(float pAmt) {
		mYScrollVal += pAmt;

	}

	@Override
	public void AbsCurrentYPos(float pValue) {
		mYScrollVal = pValue;

	}

	@Override
	public UIRectangle windowArea() {
		return mWindowArea;
	}

	@Override
	public ScrollBarContentRectangle contentArea() {
		return mContentRectangle;
	}

	public void addComponent(UIWidget pComponent) {
		if (!mComponents.contains(pComponent)) {
			mComponents.add(pComponent);

		}

	}

	public void removeComponent(UIWidget pComponent) {
		if (mComponents.contains(pComponent)) {
			mComponents.remove(pComponent);

		}

	}

	public final void closeWindow() {
		mIsOpen = false;

	}

	@Override
	public void onWindowClosed(UIWindow pUIWindow) {
		// TODO Auto-generated method stub

	}

}