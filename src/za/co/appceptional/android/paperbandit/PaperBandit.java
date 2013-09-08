/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.appceptional.android.paperbandit;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;

/*
 * This animated wallpaper draws a rotating wireframe shape. It is similar to
 * example #1, but has a choice of 2 shapes, which are user selectable and
 * defined in resources instead of in code.
 */

public class PaperBandit extends WallpaperService {

	public static final String SHARED_PREFS_NAME = "paperbanditsettings";

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Engine onCreateEngine() {
		return new PaperBanditEngine();
	}

	class PaperBanditEngine extends Engine implements
			SharedPreferences.OnSharedPreferenceChangeListener {

		private final Handler mHandler = new Handler();

		private final Paint mPaint = new Paint();
		private CroppedImage mFrameDollar;
		private CroppedImage mBackgroundImage = null;
		private CroppedImage mFrameNormal;
		private CroppedImage mFrameMaxBet;
		private CroppedImage mFrameWin;
		private CroppedImage[] mButtons = new CroppedImage[0];
		private CroppedImage mButtonToShow = null;

		private CroppedImage mSpinApple;
		private CroppedImage mSpinBanana;
		private CroppedImage mSpinCherries;
		private CroppedImage mSpinDollar;
		private CroppedImage mSpinHeart;
		private CroppedImage mSpinMusic;
		private CroppedImage mSpinStar;

		private ScrollingImageSet mSpinner1;
		private ScrollingImageSet mSpinner2;
		private ScrollingImageSet mSpinner3;

		private float mOffset;
		private float mTouchX = -1;
		private float mTouchY = -1;
		private long mStartTime;
		private int mWidth;
		private int mHeight;

		private final Runnable mPaperBandit = new Runnable() {
			public void run() {
				drawFrame();
			}
		};
		private boolean mVisible;
		private SharedPreferences mPrefs;

		PaperBanditEngine() {
			mStartTime = SystemClock.elapsedRealtime();

			mPrefs = PaperBandit.this
					.getSharedPreferences(SHARED_PREFS_NAME, 0);
			mPrefs.registerOnSharedPreferenceChangeListener(this);
			onSharedPreferenceChanged(mPrefs, null);
		}

		/**
		 * Load an image
		 * 
		 * @param id
		 * @return Bitmap
		 */
		protected Bitmap getImage(int id) {
			return BitmapFactory.decodeResource(getApplicationContext()
					.getResources(), id);
		}

		public void onSharedPreferenceChanged(SharedPreferences prefs,
				String key) {
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			setTouchEventsEnabled(true);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			mHandler.removeCallbacks(mPaperBandit);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
			if (visible) {
				drawFrame();
			} else {
				mHandler.removeCallbacks(mPaperBandit);
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			mWidth = width;
			mHeight = height;
			loadImages();
			drawFrame();
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			super.onSurfaceCreated(holder);
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			mVisible = false;
			mHandler.removeCallbacks(mPaperBandit);
		}

		public void loadImages() {
			// if (height>width){mBackgroundImage =
			// getImage(R.drawable.background);}
			// else {mBackgroundImage = getImage(R.drawable.background);}

			// Debug.out("Wrinkled Paper");
			mBackgroundImage = new CroppedImage("p_paper", getApplicationContext(),
					R.drawable.p_paper, mWidth, mHeight, true);

			// Debug.out("Normal Frame");
			mFrameNormal = new CroppedImage("frame_normal", getApplicationContext(),
					mBackgroundImage, R.drawable.frame_normal);
			
			// Debug.out("Winning Frame");
			mFrameWin = new CroppedImage("frame_win", getApplicationContext(),
					mBackgroundImage, R.drawable.frame_win);
			
			// Debug.out("Dollar Frame");
			mFrameDollar = new CroppedImage("frame_dollar", getApplicationContext(),
					R.drawable.frame_dollar, mWidth, mHeight);

			// Debug.out("Max Bet Frame");
			mFrameMaxBet = new CroppedImage("frame_max_bet", getApplicationContext(),
					R.drawable.frame_max_bet, mWidth, mHeight);


			mButtons = new CroppedImage[] { mFrameDollar, mFrameMaxBet };

			mSpinApple = new CroppedImage("paper_apple", getApplicationContext(),
					mBackgroundImage, R.drawable.paper_apple);
			mSpinBanana = new CroppedImage("paper_banana", getApplicationContext(),
					mBackgroundImage, R.drawable.paper_banana);
			mSpinCherries = new CroppedImage("paper_cherries", getApplicationContext(),
					mBackgroundImage, R.drawable.paper_cherries);
			mSpinDollar = new CroppedImage("paper_dollar", getApplicationContext(),
					mBackgroundImage, R.drawable.paper_dollar);
			mSpinHeart = new CroppedImage("paper_heart", getApplicationContext(),
					mBackgroundImage, R.drawable.paper_heart);
			mSpinMusic = new CroppedImage("paper_music", getApplicationContext(),
					mBackgroundImage, R.drawable.paper_music);
			mSpinStar = new CroppedImage("paper_star", getApplicationContext(),
					mBackgroundImage, R.drawable.paper_star);

			mSpinner1 = new ScrollingImageSet(mBackgroundImage,
					new Bitmap[] { mSpinApple.mBitmap, mSpinBanana.mBitmap,
							mSpinCherries.mBitmap, mSpinDollar.mBitmap,
							mSpinHeart.mBitmap, mSpinMusic.mBitmap,
							mSpinStar.mBitmap }, 100, 40,
					mSpinApple.mBitmap.getHeight() * 5, new int[] { 0, 1, 2, 3,
							4, 5, 6 });

			mSpinner1.acceleration = 0.0f;
			mSpinner1.speed = 0.0f;
			mSpinner1.position = 0.0f;

			mSpinner2 = new ScrollingImageSet(mBackgroundImage,
					new Bitmap[] { mSpinApple.mBitmap, mSpinBanana.mBitmap,
							mSpinCherries.mBitmap, mSpinDollar.mBitmap,
							mSpinHeart.mBitmap, mSpinMusic.mBitmap,
							mSpinStar.mBitmap }, 200, 40,
					mSpinApple.mBitmap.getHeight() * 5, new int[] { 4, 2, 6, 3,
							5, 1, 0 });

			mSpinner2.acceleration = 0.0f;
			mSpinner2.speed = 0.0f;
			mSpinner2.position = 1.0f;

			mSpinner3 = new ScrollingImageSet(mBackgroundImage,
					new Bitmap[] { mSpinApple.mBitmap, mSpinBanana.mBitmap,
							mSpinCherries.mBitmap, mSpinDollar.mBitmap,
							mSpinHeart.mBitmap, mSpinMusic.mBitmap,
							mSpinStar.mBitmap }, 300, 40,
					mSpinApple.mBitmap.getHeight() * 5, new int[] { 6, 5, 4, 3, 2, 1, 0});

			mSpinner3.acceleration = 0.0f;
			mSpinner3.speed = 0.0f;
			mSpinner3.position = 2.0f;
		}

		/*
		 * This is when the screen is scrolled by the user. Later we will give
		 * money here. (non-Javadoc)
		 * 
		 * @see
		 * android.service.wallpaper.WallpaperService.Engine#onOffsetsChanged
		 * (float, float, float, float, int, int)
		 */
		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xStep,
				float yStep, int xPixels, int yPixels) {
			mOffset = xOffset;
			drawFrame();
		}

		/*
		 * Store the position of the touch event so we can use it for drawing
		 * later
		 */
		@Override
		public void onTouchEvent(MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mTouchX = event.getX();
				mTouchY = event.getY();

				int whichButton = CroppedImage.findTouchArea(mButtons, mTouchX,
						mTouchY);

				// Log.d("PaperBandit", "Event " + event.getX () + ":" +
				// event.getY ());
				// Log.d("PaperBandit", "Dollar " + mFrameDollar.mLeft + ":" +
				// mFrameDollar.mTop + ":" + mFrameDollar.mWidth + ":" +
				// mFrameDollar.mHeight);

				switch (whichButton) {
				case 0:
					mButtonToShow = mFrameDollar;
					drawFrame();
					// Dollar bill clicked
					Toast.makeText(getApplicationContext(),
							"Dollar bill clicked", Toast.LENGTH_LONG);
					
					mSpinner1.speed = 0.7f;
					mSpinner1.acceleration = -0.01f;
					
					mSpinner2.speed = 0.6f;
					mSpinner2.acceleration = -0.01f;
					
					mSpinner3.speed = 0.5f;
					mSpinner3.acceleration = -0.01f;
					
					
					break;
				case 1:
					mButtonToShow = mFrameMaxBet;
					drawFrame();
					Toast.makeText(getApplicationContext(), "Max bet clicked",
							Toast.LENGTH_LONG);
					// Max bet clicked
					break;
				default:
					mButtonToShow = null;
					drawFrame();
					Toast.makeText(getApplicationContext(), "Frame clicked",
							Toast.LENGTH_LONG);
					// Somewhere else clicked
				}
			} else {
				mTouchX = -1;
				mTouchY = -1;
			}
			super.onTouchEvent(event);
		}

		/*
		 * Draw one frame of the animation. This method gets called repeatedly
		 * by posting a delayed Runnable. You can do any drawing you want in
		 * here.
		 */
		void drawFrame() {
			final SurfaceHolder holder = getSurfaceHolder();
			final Rect frame = holder.getSurfaceFrame();
			final int width = frame.width();
			final int height = frame.height();

			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					// Draw paper
					mBackgroundImage.draw(c);
					
					mSpinner1.move_to_stop (1f);
					mSpinner2.move_to_stop (1f);
					mSpinner3.move_to_stop (1f);
					mSpinner1.draw(c);
					mSpinner2.draw(c);
					mSpinner3.draw(c);

					mFrameNormal.draw(c);
					
					if (mButtonToShow != null) {
						mButtonToShow.draw(c);
					}


					// draw something
					//drawTouchPoint(c);
				}
			} finally {
				if (c != null)
					holder.unlockCanvasAndPost(c);
			}

			mHandler.removeCallbacks(mPaperBandit);
			if (mVisible) {
				mHandler.postDelayed(mPaperBandit, 1000 / 25);
			}
		}

		void drawTouchPoint(Canvas c) {
			if (mTouchX >= 0 && mTouchY >= 0) {
				c.drawCircle(mTouchX, mTouchY, 80, mPaint);
			}
		}
	}
}
