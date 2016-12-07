package net.ld.library.core.config;

import static org.lwjgl.glfw.Callbacks.errorCallbackPrint;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GLContext;

import net.ld.library.GameInfo;
import net.ld.library.core.input.InputState;

public class DisplayConfig extends BaseConfig {

	// =============================================
	// Constants
	// =============================================

	public static final float GEOMETRY_SCREEN_SAFE_AREA = 20; // px overdraw

	// =============================================
	// Enums
	// =============================================

	public enum TARGET_FPS {
		fps50(50), fps60(60);

		public final int value;

		private TARGET_FPS(int pValue) {
			this.value = pValue;
		}
	}

	public enum CAMERA_SIZE {
		small, medium, large,
	}

	// =============================================
	// Class Variables
	// =============================================

	private boolean mFullScreen;
	private int mWindowWidth;
	private int mWindowHeight;
	private boolean mVSYNCEnabled;
	private boolean mWindowIsResizable;
	private boolean mWindowWasResized;

	private TARGET_FPS mTargetFPS;
	// We need to keep this reference alive
	@SuppressWarnings("unused")
	private GLFWFramebufferSizeCallback mFrameBufferSizeCallback;
	private long mWindowID;
	private boolean mRecompileShaders = false;

	List<IResizeListener> mResizeListeners;

	// =============================================
	// Properties
	// =============================================

	public long windowID() {
		return mWindowID;
	}

	public TARGET_FPS targetFPS() {
		return mTargetFPS;
	}

	public boolean recompileShaders() {
		return mRecompileShaders;
	}

	public boolean fullscreen() {
		return mFullScreen;
	}

	public int windowWidth() {
		return mWindowWidth;
	}

	public int windowHeight() {
		return mWindowHeight;
	}

	public boolean vsyncEnabled() {
		return mVSYNCEnabled;
	}

	public boolean windowResizable() {
		return mWindowIsResizable;
	}

	public boolean windowWasResized() {
		return mWindowWasResized;
	}

	public void resizeListeners(List<IResizeListener> pV) {
		mResizeListeners = pV;
	}

	public List<IResizeListener> resizeListeners() {
		return mResizeListeners;
	}

	// =============================================
	// Constructor
	// =============================================

	public DisplayConfig(GameInfo pGameInfo) {
		super(pGameInfo);

		/* set some defaults */
		mWindowWidth = mGameInfo.windowWidth();
		mWindowHeight = mGameInfo.windowHeight();
		mFullScreen = mGameInfo.windowCanBeFullscreen();
		mWindowIsResizable = mGameInfo.windowResizeable();
		mVSYNCEnabled = true;
		mTargetFPS = TARGET_FPS.fps60;

		mResizeListeners = new ArrayList<>();

	}

	// =============================================
	// Methods
	// =============================================

	public void clearListeners() {
		mResizeListeners.clear();
	}

	public void handleInput(InputState pInputState) {
		if (pInputState.keyDownTimed(GLFW.GLFW_KEY_H)) {
			mRecompileShaders = true;
		}

		if (pInputState.keyDownTimed(GLFW.GLFW_KEY_F11)) {
			mFullScreen = !mFullScreen;
			onCreateWindow();
		}
	}

	public void resetFlags() {
		mRecompileShaders = false;
		mWindowWasResized = false;
	}

	public void changeResolution(int pWidth, int pHeight) {

		mWindowWasResized = true;

		mWindowWidth = pWidth;
		mWindowHeight = pHeight;

		int lListenerCount = mResizeListeners.size();
		for (int i = 0; i < lListenerCount; i++) {
			mResizeListeners.get(i).onResize(pWidth, pHeight);
		}

	}

	public static void saveOptions() {

	}

	public static void loadOptions() {

	}

	public long onCreateWindow() {

		// All GLFW errors to the system err print stream
		glfwSetErrorCallback(errorCallbackPrint(System.err));

		if (glfwInit() != GL_TRUE) {
			System.err.println("Unable to initialize GLFW");
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		// Configure the window
		glfwDefaultWindowHints(); // GL3.2 CORE
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);

		if (mWindowID != NULL) {
			glfwDestroyWindow(mWindowID);
		}

		// FIXME: Full screen toggling doesn't work
		mFullScreen = false;
		if (mFullScreen && mGameInfo.windowCanBeFullscreen()) {

			// Get the native resolution
			ByteBuffer lVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			int lFullScreenWidth = GLFWvidmode.width(lVidMode);
			int lFullScreenHeight = GLFWvidmode.height(lVidMode);

			// Make sure to set the resolution we are actually using
			changeResolution(lFullScreenWidth, lFullScreenHeight);

			mWindowID = glfwCreateWindow(lFullScreenWidth, lFullScreenHeight, "", glfwGetPrimaryMonitor(), NULL);
			if (mWindowID == NULL) {
				System.err.println("Unable to create window!");
				throw new IllegalStateException("Unable to create window!");
			}
		}

		// Create a new windowed window
		else {

			mWindowID = glfwCreateWindow(windowWidth(), windowHeight(), mGameInfo.windowTitle(), NULL, NULL);
			if (mWindowID == NULL) {
				System.err.println("Unable to create window!");
				throw new IllegalStateException("Unable to create window!");
			}
		}

		// center the window
		ByteBuffer lVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(mWindowID, (GLFWvidmode.width(lVidMode) - windowWidth()) / 2, (GLFWvidmode.height(lVidMode) - windowHeight()) / 2);

		// Make the openGL the current context
		glfwMakeContextCurrent(mWindowID);

		glfwSwapInterval(1); // cap to 60 (v-sync)
		glfwShowWindow(mWindowID);

		//
		GLContext.createFromCurrent();

		glfwSetFramebufferSizeCallback(mWindowID, mFrameBufferSizeCallback = new GLFWFramebufferSizeCallback() {

			@Override
			public void invoke(long window, int width, int height) {
				changeResolution(width, height);
				;

			}
		});

		return mWindowID;

	}

	public void addResizeListener(IResizeListener pListener) {
		if (!mResizeListeners.contains(pListener)) {
			mResizeListeners.add(pListener);
		}
	}
}
