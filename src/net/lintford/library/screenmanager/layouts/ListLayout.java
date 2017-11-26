package net.lintford.library.screenmanager.layouts;

import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.MenuScreen.ALIGNMENT;

/** The list layout lays out all the menu entries linearly down the layout.
 * 
 * @author Lintford Pickle */
public class ListLayout extends BaseLayout {

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public ListLayout(MenuScreen pParentScreen) {
		super(pParentScreen);

		// Set some defaults
		mOrientation = ORIENTATION.vertical;
		mAlignment = ALIGNMENT.center;
		mAnchor = ANCHOR.top;
	}

	public ListLayout(MenuScreen pParentScreen, float pX, float pY) {
		this(pParentScreen);

		x = pX;
		y = pY;
	}

	public ListLayout(MenuScreen pParentScreen, float pX, float pY, float pW, float pH) {
		this(pParentScreen, pX, pY);

		width = pW;
		height = pH;
	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void updateStructure() {
		if (mOrientation == ORIENTATION.vertical) {
			float lYPos = y + mYScrollPos;

			if (mAnchor == ANCHOR.bottom)
				lYPos = y + height;

			int lEntryCount = menuEntries().size();
			for (int i = 0; i < lEntryCount; i++) {
				MenuEntry lEntry = menuEntries().get(i);

				if (mAnchor == ANCHOR.top) {
					lYPos += lEntry.paddingVertical();

				} else {
					lYPos -= lEntry.paddingVertical();
					lYPos -= lEntry.getHeight();

				}

				switch (mAlignment) {
				case left:
					lEntry.x = x + lEntry.paddingHorizontal();
					break;
				case center:
					lEntry.x = x + width / 2 - lEntry.getWidth() / 2;
					break;
				case right:
					lEntry.x = x + width - lEntry.getWidth() - lEntry.paddingHorizontal();
					break;
				}

				lEntry.y = lYPos;

				if (mAnchor == ANCHOR.top) {
					lYPos += lEntry.getHeight();
					lYPos += lEntry.paddingVertical();

				} else {
					lYPos -= lEntry.paddingVertical();

				}

			}

		}

		final int lCount = mMenuEntries.size();
		for (int i = 0; i < lCount; i++) {
			mMenuEntries.get(i).updateStructure();
		}
	}

}
