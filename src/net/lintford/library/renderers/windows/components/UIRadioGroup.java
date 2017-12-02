package net.lintford.library.renderers.windows.components;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.renderers.windows.UIWindow;
import net.lintford.library.screenmanager.entries.IMenuEntryClickListener;

public class UIRadioGroup extends UIWidget implements IMenuEntryClickListener {

	// --------------------------------------
	// Variables
	// --------------------------------------

	private List<UIRadioButton> mButtons;
	private IMenuEntryClickListener mCallback;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public UIRadioGroup(final UIWindow pUIWindow) {
		super(pUIWindow);

		mButtons = new ArrayList<>();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (intersects(pCore.HUD().getMouseCameraSpace())) {
			final int lButtonCount = mButtons.size();
			for (int i = 0; i < lButtonCount; i++) {
				if (mButtons.get(i).handleInput(pCore)) {

					return true;
				}

			}

		}

		return false;
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		updateLayout();

		float lYPos = y + mParentWindow.getTitleBarHeight() + UIWindow.WINDOW_CONTENT_PADDING_Y;

		final int lButtonCount = mButtons.size();
		for (int i = 0; i < lButtonCount; i++) {
			mButtons.get(i).x = x;
			mButtons.get(i).y = lYPos;
			mButtons.get(i).width = 50;

			lYPos += 35;

			mButtons.get(i).update(pCore);

		}

	}

	@Override
	public void draw(LintfordCore pCore) {

		final int lButtonCount = mButtons.size();
		for (int i = 0; i < lButtonCount; i++) {
			mButtons.get(i).draw(pCore);

		}

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void addButton(final UIRadioButton pButton) {
		if (!mButtons.contains(pButton)) {
			pButton.setClickListener(this);

			mButtons.add(pButton);

		}

	}

	public void removeButton(final UIRadioButton pButton) {
		if (mButtons.contains(pButton)) {
			pButton.removeClickListener(this);

			mButtons.remove(pButton);

		}

	}

	public void updateLayout() {

	}

	@Override
	public void onClick(final int pEntryID) {
		final int lButtonCount = mButtons.size();
		for (int i = 0; i < lButtonCount; i++) {
			if (mButtons.get(i).buttonListenerID() == pEntryID) {
				// Was clicked
				mButtons.get(i).isSelected(true);

			}

			else {
				// was not clicked
				mButtons.get(i).isSelected(false);

			}

		}

		if (mCallback != null) {
			mCallback.onClick(pEntryID);

		}

	}

	public void setClickListener(final IMenuEntryClickListener pCallbackObject) {
		mCallback = pCallbackObject;
	}

	public void removeClickListener(final IMenuEntryClickListener pCallbackObject) {
		mCallback = null;
	}

}