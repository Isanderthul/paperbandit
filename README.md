paperbandit
===========

An animated one-arm bandit live wallpaper for Android.

Functions:
----------
Bets are:
Tap : 1 credit - middle payout line
Dollar : 3 credits - three horizontal payouts
Max bet : 5 credits - 2 diagonals and 3 horizontal payout lines
Swipe : Dollar bill icon follows your finger, and a successful swipe increase credits by 1 with standard credits animation.

Symbols:
--------
There are the following symbols with the following probabilities :
Apple : 1/7
Banana : 1/7
Cherries : 1/7
Music : 1/7
Heart : 1/7
Dollar : 1/7
Star : 1/7

Payouts:
--------
Payouts are for 3 in a row : 
Apple : 30
Banana : 50
Cherries : 80
Music : 100
Heart : 200
Dollar : 300
Star : 500

Logic:
------
The app will make 3 preconfigured sprite sheets - 1 per column.

Upon rolling the app will pick a random slow speed for left column (SLOW_SPEED + random), medium speed 

(MEDIUM_SPEED + random) for middle column, and fast speed for right column (FAST_SPEED + random). Columns will 

decelerate at a fixed rate (DECELERATION). When the speed of a column reaches zero, it will decide whether it 

overshot or undershot a slot, and rewind or forward at a fixed speed to its discrete slot.

GUI:
----
Once all columns have stopped, the bet-upon lines are shown in yellow highlighter. The winning lines flash yellow highlighter to green highlighter while credits are incrementing. If there are 1 or more winning lines, then the symbols around the screen also flash. Credits increment at 10 per second. By tapping on the screen after at least three seconds, the final credits are warped into position, and all animation stops instantly - ready for the next bet.

States:
-------
Waiting for bet
Bet value display (credits decreasing + clicked-on bet button highlights + payout lines)
Follow swipe
Increase money (possible wins flashing)
Spindles rolling

Z-Order:
--------
Paper
Frames
Symbols
Buttons
Highlights
Dollar bill mouse cursor

Settings:
---------
About
Help - describe betting and payouts
Sharing & Rating URL intents

Layouts:
--------
Portrait : 3x3 Spinning bars above buttons and credits
Lanscape : 3x3 Spinning bars to the left of buttons and credits
