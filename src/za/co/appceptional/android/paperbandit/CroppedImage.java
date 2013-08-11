package za.co.appceptional.android.paperbandit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

public class CroppedImage {
	int mLeft;
	int mTop;
	int mWidth;
	int mHeight;
	Bitmap mBitmap;

	public CroppedImage(Context context, int resourceId, int scaledWidth,
			int scaledHeight) {
		this(context, resourceId, scaledWidth, scaledHeight, false);
	}

	public CroppedImage(Context context, int resourceId, int scaledWidth,
			int scaledHeight, boolean skipCropping) {
		int minx;
		int miny;
		int maxx;
		int maxy;

		Bitmap uncropped = getImage(context, resourceId);
		// Scale the source uncropped image to the full size of the canvas
		uncropped = Bitmap.createScaledBitmap(uncropped, scaledWidth,
				scaledHeight, true);

		if (skipCropping) {
			mBitmap = uncropped;
			mLeft = 0;
			mTop = 0;
			mWidth = uncropped.getWidth();
			mHeight = uncropped.getHeight();
		} else {
			minx = uncropped.getWidth();
			miny = uncropped.getHeight();
			maxx = 0;
			maxy = 0;

			for (int y = 0; y < uncropped.getHeight(); ++y) {
				for (int x = 0; x < uncropped.getWidth(); ++x) {
					int alpha = Color.alpha(uncropped.getPixel(x, y));
					if (alpha > 0) {
						if (x < minx)
							minx = x;
						if (y < miny)
							miny = y;
						if (x > maxx)
							maxx = x;
						if (y > maxy)
							maxy = y;
					}
				}
			}

			this.mLeft = minx;
			this.mTop = miny;
			this.mWidth = maxx - minx;
			this.mHeight = maxy - miny;
			this.mBitmap = Bitmap.createBitmap(uncropped, mLeft, mTop, mWidth,
					mHeight);
		}
	}

	protected Bitmap getImage(Context context, int id) {
		return BitmapFactory.decodeResource(context.getResources(), id);
	}

	/*
	 * Tests if the area encompasses the specified point. It first does a coarse
	 * check of the bounding box of the bitmap. Then it does a check of the
	 * transparency of the specific pixel of the bitmap
	 */
	public boolean InArea(float x, float y) {
		// Check if point is on the Bitmap at least, otherwise return false
		if (x < mLeft)
			return false;
		if (y < mTop)
			return false;
		if (x >= (mLeft + mBitmap.getWidth()))
			return false;
		if (y >= (mTop + mBitmap.getHeight()))
			return false;

		/*
		 * If code reaches here, then the point is on the bitmap. Make x & y
		 * relative to the top-left of the bitmap itself. Then check if the
		 * point was an opaque point of the bitmap
		 */
		x -= this.mLeft;
		y -= this.mTop;

		int alpha = Color.alpha(mBitmap.getPixel((int) x, (int) y));

		// Log.d("CroppedImage", "Alpha : " + alpha);

		return (alpha != 0);
	}

	// Returns the index of the first area in the array of areas that
	// encompasses the point x and y.
	public static int findTouchArea(CroppedImage[] areas, float x, float y) {
		for (int i = 0; i < areas.length; ++i)
			if (areas[i].InArea(x, y))
				return i;
		return -1;
	}

	public void draw(Canvas c) {
		c.drawBitmap(mBitmap, mLeft, mTop, null);
	}
}
