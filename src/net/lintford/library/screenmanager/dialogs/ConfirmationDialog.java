package net.lintford.library.screenmanager.dialogs;

import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuEntry.BUTTON_SIZE;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.layouts.BaseLayout.ANCHOR;
import net.lintford.library.screenmanager.layouts.ListLayout;

public class ConfirmationDialog extends BaseDialog {

	// --------------------------------------==============
	// Constants
	// --------------------------------------==============

	public static final int BUTTON_CONFIRM_YES = 100;
	public static final int BUTTON_CONFIRM_NO = 101;

	// --------------------------------------==============
	// Variables
	// --------------------------------------==============

	private MenuEntry mConfirmEntry;
	private MenuEntry mCancelEntry;

	// --------------------------------------==============
	// Properties
	// --------------------------------------==============

	public MenuEntry confirmEntry() {
		return mConfirmEntry;
	}

	public MenuEntry cancelEntry() {
		return mCancelEntry;
	}

	// --------------------------------------==============
	// Constructors
	// --------------------------------------==============

	public ConfirmationDialog(ScreenManager pScreenManager, MenuScreen pParentScreen, String pDialogMessage) {
		super(pScreenManager, pParentScreen, pDialogMessage);

		ListLayout lListLayout = new ListLayout(this);
		lListLayout.anchor(ANCHOR.bottom);

		mConfirmEntry = new MenuEntry(pScreenManager, this, "Okay");
		mConfirmEntry.registerClickListener(pParentScreen, BUTTON_CONFIRM_YES);
		mConfirmEntry.buttonSize(BUTTON_SIZE.narrow);
		mCancelEntry = new MenuEntry(pScreenManager, this, "Cancel");
		mCancelEntry.registerClickListener(pParentScreen, BUTTON_CONFIRM_NO);
		mCancelEntry.buttonSize(BUTTON_SIZE.narrow);

		lListLayout.menuEntries().add(mCancelEntry);
		lListLayout.menuEntries().add(mConfirmEntry);

		layouts().add(lListLayout);

		// mEntryOffsetFromTop = 285f;
	}

	// --------------------------------------==============
	// Methods
	// --------------------------------------==============

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_CONFIRM_YES:

			break;

		case BUTTON_CONFIRM_NO:

			break;

		default:
			break;
		}
	}

}
