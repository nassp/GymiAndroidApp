package de.mad.seminarapp.activity;

import java.util.ArrayList;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.debugdraw.DebugRenderer;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;
import org.andengine.util.math.MathUtils;

import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

/**
 * @author albrandroid
 * @author Nicolas Gramlich
 * @author Stefan Steinhart
 * @since 10:35:23 - 28.02.2011
 */
public class GameActivity extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener, IOnAreaTouchListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private int CAMERA_WIDTH;
	private int CAMERA_HEIGHT;

	private Camera mCamera;

	// fixture binds a shape to a body and defines things like density,friction,
	// restitution(bounce), collision filtering, sensoring
	// ref to: http://www.box2d.org/manual.html#_Toc258082972
	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	private static final FixtureDef PADDLE_FIXTUREDEF = PhysicsFactory.createFixtureDef(0, 1, 0);
	private static final FixtureDef BALL_FIXTUREDEF = PhysicsFactory.createFixtureDef(1, 1, 0);
	private static final FixtureDef WALL_FIXTUREDEF = PhysicsFactory.createFixtureDef(1, 1, 0);

	// ===========================================================
	// Fields
	// ===========================================================

	private BitmapTextureAtlas mFacesTextureAtlas;
	private TiledTextureRegion mBoxFaceTextureRegion;
	private TiledTextureRegion mCircleFaceTextureRegion;

	private Scene mScene;

	private PhysicsWorld mPhysicsWorld;

	ArrayList<Body> bodyarray = new ArrayList<Body>();
	static int bodycount = 0;
	private int mFaceCount = 0;

	private Body mGroundBody;

	private int PADDLE_WIDTH;
	private int PADDLE_HEIGHT;
	private Body paddle;

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

		Toast.makeText(this, "Catch the faces.", Toast.LENGTH_LONG).show();

		DisplayMetrics displayMetrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		CAMERA_HEIGHT = displayMetrics.heightPixels;
		CAMERA_WIDTH = displayMetrics.widthPixels;

		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
	}

	@Override
	public void onCreateResources() {

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mFacesTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 64, 64, TextureOptions.BILINEAR);
		this.mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mFacesTextureAtlas, this, "face_box_tiled.png", 0, 0, 2,
				1); // 64x32
		this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mFacesTextureAtlas, this, "face_circle_tiled.png", 0,
				32, 2, 1); // 64x32
		this.mFacesTextureAtlas.load();
	}

	@Override
	public Scene onCreateScene() {

		this.enableAccelerationSensor(this);

		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0, 0, 0));
		this.mScene.setOnSceneTouchListener(this);
		this.mScene.setOnAreaTouchListener(this);

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		this.mGroundBody = this.mPhysicsWorld.createBody(new BodyDef());

		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);

		PADDLE_WIDTH = 100;
		PADDLE_HEIGHT = 20;
		Rectangle paddleRect = new Rectangle(0, 0, PADDLE_WIDTH, PADDLE_HEIGHT, vertexBufferObjectManager);
		paddleRect.setPosition(CAMERA_WIDTH / 2, CAMERA_HEIGHT - 40);
		this.paddle = PhysicsFactory.createBoxBody(this.mPhysicsWorld, paddleRect, BodyType.KinematicBody, PADDLE_FIXTUREDEF);
		this.paddle.setUserData("paddle");
		this.mScene.attachChild(paddleRect);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(paddleRect, this.paddle, true, true));

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		this.createSpriteSpawnTimeHandler(2);

		DebugRenderer debug = new DebugRenderer(mPhysicsWorld, getVertexBufferObjectManager());
		mScene.attachChild(debug);

		mPhysicsWorld.setContactListener(new ContactListener() {

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				// TODO Auto-generated method stub

			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				// TODO Auto-generated method stub

			}

			@Override
			public void endContact(Contact contact) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beginContact(Contact contact) {
				// TODO Auto-generated method stub

				Body bodya = contact.getFixtureA().getBody();
				Body bodyb = contact.getFixtureB().getBody();
				if (bodya.getUserData() == "paddle") {
					bodyarray.add(bodyb);
				}
			}
		});

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		mScene.registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUpdate(float pSecondsElapsed) {
				// TODO Auto-generated method stub
				try {
					getCollisionUpdateHandler();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		});

		this.mScene.setOnAreaTouchListener(this);

		return this.mScene;
	}

	@Override
	public void onGameCreated() {
		this.mEngine.enableVibrator(this);
	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		if (this.mPhysicsWorld != null) {
			switch (pSceneTouchEvent.getAction()) {
			case TouchEvent.ACTION_DOWN:

				return true;
			case TouchEvent.ACTION_MOVE:

				this.paddle.setTransform(pSceneTouchEvent.getX() / 32, this.paddle.getPosition().y, 0);

				return true;
			case TouchEvent.ACTION_UP:

				return true;
			}
			return false;
		}
		return false;
	}

	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		if (pSceneTouchEvent.isActionDown()) {
			// final IAreaShape paddle = (IAreaShape) pTouchArea;

			return true;
		}
		return false;
	}

	@Override
	public void onAccelerationAccuracyChanged(final AccelerationData pAccelerationData) {

	}

	@Override
	public void onAccelerationChanged(final AccelerationData pAccelerationData) {

		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(), pAccelerationData.getY());

		// Vector2 force = new Vector2(gravity.x, 0.0f);
		// Vector2 point = this.paddle.getWorldCenter();
		// this.paddle.applyLinearImpulse(force, point);

		Vector2Pool.recycle(gravity);
	}

	@Override
	public void onResumeGame() {

		super.onResumeGame();

		this.enableAccelerationSensor(this);
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

		this.disableAccelerationSensor();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void createSpriteSpawnTimeHandler(float timeInterval) {

		final TimerHandler spriteTimerHandler = new TimerHandler(timeInterval, true, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {

				// spriteTimerHandler.reset();

				// Random Position Generator
				final float xPos = MathUtils.random(20.0f, (CAMERA_WIDTH - 20.0f));
				final float yPos = MathUtils.random(0, 30.0f);

				Body body = addFace(xPos, yPos);

				final float angleX = MathUtils.random(0, 120.0f);
				final float angleY = MathUtils.random(0, 30.0f);
				shootBody(body, angleX, angleY, 10);
				mEngine.vibrate(70);
			}
		});

		this.getEngine().registerUpdateHandler(spriteTimerHandler);
	}

	private Body addFace(final float pX, final float pY) {
		this.mFaceCount++;
		Debug.d("Faces: " + this.mFaceCount);

		final AnimatedSprite face;
		final Body body;

		// if (this.mFaceCount % 2 == 0) {
		// face = new AnimatedSprite(pX, pY, this.mBoxFaceTextureRegion,
		// this.getVertexBufferObjectManager());
		// body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face,
		// BodyType.DynamicBody, BALL_FIXTUREDEF);
		// } else {
		face = new AnimatedSprite(pX, pY, this.mCircleFaceTextureRegion, this.getVertexBufferObjectManager());
		body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face, BodyType.DynamicBody, BALL_FIXTUREDEF);
		// }
		body.setUserData(face);
		face.animate(200);
		this.mScene.attachChild(face);

		// mCamera.setChaseEntity(face);

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));

		return body;
	}

	private void removeFace(final AnimatedSprite face) {
		final PhysicsConnector facePhysicsConnector = this.mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(face);

		this.mPhysicsWorld.unregisterPhysicsConnector(facePhysicsConnector);
		this.mPhysicsWorld.destroyBody(facePhysicsConnector.getBody());

		this.mScene.unregisterTouchArea(face);
		this.mScene.detachChild(face);

		System.gc();
	}

	public void getCollisionUpdateHandler() {
		runOnUpdateThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (bodyarray.size() > 0) {
					for (int i = 0; i < bodyarray.size(); i++) {
						try {
							final int myid = i;
							Debug.e("ID = " + myid + "BODY SIZE" + bodyarray.get(myid).getUserData());
							final AnimatedSprite as = (AnimatedSprite) bodyarray.get(myid).getUserData();
							removeFace(as);
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				}
			}
		});
	}

	private void shootBody(Body body, final float pX, final float pY, final float pDistance) {
		float angleRad = (float) Math.atan2(pY, pX);
		float velocity = (pDistance * 12.5f) / 100f;
		if (body != null) {
			float Vx = velocity * (float) Math.cos(angleRad);
			float Vy = velocity * (float) Math.sin(angleRad);
			body.applyLinearImpulse(new Vector2(Vx, Vy), body.getWorldCenter());
			// body.setAngularDamping(0.8f); // to decrease velocity slowly. no
			// linear no floaty
			body.applyTorque(100f);
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
