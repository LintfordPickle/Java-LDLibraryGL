package net.lintford.library.screenmanager.entries;

import net.lintford.library.core.camera.ICamera;
import net.lintford.library.core.graphics.fonts.BitmapFont;
import net.lintford.library.core.input.IBufferedInputCallback;
import net.lintford.library.core.input.InputState;
import net.lintford.library.core.rendering.RenderState;
import net.lintford.library.core.time.GameTime;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.Screen;
import net.lintford.library.screenmanager.ScreenManager;

public class MenuInputEntry extends MenuEntry implements IBufferedInputCallback {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final float SPACE_BETWEEN_TEXT = 15;
	private static final float CARET_FLASH_TIME = 250; // ms

	// --------------------------------------
	// Variables
	// --------------------------------------

	private String mLabel;
	private String mDefaultText;
	private final String mSeparator = " : ";
	private float mCaretFlashTimer;
	private boolean mShowCaret;
	private String mTempString;

	private StringBuilder mInputField;
	private boolean mResetOnDefaultClick;

	// --------------------------------------
	// Properties
	// --------------------------------------

	@Override
	public boolean hasFocus() {
		return super.hasFocus();
	}

	public void label(String pNewLabel) {
		mLabel = pNewLabel;
	}

	public String label() {
		return mLabel;
	}

	public void inputString(String pNewValue) {
		if (mInputField.length() > 0) {
			mInputField.delete(0, mInputField.length());
		}
		mInputField.append(pNewValue);
	}

	public String inputString() {
		return mInputField.toString();
	}

	public void setDefaultText(String pText, boolean pResetOnClick) {
		mDefaultText = pText;
		mResetOnDefaultClick = pResetOnClick;
		if (mInputField.length() > 0) {
			mInputField.delete(0, mInputField.length());
		}

		mInputField.append(mDefaultText);
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public MenuInputEntry(ScreenManager pScreenManager, MenuScreen pParentScreen) {
		super(pScreenManager, pParentScreen, "");

		mLabel = "Label:";
		mResetOnDefaultClick = true;

		mDrawBackground = false;
		mHighlightOnHover = false;

		mInputField = new StringBuilder();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public boolean handleInput(InputState pInputState, ICamera pHUDCamera) {
		boolean lResult = super.handleInput(pInputState, pHUDCamera);

		/*
		 * The code for handling the input for this InputMenuEntry is handled for us in the super class. The only addition to that code is the need to capture the keyboard buffered input, which is below.
		 */
		if (mHasFocus) {
			// Update the string displayed with the input received
			pInputState.startCapture(this);
		} else {
			mFocusLocked = false; // no lock if not focused
			mHasFocus = false;
		}

		return lResult;
	}

	@Override
	public void update(GameTime pGameTime, MenuScreen pScreen, boolean pIsSelected) {
		super.update(pGameTime, pScreen, pIsSelected);

		final double lDeltaTime = pGameTime.elapseGameTimeMilli();

		mCaretFlashTimer += lDeltaTime;

		if (mFocusLocked) {
			// flash and update the location of the carot
			if (mCaretFlashTimer > CARET_FLASH_TIME) {
				mShowCaret = !mShowCaret;
				mCaretFlashTimer = 0;
			}
		}
	}

	@Override
	public void draw(Screen pScreen, RenderState pRenderState, boolean pIsSelected, float pParentZDepth) {
		super.draw(pScreen, pRenderState, pIsSelected, pParentZDepth);

		BitmapFont lFontBitmap = mParentScreen.font().bitmap();

		final float lLabelWidth = lFontBitmap.getStringWidth(mLabel);
		final float lInputTextWidth = lFontBitmap.getStringWidth(mInputField.toString());
		final float lFontHeight = lFontBitmap.fontHeight();
		final float lSeparatorHalfWidth = lFontBitmap.getStringWidth(mSeparator) * 0.5f;

		mParentScreen.font().begin(mScreenManager.HUD());
		mParentScreen.font().draw(mLabel, x + width / 2 - 10 - lLabelWidth - lSeparatorHalfWidth, y + height / 2 - lFontHeight * 0.5f, pParentZDepth + .1f, 1f);
		mParentScreen.font().draw(mSeparator, x + width / 2 - lSeparatorHalfWidth, y + height / 2 - lFontHeight * 0.5f, pParentZDepth + .1f, 1f);
		mParentScreen.font().draw(mInputField.toString(), x + width / 2 + lSeparatorHalfWidth + SPACE_BETWEEN_TEXT, y + height / 2 - lFontHeight * 0.5f, pParentZDepth + .1f, 1f);

		if (mShowCaret && mFocusLocked) {
			mParentScreen.font().draw("|", x + width / 2 + lSeparatorHalfWidth + SPACE_BETWEEN_TEXT + lInputTextWidth, y + height / 2 - lFontHeight * 0.5f, pParentZDepth + .1f, 1.0f);
		}

		mParentScreen.font().end();

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	public void onClick(InputState pInputState) {
		super.onClick(pInputState);

		mHasFocus = !mHasFocus;
		System.out.println("focus : " + mHasFocus);
		if (mHasFocus) {
			mFocusLocked = true;

			// Store the current string in case the user cancels the input, in which case, we
			// can restore the previous entry.
			if (mInputField.length() > 0)
				mTempString = mInputField.toString();

			if (mResetOnDefaultClick && mInputField.toString().equals(mDefaultText)) {
				if (mInputField.length() > 0) {
					mInputField.delete(0, mInputField.length());
				}
			}

		} else {
			mFocusLocked = false; // no lock if not focused
		}
	}

	@Override
	public StringBuilder getStringBuilder() {
		return mInputField;
	}

	@Override
	public void onEnterPressed() {
		mHasFocus = false;
		mShowCaret = false;

		if (mInputField.length() == 0) {
			setDefaultText("Empty", true);
		}
	}

	@Override
	public boolean getEnterFinishesInput() {
		return true;
	}

	@Override
	public void onEscapePressed() {
		if (mInputField.length() > 0) {
			mInputField.delete(0, mInputField.length());
		}
		if (mTempString != null && mTempString.length() == 0) {
			mInputField.append(mTempString);
		}

		mHasFocus = false;
		mShowCaret = false;
	}

	@Override
	public boolean getEscapeFinishesInput() {
		return true;
	}

	@Override
	public void onKeyPressed(char pCh) {

	}

}
