package net.lintford.library.screenmanager.entries;

import net.lintford.library.ConstantsApp;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.Screen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.ScreenManagerConstants.FILLTYPE;
import net.lintford.library.screenmanager.layouts.BaseLayout;

public class MenuLabelEntry extends MenuEntry {

	private static final long serialVersionUID = -6246272207476797676L;

	// --------------------------------------
	// Variables
	// --------------------------------------

	protected float mPadding = 15f;
	protected boolean mTrimText;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public boolean trimText() {
		return mTrimText;
	}

	public void trimText(boolean pNewValue) {
		mTrimText = pNewValue;
	}

	/** Padding is applied when the label is either aligned left or right (not when centered). */
	public float padding() {
		return mPadding;
	}

	/** Padding is applied when the label is either aligned left or right (not when centered). */
	public void padding(float pNewValue) {
		mPadding = pNewValue;
	}

	@Override
	public boolean hasFocus() {
		return super.hasFocus();
	}

	@Override
	public void hasFocus(boolean pNewValue) {
		if (pNewValue) {

		}

		super.hasFocus(pNewValue);
	}

	public void label(String pNewLabel) {
		mText = pNewLabel;
	}

	public String label() {
		return mText;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public MenuLabelEntry(ScreenManager pScreenManager, BaseLayout pParentLayout) {
		super(pScreenManager, pParentLayout, "");

		mDrawBackground = false;
		mText = "Unnamed Label";

		entryColor.r = .34f;
		entryColor.g = .34f;
		entryColor.b = .34f;

		mCanHaveFocus = false;
		mCanHoverOver = false;

		mLeftMargin = mRightMargin = 0.f;
		mLeftPadding = 10.f;

		mVerticalFillType = FILLTYPE.TAKE_WHATS_NEEDED;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void updateStructure() {
		super.updateStructure();

		// TODO: This -50 is because of the scrollbar - this is why I needed to keep the padding :(
		w = Math.min(mParentLayout.w() - 50f, mMaxWidth);

		final var lParentScreen = mParentLayout.parentScreen;
		final var lFont = lParentScreen.font();
		if (lFont == null)
			return;

		final float lUiTextScale = lParentScreen.uiTextScale();

		final float lFontHeight = lFont.bitmap().fontHeight() * lUiTextScale;
		h = lFontHeight * lUiTextScale;

	}

	@Override
	public void draw(LintfordCore pCore, Screen pScreen, boolean pIsSelected, float pParentZDepth) {
		if (!enabled())
			return;

		final var lParentScreen = mParentLayout.parentScreen;
		final var lFont = lParentScreen.font();

		if (lFont == null) {
			return;

		}

		entryColor.a = lParentScreen.a();
		textColor.a = lParentScreen.a();

		final float lUiTextScale = lParentScreen.uiTextScale();

		final float lLabelWidth = lFont.bitmap().getStringWidth(mText, lUiTextScale);
		final float lFontHeight = lFont.bitmap().fontHeight() * lUiTextScale;

		final var lTextureBatch = lParentScreen.textureBatch();

		if (mDrawBackground) {
			lTextureBatch.begin(pCore.HUD());
			lTextureBatch.draw(mUITexture, 0, 0, 32, 32, x, y, w, h, pParentZDepth + .15f, entryColor.r, entryColor.g, entryColor.b, entryColor.a);
			lTextureBatch.end();

		}

		float lX = x + w / 2 - lLabelWidth / 2; // Center label
		switch (mHorizontalAlignment) {
		case LEFT:
			lX = x;
			break;
		case RIGHT:
			lX = x - mPadding - lLabelWidth;
			break;
		default:
			lX = x + w / 2 - lLabelWidth / 2; // Center label
			break;
		}

		lFont.begin(pCore.HUD());
		lFont.drawShadow(mDrawTextShadow);
		lFont.trimText(mTrimText);
		lFont.draw(mText, lX + 15.f, y + h / 2f - lFontHeight / 2f, pParentZDepth + .15f, textColor.r, textColor.g, textColor.b, textColor.a, lUiTextScale);
		lFont.end();

		if (mShowInfoIcon) {
			drawInfoIcon(pCore, lTextureBatch, mInfoIconDstRectangle, entryColor.a);

		}

		if (mShowWarnIcon) {
			drawWarningIcon(pCore, lTextureBatch, mWarnIconDstRectangle, entryColor.a);

		}

		if (ConstantsApp.getBooleanValueDef("DEBUG_SHOW_UI_COLLIDABLES", false)) {
			lTextureBatch.begin(pCore.HUD());
			lTextureBatch.draw(mUITexture, 0, 0, 32, 32, x, y, w, h, mZ, 1f, 0.2f, 0.2f, entryColor.a);
			lTextureBatch.end();

		}

	}

}
