package de.mad.seminarapp.activity;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

/**
 * @author albrandroid
 * @author Nicolas Gramlich
 * @author Stefan Steinhart
 * @since 10:35:23 - 28.02.2011
 */
public class BasicGameActivity extends SimpleBaseGameActivity {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;

	private static final float DEMO_VELOCITY = 100.0f;

	// ===========================================================
	// Fields
	// ===========================================================

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mFaceTextureRegion;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public EngineOptions onCreateEngineOptions() {

		final Camera camera = new Camera(0, 0, BasicGameActivity.CAMERA_WIDTH, BasicGameActivity.CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(BasicGameActivity.CAMERA_WIDTH,
				BasicGameActivity.CAMERA_HEIGHT), camera);
	}

	@Override
	public void onCreateResources() {

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 64, 32, TextureOptions.BILINEAR);
		this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_circle_tiled.png", 0, 0, 2,
				1);
		this.mBitmapTextureAtlas.load();
	}

	@Override
	public Scene onCreateScene() {

		this.mEngine.registerUpdateHandler(new FPSLogger());

		final Scene scene = new Scene();
		scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));

		final float centerX = (BasicGameActivity.CAMERA_WIDTH - this.mFaceTextureRegion.getWidth()) / 2;
		final float centerY = (BasicGameActivity.CAMERA_HEIGHT - this.mFaceTextureRegion.getHeight()) / 2;
		final Ball ball = new Ball(centerX, centerY, this.mFaceTextureRegion, this.getVertexBufferObjectManager());

		scene.attachChild(ball);

		return scene;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private static class Ball extends AnimatedSprite {

		private final PhysicsHandler mPhysicsHandler;

		public Ball(final float pX, final float pY, final TiledTextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager) {
			super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
			this.mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(this.mPhysicsHandler);
			this.mPhysicsHandler.setVelocity(BasicGameActivity.DEMO_VELOCITY, BasicGameActivity.DEMO_VELOCITY);
		}

		@Override
		protected void onManagedUpdate(final float pSecondsElapsed) {
			if (this.mX < 0) {
				this.mPhysicsHandler.setVelocityX(BasicGameActivity.DEMO_VELOCITY);
			} else if (this.mX + this.getWidth() > BasicGameActivity.CAMERA_WIDTH) {
				this.mPhysicsHandler.setVelocityX(-BasicGameActivity.DEMO_VELOCITY);
			}

			if (this.mY < 0) {
				this.mPhysicsHandler.setVelocityY(BasicGameActivity.DEMO_VELOCITY);
			} else if (this.mY + this.getHeight() > BasicGameActivity.CAMERA_HEIGHT) {
				this.mPhysicsHandler.setVelocityY(-BasicGameActivity.DEMO_VELOCITY);
			}

			super.onManagedUpdate(pSecondsElapsed);
		}
	}
}
