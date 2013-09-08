package za.co.appceptional.android.paperbandit;

import android.util.Log;
import android.widget.Spinner;

public class WinCalculator
{
	public static final int TOP_ROW = 1;
	public static final int MIDDLE_ROW = 2;
	public static final int BOTTOM_ROW = 4;
	public static final int FWD_DIAGONAL = 8;
	public static final int BACK_DIAGONAL = 16;
	public static final int SMALL_BET = MIDDLE_ROW;
	public static final int MEDIUM_BET = TOP_ROW | MIDDLE_ROW | BOTTOM_ROW;
	public static final int MAX_BET = TOP_ROW | MIDDLE_ROW | BOTTOM_ROW | FWD_DIAGONAL | BACK_DIAGONAL;
	
	//Returns the bit flag of all rows the player won on
	public static int GetWins (ScrollingImageSet [] spinners, int possible_wins)
	{
		Log.d ("WinCalculator", "00:" + spinners [0].sprite_from_middle(-1));
		Log.d ("WinCalculator", "01:" + spinners [1].sprite_from_middle(-1));
		Log.d ("WinCalculator", "02:" + spinners [2].sprite_from_middle(-1));
		Log.d ("WinCalculator", "10:" + spinners [0].sprite_from_middle(0));
		Log.d ("WinCalculator", "11:" + spinners [1].sprite_from_middle(0));
		Log.d ("WinCalculator", "12:" + spinners [2].sprite_from_middle(0));
		Log.d ("WinCalculator", "20:" + spinners [0].sprite_from_middle(1));
		Log.d ("WinCalculator", "21:" + spinners [1].sprite_from_middle(1));
		Log.d ("WinCalculator", "22:" + spinners [2].sprite_from_middle(1));
		
		int winnings = 
				is_win (spinners, possible_wins, TOP_ROW) |
				is_win (spinners, possible_wins, MIDDLE_ROW) |
				is_win (spinners, possible_wins, BOTTOM_ROW) |
				is_win (spinners, possible_wins, FWD_DIAGONAL) |
				is_win (spinners, possible_wins, BACK_DIAGONAL);
		
		Log.d ("WinCalculator", "WIN:" + winnings);
		
		return winnings;
	}
	
	//Returns the bit flag of the row if a) the player did bet that row, and b) they won on that row
	//Otherwise return 0
	public static int is_win (ScrollingImageSet [] spinners, int possible_wins, int test_win)
	{
		//If the player didn't bet that row, then they didn't win that row
		if ((test_win & possible_wins) == 0) return 0;

		int [] sprite_positions = positions_of_row (test_win);
		
		int val0 = spinners [0].sprite_from_middle(sprite_positions [0]);
		int val1 = spinners [1].sprite_from_middle(sprite_positions [1]);
		int val2 = spinners [2].sprite_from_middle(sprite_positions [2]);

		//There are two sprites different in the row - they didn't win
		if ((val0 != val1) || (val0 != val2)) return 0;
		
		return test_win;
	}
	
	public static int [] positions_of_row (int win_style)
	{
		switch (win_style)
		{
		case TOP_ROW : return new int [] {-1, -1, -1};
		case MIDDLE_ROW : return new int [] {0, 0, 0};
		case BOTTOM_ROW : return new int [] {1, 1, 1};
		case FWD_DIAGONAL : return new int [] {-1, 0, 1};
		case BACK_DIAGONAL : return new int [] {1, 0, -1};
		}
		return new int [] {0, 0, 0}; 
	}
}
