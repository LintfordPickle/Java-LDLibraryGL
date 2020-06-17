package net.lintford.library.renderers.windows.components;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.geometry.Rectangle;
import net.lintford.library.core.graphics.fonts.FontManager.FontUnit;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatch;
import net.lintford.library.core.input.IBufferedInputCallback;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.renderers.windows.UIWindow;

public class UIInputText extends UIWidget implements IBufferedInputCallback {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = 3637330515154931480L;

	private static final float SPACE_BETWEEN_TEXT = 1;
	private static final float CARET_FLASH_TIME = 250;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private transient boolean mHasFocus;
	private transient float mCaretFlashTimer;
	private transient boolean mShowCaret;
	private transient String mTempString;
	private transient String mEmptyString;

	private transient Rectangle mCancelRectangle;

	// A little wierd, we store the string length to check if the string has changed since the last frame (since
	// working with the length (int) doesn't cause a heap allocation as toString() does )
	private transient int mStringLength;
	private transient StringBuilder mInputField;
	private transient boolean mResetOnDefaultClick;
	private boolean mMouseClickBreaksInputTextFocus;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public boolean mouseClickBreaksInputTextFocus() {
		return mMouseClickBreaksInputTextFocus;
	}

	public void mouseClickBreaksInputTextFocus(boolean pNewValue) {
		mMouseClickBreaksInputTextFocus = pNewValue;
	}

	public String emptyString() {
		return mEmptyString;
	}

	public void emptyString(String pNewString) {
		if (pNewString == null)
			mEmptyString = "";
		else
			mEmptyString = pNewString;
	}

	public int stringLength() {
		return mStringLength;
	}

	public boolean isEmpty() {
		return isEmptyString();
	}

	public boolean hasFocus() {
		return mHasFocus;
	}

	public void hasFocus(boolean v) {
		mHasFocus = v;
	}

	public void inputString(String pNewValue) {
		if (mInputField.length() > 0) {
			mInputField.delete(0, mInputField.length());
		}
		mInputField.append(pNewValue);
	}

	public StringBuilder inputString() {
		return mInputField;
	}

	public boolean isEmptyString() {
		return mInputField.toString().equals(mEmptyString);
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public UIInputText(UIWindow pParentWindow) {
		super(pParentWindow);

		mResetOnDefaultClick = true;
		mInputField = new StringBuilder();

		mCancelRectangle = new Rectangle();
		mEmptyString = "";

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	public boolean handleInput(LintfordCore pCore) {
		if (mCancelRectangle.intersectsAA(pCore.HUD().getMouseCameraSpace())) {
			if (pCore.input().mouse().tryAcquireMouseLeftClickTimed(hashCode(), this)) {
				if (mInputField.length() > 0) {

					if (mInputField.length() > 0)
						mInputField.delete(0, mInputField.length());
					mStringLength = 0;

					pCore.input().keyboard().stopCapture();

					mHasFocus = false;
					mShowCaret = false;

				}

			}
		}

		if (intersectsAA(pCore.HUD().getMouseCameraSpace())) {
			if (pCore.input().mouse().tryAcquireMouseLeftClickTimed(hashCode(), this)) {

				onClick(pCore.input());
				mHasFocus = true;

				return true;

			}

		}

		// Stop the keyboard capture if the player clicks somewhere else within the game
		if (mHasFocus && mMouseClickBreaksInputTextFocus && (pCore.input().mouse().isMouseLeftButtonDownTimed(this) || pCore.input().mouse().isMouseRightButtonDownTimed(this))) {
			pCore.input().keyboard().stopCapture();

			mHasFocus = false;
			mShowCaret = false;

		}

		return false;

	}

	public void update(LintfordCore pCore) {
		super.update(pCore);

		mCaretFlashTimer += pCore.appTime().elapsedTimeMilli();

		final int lHorizontalPadding = 5;
		final int lCancelRectSize = 24;
		mCancelRectangle.set(x + w - lCancelRectSize - lHorizontalPadding, y + h / 2 - lCancelRectSize / 2, lCancelRectSize, lCancelRectSize);

		if (mHasFocus) {
			// flash and update the location of the caret
			if (mCaretFlashTimer > CARET_FLASH_TIME) {
				mShowCaret = !mShowCaret;
				mCaretFlashTimer = 0;
			}

			// Limit the number of characters which can be entered
			if (mInputField.length() > 15)
				mInputField.delete(15, mInputField.length() - 1);

		}

	}

	@Override
	public void draw(LintfordCore pCore, TextureBatch pTextureBatch, Texture pUITexture, FontUnit pTextFont, float pComponentZDepth) {
		// Renders the background of the input text widget
		pTextureBatch.begin(pCore.HUD());
		pTextureBatch.draw(pUITexture, 0, 288, 32, 32, x, y, 32, h, pComponentZDepth, 1f, 1f, 1f, 1);
		if (w > 32) {
			pTextureBatch.draw(pUITexture, 64, 288, 32, 32, x + 32, y, w - 64, h, pComponentZDepth, 1f, 1f, 1f, 1);
			pTextureBatch.draw(pUITexture, 128, 288, 32, 32, x + w - 32, y, 32, h, pComponentZDepth, 1f, 1f, 1f, 1);
		}

		pTextureBatch.end();

		// Draw the cancel button rectangle
		pTextureBatch.begin(pCore.HUD());
		pTextureBatch.draw(pUITexture, 32, 128, 32, 32, mCancelRectangle, pComponentZDepth, 1f, 1f, 1f, 1);
		pTextureBatch.end();

		final float lInputTextWidth = pTextFont.bitmap().getStringWidth(mInputField.toString());

		String lText = mInputField.toString();
		final float lTextHeight = pTextFont.bitmap().fontHeight();
		float lAlpha = 1f;
		if (lText.length() == 0 && !mHasFocus) {
			if (mEmptyString.isEmpty()) {
				lText = "<search>";

			} else {
				lText = mEmptyString;

			}

			lAlpha = 0.5f;
		}

		pTextFont.begin(pCore.HUD());
		pTextFont.draw(lText, x + 10, y + h / 2 - lTextHeight / 2, pComponentZDepth, 1f, 1f, 1f, lAlpha, 1f, -1);

		if (mShowCaret && mHasFocus) {
			pTextFont.draw("|", x + 7 + lInputTextWidth + SPACE_BETWEEN_TEXT * 3, y + h / 2 - lTextHeight / 2, pComponentZDepth, 1f);

		}

		pTextFont.end();

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void onClick(InputManager pInputState) {
		mHasFocus = !mHasFocus;

		if (mHasFocus) {
			pInputState.keyboard().startCapture(this);

			// Store the current string in case the user cancels the input, in which case, we
			// can restore the previous entry.
			if (mInputField.length() > 0)
				mTempString = mInputField.toString();

			if (mResetOnDefaultClick && mInputField.toString().equals(mEmptyString)) {
				if (mInputField.length() > 0) {
					mInputField.delete(0, mInputField.length());
				}

			}

		}

	}

	@Override
	public StringBuilder getStringBuilder() {
		return mInputField;
	}

	@Override
	public boolean onEnterPressed() {
		mHasFocus = false;
		mShowCaret = false;

		return getEnterFinishesInput();

	}

	@Override
	public void onKeyPressed(char pCh) {
		mStringLength = mInputField.length();

	}

	@Override
	public boolean getEnterFinishesInput() {
		return true;
	}

	@Override
	public boolean onEscapePressed() {
		if (mInputField.length() > 0) {
			mInputField.delete(0, mInputField.length());
		}
		if (mTempString != null && mTempString.length() == 0) {
			mInputField.append(mTempString);
		}

		mStringLength = 0;

		mHasFocus = false;
		mShowCaret = false;

		return getEscapeFinishesInput();

	}

	@Override
	public boolean getEscapeFinishesInput() {
		return true;
	}

}
