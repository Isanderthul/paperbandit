package za.co.appceptional.android.paperbandit;

import java.util.Currency;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class ScrollingImageSet
{
	Bitmap [] sprites;	//Array of sprites to display
	int mLeft;	//The left co-ordinate of the window to display (not used at all in calculations)
	int mTop;	//The top co-ordinate of the window to display (not used at all in calculations)
	int mHeight; //The height of the window to display - used in calculations
	int [] indexes;	//The actual value of the image in the different positions
	int shown_sprites;
	int middle_sprite;
	
	float position; //The current value of this roller set
	float speed; //The speed of this roller set
	float acceleration; //The acceleration of this roller set
	int desired_sprite;
	
	int snap_frames = 10;
	int snap_counter;
	
	
	public ScrollingImageSet(CroppedImage imageForScaling, Bitmap[] sprites, int mLeft, int mTop, int mHeight, int [] indexes)
	{
		super();
		this.sprites = sprites;
		this.mLeft = mLeft * imageForScaling.mWidth / imageForScaling.originalWidth;
		this.mTop = mTop * imageForScaling.mHeight / imageForScaling.originalHeight;
		this.mHeight = mHeight;
		this.indexes = indexes;
		position = 0.0f;
		speed = 0.0f;
		acceleration = 0.0f;
		shown_sprites = mHeight / sprites [0].getHeight ();
		middle_sprite = (shown_sprites - 1) / 2;
		
		Log.d ("ScrollingImageSet", "ss:" + shown_sprites);
		Log.d ("ScrollingImageSet", "wo:" + imageForScaling.originalWidth + ", wn:" + imageForScaling.mWidth);
		Log.d ("ScrollingImageSet", "ho:" + imageForScaling.originalHeight + ", hn:" + imageForScaling.mHeight);
		Log.d ("ScrollingImageSet", "lo:" + mLeft + ", ln:" + this.mLeft);
		Log.d ("ScrollingImageSet", "to:" + mTop + ", tn:" + this.mTop);
	}
	
	//Work backwards to see what speed the system should have started at to attain the correct end position
	//Acceleration MUST be negative if target_position is greater than this.position
	public void setup (float target_position, float time, float change_in_time, float acceleration)
	{
		float calc_position = position;
		float calc_speed = position;
		
		float calc_time = 0;
		speed = 0;
		
		while (calc_time < time)
		{
			calc_position += calc_speed * change_in_time;
			calc_speed += acceleration * change_in_time;
			calc_time += change_in_time;
		}
		
		this.speed = calc_speed;
		this.acceleration = acceleration;
	}
	
	public void move_to_stop (float change_in_time)
	{
		float distance_from_desired_position;
		speed += acceleration * change_in_time;
		
		//Check for too slow mode
		if (((acceleration < 0.0f) && (speed < 0.0f)) || ((acceleration > 0.0f) && (speed > 0.0f)))
		{	//Spinners changed direction
			//Calculate snap-to direction
			float sprite_height = sprites [0].getHeight ();
			
			int base_sprite = (int)Math.floor (position);
			
			float fraction_of_complete_sprite = (position - base_sprite);
			if (fraction_of_complete_sprite >= 0.5)
			{	//Snap forwards
				desired_sprite = base_sprite + 1;
				distance_from_desired_position = 1-fraction_of_complete_sprite;
			} else
			{	//Snap backwards
				desired_sprite = base_sprite;
				distance_from_desired_position = -fraction_of_complete_sprite;
			}

			//Attain a perfect row positioning within 10 frames
			speed = (distance_from_desired_position) / snap_frames;
			snap_counter = snap_frames;
			acceleration = 0;

			Log.d("ScrollingImageSet", "pos:" + position);
			Log.d("ScrollingImageSet", "ds:" + desired_sprite);
			Log.d("ScrollingImageSet", "focc:" + fraction_of_complete_sprite);
			Log.d("ScrollingImageSet", "dfdp:" + distance_from_desired_position);
		} else if (acceleration == 0.0f)
		{	//Snapping to an integer
			if (snap_counter > 0)
			{
				--snap_counter;
			}
			if (snap_counter == 0)
			{
				speed = 0.0f;
			}
		}
		
		position += speed * change_in_time;
	}

	public int sprite_from_middle (int distance)
	{
		int row = (middle_sprite + distance + desired_sprite) % sprites.length;
		
		return indexes [row];
	}
	
	public boolean is_stopped ()
	{
		return ((speed == 0.0f) && (acceleration == 0.0f) && (snap_counter == 0));
	}
	
	public void move (float change_in_time)
	{
		speed += acceleration * change_in_time;
		position += speed * change_in_time;
	}
	
	public void draw (Canvas c)
	{
		Bitmap current_sprite;
		int startY;	//Top pixel in the current sprite that will be drawn when I draw. Will only be non-zero for first sprite, otherwise will be 0
		int stopY;	//Bottom pixel in the current sprite that will be drawn when I draw
		int sprite_num; //Number of sprite currently being drawn
		int y;	//Location where the next set of picture will be drawn. Will increase after each sprite drawn by the amount drawn.
		
		Rect src;	//The subset of the sprite to use
		Rect dst;	//The location where the sprite will be drawn
		
		//Force position to be between 0 and number of sprites (like mod but for floating point numbers)
		while (position < 0) position += sprites.length;
		while (position >= sprites.length) position -= sprites.length;
		
		//only display the applicable bottom portion of the sprite
		sprite_num = (int)Math.floor(position);
		current_sprite = sprites [indexes [sprite_num]];
		float frac = (position - sprite_num);
		startY = (int)(current_sprite.getHeight() * frac);
		
		y = 0;	//We start drawing at the top of the window
		while (y < mHeight)
		{
			stopY = current_sprite.getHeight ();
			
			//Check if this sprite will overflow the window - if so display less than the whole sprite (reduce stopY) 
			if (y + (stopY - startY) > mHeight)
			{
				stopY = (mHeight - y) - startY;
			}

			//Draw the image
			src = new Rect (0, startY, current_sprite.getWidth(), stopY);
			dst = new Rect (mLeft, mTop + y, mLeft + current_sprite.getWidth (), mTop + y + (stopY - startY));
			c.drawBitmap (current_sprite, src, dst, null);

			//Add to our tally of how far down we've drawn
			y += stopY - startY;

			//Move on to next sprite
			++sprite_num;
			//If we hit the end of the list start again at the beginning
			if (sprite_num == sprites.length) sprite_num = 0;
			current_sprite = sprites [indexes [sprite_num]];
			//Start drawing from the top of this sprite
			startY = 0;
		}
		
		/*
		 *Draw a red line to show the location of things to be drawn
		Paint p = new Paint ();
		p.setColor(Color.RED);
		c.drawLine(mLeft, mTop, mLeft + current_sprite.getWidth(), mTop + mHeight, p);
		*/
	}
}
