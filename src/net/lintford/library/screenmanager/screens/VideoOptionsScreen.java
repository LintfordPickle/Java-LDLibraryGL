package net.lintford.library.screenmanager.screens;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import net.lintford.library.options.DisplayManager;
import net.lintford.library.options.VideoSettings;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuEntry.BUTTON_SIZE;
import net.lintford.library.screenmanager.MenuEntry.ENTRY_ALIGNMENT;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.dialogs.ConfirmationDialog;
import net.lintford.library.screenmanager.dialogs.TimedConfirmationDialog;
import net.lintford.library.screenmanager.dialogs.TimedDialogInterface;
import net.lintford.library.screenmanager.entries.EntryInteractions;
import net.lintford.library.screenmanager.entries.HorizontalEntryGroup;
import net.lintford.library.screenmanager.entries.MenuDropDownEntry;
import net.lintford.library.screenmanager.entries.MenuEnumEntryIndexed;
import net.lintford.library.screenmanager.entries.MenuLabelEntry;
import net.lintford.library.screenmanager.entries.MenuToggleEntry;
import net.lintford.library.screenmanager.layouts.BaseLayout;
import net.lintford.library.screenmanager.layouts.ListLayout;

// TODO: Monitor and Aspect Ratio are only considered in fullscreen mode
// TODO: Need to add a 15 second cooldown when applying settings for the first time
public class VideoOptionsScreen extends MenuScreen implements EntryInteractions, TimedDialogInterface {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final int BUTTON_APPLY_CHANGES = 0;
	public static final int BUTTON_CANCEL_CHANGES = 1;

	public static final int BUTTON_MONITOR = 10;
	public static final int BUTTON_RESOLUTION = 11;
	public static final int BUTTON_VSYNC = 12;
	public static final int BUTTON_FULLSCREEN = 13;
	public static final int BUTTON_ASPECTRATIO = 14;

	private static final int CONFIRMATION_TIMER_MILLI = 15000; // ms

	private static final String FULLSCREEN_YES = "Yes";
	private static final String FULLSCREEN_NO = "No";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private DisplayManager mDisplayManager;

	private ConfirmationDialog mConfirmationDialog;
	private TimedConfirmationDialog m15SecConfirmationDialog;

	private MenuEntry mApplyButton;
	private MenuToggleEntry mVSync;

	private MenuEnumEntryIndexed<Integer> mFullScreenEntry;
	private MenuEnumEntryIndexed<Long> mMonitorEntry;
	private MenuDropDownEntry<GLFWVidMode> mResolutionEntry;

	// Quality settings
	private MenuLabelEntry mChangesPendingWarning;

	private VideoSettings modifiedVideoConfig;
	private VideoSettings currentVideoConfig;
	private VideoSettings lastVideoConfig; // last known working config, in case we need to revert

	private ListLayout mConfirmChangesLayout;
	private ListLayout mVideoList;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public VideoOptionsScreen(ScreenManager pScreenManager) {
		super(pScreenManager, "VIDEO SETTINGS");

		mPaddingTop = 0;

		// Get the config options
		mDisplayManager = pScreenManager.core().config().display();

		mVideoList = new ListLayout(this);
		mVideoList.setDrawBackground(true, 1f, 1f, 1f, 0.85f);

		mConfirmChangesLayout = new ListLayout(this);

		createVideoSection(mVideoList);

		currentVideoConfig = mDisplayManager.currentOptionsConfig();
		lastVideoConfig = new VideoSettings(currentVideoConfig);
		modifiedVideoConfig = new VideoSettings(currentVideoConfig);

		setUIFromVideoSettings(currentVideoConfig);

		mChangesPendingWarning = new MenuLabelEntry(mScreenManager, mConfirmChangesLayout);
		mChangesPendingWarning.label("Current changes have not yet been applied!");
		mChangesPendingWarning.labelColor(0.86f, 0.22f, 0.37f);
		mChangesPendingWarning.enabled(true);

		mConfirmChangesLayout.menuEntries().add(mChangesPendingWarning);
		mConfirmChangesLayout.setDrawBackground(true, 1f, 1f, 1f, 0.85f);

		/* Screen control buttons */
		HorizontalEntryGroup lGroup = new HorizontalEntryGroup(pScreenManager, footerLayout());

		MenuEntry lBackButton = new MenuEntry(pScreenManager, footerLayout(), "Back");
		lBackButton.buttonSize(BUTTON_SIZE.narrow);
		lBackButton.registerClickListener(this, BUTTON_CANCEL_CHANGES);
		mApplyButton = new MenuEntry(pScreenManager, footerLayout(), "Apply");
		mApplyButton.buttonSize(BUTTON_SIZE.narrow);
		mApplyButton.registerClickListener(this, BUTTON_APPLY_CHANGES);
		mApplyButton.enabled(false);

		lGroup.addEntry(lBackButton);
		lGroup.addEntry(mApplyButton);

		footerLayout().menuEntries().add(lGroup);

		// Add the layouts to the screen
		layouts().add(mVideoList);
		layouts().add(mConfirmChangesLayout);

		mConfirmChangesLayout.visible(false);

	}

	private void createVideoSection(BaseLayout lLayout) {
		MenuLabelEntry lVideoOptionsTitle = new MenuLabelEntry(mScreenManager, lLayout);
		lVideoOptionsTitle.label("Video Options");
		lVideoOptionsTitle.enableBackground(true);
		lVideoOptionsTitle.horizontalAlignment(ENTRY_ALIGNMENT.LEFT);

		mFullScreenEntry = new MenuEnumEntryIndexed<>(mScreenManager, lLayout, "Fullscreen");
		mFullScreenEntry.buttonSize(BUTTON_SIZE.wide);
		mResolutionEntry = new MenuDropDownEntry<>(mScreenManager, lLayout, "Resolution");
		mResolutionEntry.buttonSize(BUTTON_SIZE.wide);
		mMonitorEntry = new MenuEnumEntryIndexed<>(mScreenManager, lLayout, "Monitor");
		mVSync = new MenuToggleEntry(mScreenManager, lLayout);

		// Setup buttons
		mFullScreenEntry.addItem(mFullScreenEntry.new MenuEnumEntryItem(FULLSCREEN_NO, VideoSettings.FULLSCREEN_NO_INDEX));
		mFullScreenEntry.addItem(mFullScreenEntry.new MenuEnumEntryItem(FULLSCREEN_YES, VideoSettings.FULLSCREEN_YES_INDEX));
		mFullScreenEntry.setButtonsEnabled(true);
		mVSync.label("V-Sync");

		// Register listeners with this window
		mFullScreenEntry.registerClickListener(this, BUTTON_FULLSCREEN);
		mResolutionEntry.registerClickListener(this, BUTTON_RESOLUTION);
		mResolutionEntry.setToolTip("In windowed mode, you can drag the window borders to change the size!");
		mResolutionEntry.showInfoButton(true);
		mMonitorEntry.registerClickListener(this, BUTTON_MONITOR);
		mVSync.registerClickListener(this, BUTTON_VSYNC);

		// TODO: Add ToolTips for all menu options

		// Add the menu entries to the window
		lLayout.menuEntries().add(lVideoOptionsTitle);
		lLayout.menuEntries().add(mFullScreenEntry);
		lLayout.menuEntries().add(mMonitorEntry);
		lLayout.menuEntries().add(mResolutionEntry);
		lLayout.menuEntries().add(mVSync);

		fillMonitorEntry(mMonitorEntry);
		fillResolutions(mResolutionEntry, GLFW.glfwGetPrimaryMonitor(), 16, 9);

		mMonitorEntry.enabled(mDisplayManager.currentOptionsConfig().fullScreenIndex == VideoSettings.FULLSCREEN_YES_INDEX);
		mResolutionEntry.enabled(mDisplayManager.currentOptionsConfig().fullScreenIndex == VideoSettings.FULLSCREEN_YES_INDEX);

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void exitScreen() {
		// If the current settings are dirty then show a dialog to ask for confirmation (of losing changes) before leaving
		if (modifiedVideoConfig.isDifferent(currentVideoConfig)) {
			mConfirmationDialog = new ConfirmationDialog(mScreenManager, this, "You have some changes which have not been applied, are you sure you want to go back?");
			mConfirmationDialog.dialogTitle("Unsaved Changes");
			mConfirmationDialog.confirmEntry().entryText("Okay");
			mConfirmationDialog.confirmEntry().registerClickListener(this, ConfirmationDialog.BUTTON_CONFIRM_YES);

			mConfirmationDialog.cancelEntry().entryText("Cancel");
			mConfirmationDialog.cancelEntry().registerClickListener(this, ConfirmationDialog.BUTTON_CONFIRM_NO);

			mScreenManager.addScreen(mConfirmationDialog);

		} else {
			mScreenManager.core().config().display().saveConfig();

			super.exitScreen();

		}

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {

		case BUTTON_CANCEL_CHANGES:
			exitScreen();
			break;

		case BUTTON_APPLY_CHANGES:
			applyModifiedSettings();

			// if no changes, then quit out
			if (currentVideoConfig.equals(modifiedVideoConfig)) {
				exitScreen(); // shows

			}
			break;

		case ConfirmationDialog.BUTTON_CONFIRM_YES: // exit without saving
			if (mConfirmationDialog != null)
				mScreenManager.removeScreen(mConfirmationDialog);

			modifiedVideoConfig.copy(currentVideoConfig);

			exitScreen();
			break;

		case ConfirmationDialog.BUTTON_CONFIRM_NO: // go back and dont exit yet
			if (mConfirmationDialog != null)
				mScreenManager.removeScreen(mConfirmationDialog);

			break;

		case TimedConfirmationDialog.BUTTON_TIMED_CONFIRM_YES: // Revert to the last config
			if (m15SecConfirmationDialog != null)
				mScreenManager.removeScreen(m15SecConfirmationDialog);

			currentVideoConfig.copy(modifiedVideoConfig);
			lastVideoConfig.copy(modifiedVideoConfig);

			mConfirmChangesLayout.visible(modifiedVideoConfig.isDifferent(currentVideoConfig));
			mApplyButton.enabled(modifiedVideoConfig.isDifferent(currentVideoConfig));

			mDisplayManager.saveConfig();

			break;

		case TimedConfirmationDialog.BUTTON_TIMED_CONFIRM_NO: // Revert to the last config
			if (m15SecConfirmationDialog != null)
				mScreenManager.removeScreen(m15SecConfirmationDialog);

			revertSettings();

			break;

		}

	}

	@Override
	public void menuEntryChanged(MenuEntry e) {
		switch (e.entryID()) {
		case BUTTON_FULLSCREEN:
			modifiedVideoConfig.fullScreenIndex = mFullScreenEntry.selectedItem().value;

			// Enable the monitor selection if either fullscreen or fullscreen_borderless is selected.

			// Set the monitor
			if (modifiedVideoConfig.fullScreenIndex == VideoSettings.FULLSCREEN_YES_INDEX) {
				mMonitorEntry.enabled(true);
				mResolutionEntry.enabled(true);
				modifiedVideoConfig.monitorIndex = mMonitorEntry.selectedItem().value;
			} else {
				mMonitorEntry.enabled(false);
				mResolutionEntry.enabled(false);
			}

			break;

		case BUTTON_VSYNC:
			modifiedVideoConfig.vSyncEnabled = mVSync.isChecked();

			break;

		case BUTTON_MONITOR:
			modifiedVideoConfig.monitorIndex = mMonitorEntry.selectedItem().value;

			setResolutionEntry(modifiedVideoConfig.windowWidth, modifiedVideoConfig.windowHeight, 60);
			break;

		case BUTTON_RESOLUTION:
			modifiedVideoConfig.windowWidth = mResolutionEntry.selectedItem().value.width();
			modifiedVideoConfig.windowHeight = mResolutionEntry.selectedItem().value.height();
			modifiedVideoConfig.refreshRate = mResolutionEntry.selectedItem().value.refreshRate();

			break;

		}

		mConfirmChangesLayout.visible(modifiedVideoConfig.isDifferent(currentVideoConfig));
		mApplyButton.enabled(modifiedVideoConfig.isDifferent(currentVideoConfig));

	}

	private void setUIFromVideoSettings(VideoSettings pVideoConfig) {
		mVSync.isChecked(pVideoConfig.vSyncEnabled);
		mFullScreenEntry.setSelectedEntry(pVideoConfig.fullScreenIndex);
		mMonitorEntry.enabled(pVideoConfig.fullScreenIndex == VideoSettings.FULLSCREEN_YES_INDEX);

		// mMonitorEntry.setSelectedEntry(pVideoConfig.monitorHandle);
		setResolutionEntry(pVideoConfig.windowWidth, pVideoConfig.windowHeight, 60);

	}

	private void revertSettings() {
		mDisplayManager.setGLFWMonitor(lastVideoConfig);

		currentVideoConfig.copy(lastVideoConfig);
		modifiedVideoConfig.copy(lastVideoConfig);

		setUIFromVideoSettings(lastVideoConfig);

		// These should now be set to false ...
		mConfirmChangesLayout.visible(modifiedVideoConfig.isDifferent(currentVideoConfig));
		mApplyButton.enabled(modifiedVideoConfig.isDifferent(currentVideoConfig));

	}

	private void applyModifiedSettings() {
		if (!modifiedVideoConfig.isDifferent(currentVideoConfig))
			return;

		mDisplayManager.setGLFWMonitor(modifiedVideoConfig);

		// Add a timed confirmation dialog to the
		m15SecConfirmationDialog = new TimedConfirmationDialog(mScreenManager, this, "Your video settings have been changed. Do you want to keep these settings?");
		m15SecConfirmationDialog.dialogTitle("Confirm changes");

		m15SecConfirmationDialog.confirmEntry().entryText("Keep Settings");
		m15SecConfirmationDialog.cancelEntry().entryText("Revert");

		m15SecConfirmationDialog.setListener(this);

		m15SecConfirmationDialog.start(CONFIRMATION_TIMER_MILLI);

		mScreenManager.addScreen(m15SecConfirmationDialog);

	}

	private void fillMonitorEntry(MenuEnumEntryIndexed<Long> pEntry) {
		PointerBuffer lMonitorList = GLFW.glfwGetMonitors();

		final int COUNT = lMonitorList.limit();
		for (int i = 0; i < COUNT; i++) {
			long lMonitorHandle = lMonitorList.get();
			String lMonitorFullName = (i == 0 ? "[Primary]" : "") + GLFW.glfwGetMonitorName(lMonitorHandle);
			String lMonitorName = lMonitorFullName.substring(0, Math.min(lMonitorFullName.length(), 25));
			MenuEnumEntryIndexed<Long>.MenuEnumEntryItem lTest = pEntry.new MenuEnumEntryItem(lMonitorName, lMonitorHandle);

			mMonitorEntry.addItem(lTest);

		}

	}

	private void fillResolutions(MenuDropDownEntry<GLFWVidMode> pEntry, long pWindowHandle, float w, float h) {
		pEntry.clearItems();

		GLFWVidMode.Buffer modes = GLFW.glfwGetVideoModes(pWindowHandle);
		final int COUNT = modes.limit();
		for (int i = 0; i < COUNT; i++) {
			GLFWVidMode lVidMode = modes.get();

			// Ignore resolution entries based on low refresh rates
			if (lVidMode.refreshRate() < DisplayManager.MIN_REFRESH_RATE || lVidMode.refreshRate() > DisplayManager.MAX_REFRESH_RATE)
				continue;

			String lName = lVidMode.width() + "x" + lVidMode.height();

			if (!DisplayManager.HARD_REFRESH_RATE)
				lName += "@" + lVidMode.refreshRate();

			// Only add resolutions which meet our minimum and the selected aspect ratio
			final int MIN_WINDOW_RESOLUTION_W = 640;
			final int MIN_WINDOW_RESOLUTION_H = 480;

			if (lVidMode.width() > MIN_WINDOW_RESOLUTION_W && lVidMode.height() > MIN_WINDOW_RESOLUTION_H) {
				MenuDropDownEntry<GLFWVidMode>.MenuEnumEntryItem lTest = pEntry.new MenuEnumEntryItem(lName, lVidMode);
				pEntry.addItem(lTest);

			}

		}

	}

	private void setResolutionEntry(int pWidth, int pHeight, int pRefresh) {
		final int COUNT = mResolutionEntry.items().size();
		GLFWVidMode lBestFit = null;
		for (int i = 0; i < COUNT; i++) {
			GLFWVidMode lMode = mResolutionEntry.items().get(i).value;
			int lW = lMode.width();
			int lH = lMode.height();
			if (lW == pWidth && lH == pHeight) {
				if (lMode.refreshRate() == pRefresh) {
					mResolutionEntry.setSelectEntry(lMode);
					return;
				} else {
					lBestFit = lMode;
				}
			}
		}

		// Worst case use best fit
		if (lBestFit != null)
			mResolutionEntry.setSelectEntry(lBestFit);

	}

	// --------------------------------------
	// Callback Methods
	// --------------------------------------

	@Override
	public void timeExpired() {
		if (m15SecConfirmationDialog != null)
			mScreenManager.removeScreen(m15SecConfirmationDialog);

		revertSettings();

	}

	@Override
	public void confirmation() {
		if (m15SecConfirmationDialog != null)
			mScreenManager.removeScreen(m15SecConfirmationDialog);

		currentVideoConfig.copy(modifiedVideoConfig);
		exitScreen();

	}

	@Override
	public void decline() {
		if (m15SecConfirmationDialog != null)
			mScreenManager.removeScreen(m15SecConfirmationDialog);

		modifiedVideoConfig.copy(lastVideoConfig);
		applyModifiedSettings();

	}

}