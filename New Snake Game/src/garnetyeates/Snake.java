package garnetyeates;

import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;

public class Snake extends JPanel implements ActionListener, KeyListener, ComponentListener
{
	private static final long serialVersionUID = 1L;

	public static final Color R = new Color(255, 0, 0);
	public static final Color O = new Color(255, 127, 0);
	public static final Color Y = new Color(255, 255, 0);
	public static final Color G = new Color(0, 255, 0);
	public static final Color B = new Color(0, 0, 255);
	public static final Color I = new Color(75, 0, 130);
	public static final Color V = new Color(148, 0, 211);

	public static Color[] defaultScheme = new Color[] { G, Y, O, R };
	public static Color[] rainbowScheme = new Color[] { R, O, Y, G, B, I, V };

	private GameFrame frame;

	public int windowWidth = 1400;
	public int windowHeight = 777;

	private int spaceSize;
	private int squareSize;
	private int preferredHeight;
	private int preferredWidth;
	private int numVerticalSquares;
	private int numHorizontalSquares = 40;
	private int numVerticalSpaces;
	private int numHorizontalSpaces;

	private Color spaceColor = new Color(20, 20, 20);
	private Color borderColor = new Color(0, 0, 0);

	private Color fruitColor;
	private Color cannibalFruitColor;
	private Color deadColor = Color.GRAY;
	private Color frontColor;
	private Color backColor;

	private Timer timer;
	private int startingDelay;
	private int delay;
	private int endingDelay;
	private int progressPercentTillTopSpeed;

	private String[][] map;

	private SegmentMap segmentLocations;
	private int headIndex;

	private int numStartingSegments;

	private double defaultFoodSegmentWorth;
	private double foodSegmentWorth;

	private Direction facing = Direction.RIGHT;

	private boolean dead = false;

	private boolean mortal = true;

	private int coverableArea = 0;
	private int numSquaresFilled = 0;

	private Timer buffClock;

	public Snake(GameFrame frame, int numHorizontalSquares, int startingDelay, int endingDelay, int progressTillMaxDelay,
			Color[] colors, int foodValue, int spaceSize)
	{
		this.frame = frame;

		this.spaceSize = spaceSize;

		defaultFoodSegmentWorth = foodValue;
		foodSegmentWorth = defaultFoodSegmentWorth;

		this.numHorizontalSquares = numHorizontalSquares;
		updateSpatialFields(true);

		backColor = colors[0];
		frontColor = colors[colors.length - 1];
		initFruitColors();

		numStartingSegments = colors.length;
		if (numStartingSegments < 3)
			numStartingSegments = 3;

		segmentLocations = new SegmentMap(colors);
		addInitialSegmentsToMap();

		map = createMap(numVerticalSquares, this.numHorizontalSquares);

		progressPercentTillTopSpeed = progressTillMaxDelay;
		this.startingDelay = startingDelay;
		this.endingDelay = endingDelay;
		delay = startingDelay;
		timer = new Timer(delay, this);
		timer.setInitialDelay(0);
		timer.start();

		buffClock = new Timer(BUFF_TIMER_DELAY, this.new BuffClock());
		buffClock.setInitialDelay(0);
		buffClock.start();

		for (int i = 0; i < map.length; i++)
			for (int l = 0; l < map[i].length; l++)
				if (map[i][l].equals(" "))
					coverableArea++;
		coverableArea -= numStartingSegments;

		headIndex = segmentLocations.size() - 1;

		addSegmentsToMap();

		addFruitToMap();
	}

	/**
	 * Adds the first few Segments of the snake to the {@link #segmentLocations} map
	 */
	private void addInitialSegmentsToMap()
	{
		int y = 2;
		int x = 2;

		for (int i = 0; i < numStartingSegments; i++)
		{
			segmentLocations.put(y + "," + x, null);
			x++;
		}
	}

	/**
	 * Adjusts the size of each square in the GUI so the game can fit into the
	 * JFrame properly
	 */
	private void updateSpatialFields(boolean init)
	{
		numVerticalSpaces = this.numHorizontalSquares + 1;
		squareSize = (windowWidth - numVerticalSpaces * spaceSize) / this.numHorizontalSquares;

		if (init)
		{
			double heightToWidthRatio = (double) windowHeight / (double) windowWidth;

			numVerticalSquares = (int) (this.numHorizontalSquares * heightToWidthRatio);
			numHorizontalSpaces = numVerticalSquares + 1;
		}

		preferredWidth = (this.numHorizontalSquares * squareSize) + (numVerticalSpaces * spaceSize) - spaceSize;
		preferredHeight = (numVerticalSquares * squareSize) + (numHorizontalSpaces * spaceSize) - spaceSize;

		setPreferredSize();
	}

	private int tickNum = 0;

	/**
	 * Called once every {@link #delay} milliseconds. Adjusts the direction that the
	 * snake is moving based on the earliest keystroke saved in the
	 * {@link #directionStrokes} cache. This cache can save up to
	 * {@link #directionCacheSize} (default 3) keystrokes, and the snake's direction
	 * can only be changed once per timer tick. This means that if the user presses
	 * left, up, and left again in quick succession, it will take 3 timer ticks to
	 * perform this movement sequence. After figuring out what direction the snake
	 * should move in next, it commands it to move forwards using the
	 * {@link #preMove()} method. Also see {@link #addDirectionStroke(int)}
	 */
	private void tick()
	{
		// debug();
		if (cannibalBuff)
			borderColor = Color.WHITE;
		else
			borderColor = Color.BLACK;

		if (!dead)
		{
			tickNum++;

			digest();
			updateDirectionField();
			preMove();
			frame.updateStats();
		}
	}

	/**
	 * Returns the number of ticks that occurred in this game
	 * 
	 * @return the number of ticks that ticked this game
	 */
	public int getTickNum()
	{
		return tickNum;
	}

	/**
	 * Updates the {@link #facing} field depending on if there are any keystrokes
	 * saved in the {@link #directionStrokes} cache. If there aren't any keystrokes
	 * in the cache, the Snake will continue moving in the direction it was moving
	 * in before this method was called
	 */
	private void updateDirectionField()
	{
		if (directionStrokes.size() > 0)
		{
			Direction nextDir = directionStrokes.get(0);

			switch (nextDir)
			{
			case UP:
				if (facing != Direction.DOWN)
					facing = nextDir;
				break;
			case DOWN:
				if (facing != Direction.UP)
					facing = nextDir;
				break;
			case LEFT:
				if (facing != Direction.RIGHT)
					facing = nextDir;
				break;
			case RIGHT:
				if (facing != Direction.LEFT)
					facing = nextDir;
				break;

			}

			directionStrokes.remove(0);
		}
	}

	/**
	 * Before actually changing the head position of the snake, it checks to see
	 * whether the snake is going to collide into anything and changes the state of
	 * the game based on this. <br>
	 * <br>
	 * If the snake is going to collide with either itself or the border of the map,
	 * the snake will die and the game will end. <br>
	 * <br>
	 * If the snake is going to collide with a fruit, {@link #foodSegmentWorth}
	 * segments will be added to the end of the snake (see
	 * {@link #move(ArrayPoint)}) <br>
	 * <br>
	 * If the snake isn't going to collide anything, no additional operation will be
	 * done <br>
	 * <br>
	 * No matter what the case is, {@link #move(ArrayPoint)} is called after (even
	 * if the snake dies). This method will make every body position move to the
	 * location of the one in front of it (besides the head, because it is in
	 * front). The head is moved based on the newHeadLoc parameter of the
	 * {@link #move(ArrayPoint)} method. <br>
	 * <br>
	 * See {@link #move(ArrayPoint)}
	 */
	private void preMove()
	{
		ArrayPoint headLoc = segmentLocations.at(headIndex);
		ArrayPoint nextHeadLoc = null;

		switch (facing)
		{
		case RIGHT:
			nextHeadLoc = new ArrayPoint(headLoc.getY(), headLoc.getX() + 1);
			break;
		case LEFT:
			nextHeadLoc = new ArrayPoint(headLoc.getY(), headLoc.getX() - 1);
			break;
		case DOWN:
			nextHeadLoc = new ArrayPoint(headLoc.getY() + 1, headLoc.getX());
			break;
		case UP:
			nextHeadLoc = new ArrayPoint(headLoc.getY() - 1, headLoc.getX());
			break;
		}

		int newY = nextHeadLoc.getY();
		int newX = nextHeadLoc.getX();

		switch (map[newY][newX])
		{
		case "B":
			ON_BODY_COLLISION(nextHeadLoc);
			break;
		case "+":
			ON_WALL_COLLISION(nextHeadLoc);
			break;
		case "F":
			ON_FRUIT_COLLISION(nextHeadLoc);
			break;
		case "E":
			ON_FRUIT_MUNCHIES_COLLISION(nextHeadLoc);
			break;
		default:
			move(nextHeadLoc);
			break;
		}
	}

	/**
	 * Called whenever the Snake is about to ( between preMove() and move() )
	 * collide with the location of a cannibal power-up. When this happens, the
	 * snake gains the cannibal power-up for {@link #cannibalFruitBuffTimeWorth}
	 * seconds. After this, {@link #move(ArrayPoint)} is called to move the
	 * head/body of the snake towards this location
	 * 
	 * @param fruitLoc The location of the fruit that the Snake is about to collide
	 *                 with
	 */
	private void ON_FRUIT_MUNCHIES_COLLISION(ArrayPoint fruitLoc)
	{
		buffClock.restart();
		remainingCannibalSeconds += cannibalFruitBuffTimeWorth;
		move(fruitLoc);
	}

	/**
	 * Called whenever the Snake is about to collide with the location of another
	 * Snake segment. <br>
	 * <br>
	 * If this happens and the cannibal power-up is active, then the snake will
	 * {@link #eat(Color, boolean)} that segment and move to it. <br>
	 * <br>
	 * If the cannibal power-up is not active and mortality is on (cheat mode is
	 * off), the snake will die. <br>
	 * <br>
	 * If the cannibal power-up is not active but cheat mode is on, the snake will
	 * simply do nothing
	 * 
	 * @param bodyLoc The location of the body segment that the Snake is about to
	 *                collide with
	 */
	private void ON_BODY_COLLISION(ArrayPoint bodyLoc)
	{
		if (cannibalBuff)
		{
			eat(Color.DARK_GRAY);
			move(bodyLoc);
		} else if (mortal)
		{
			dead = true;
			move(bodyLoc);
		}
	}

	/**
	 * Called whenever the snake is about to collide with a wall. If the snake is
	 * immortal (cheat mode is activated) then it will simply stop at the wall.
	 * Otherwise the snake will die.
	 * 
	 * @param wallLoc The location of the wall that the snake is about to collide
	 *                with.
	 */
	private void ON_WALL_COLLISION(ArrayPoint wallLoc)
	{
		if (mortal && !cannibalBuff)
		{
			dead = true;
			move(wallLoc);
		}
	}

	/**
	 * 
	 * Called whenever the snake is about to collide with a regular fruit. When this
	 * happens, {@link #foodSegmentWorth} fruit are added to the fruit digesting
	 * queue. The snake will then move to the location of the fruit that it is about
	 * to collide with.
	 * 
	 * @param fruitLoc The location of the fruit that the snake is about to collide
	 *                 with
	 */
	private void ON_FRUIT_COLLISION(ArrayPoint fruitLoc)
	{
		foodSegmentWorth = defaultFoodSegmentWorth * (1 + (progress * 2.2));
		eatMultiple(fruitColor, (int) foodSegmentWorth);
		move(fruitLoc);
		addFruitToMap();
	}

	/**
	 * Moves the location of the Snake's head and updates the position of the
	 * Snake's body to adjust to this movement. To update these positions, every
	 * body position is set to the location of the body part directly in front of
	 * it. This is done by iterating through the {@link #segmentLocations} ArrayList
	 * and for every index i, add i + 1 into a new ArrayList called
	 * newSegmentLocations. The only segment that isn't moved with this iteration is
	 * the head (since it is in front and there is no segment in front of it for it
	 * to be moved to). Instead, the new head location (a parameter of this method)
	 * is added to the end of the newSegmentLocations list after the other segments
	 * are added. <br>
	 * <br>
	 * This method is also used to add segments to the end of the snake whenever the
	 * Snake eats a fruit. This is done by appending the tail location from
	 * {@link #segmentLocations} into the beginning of the newSegmentLocations list
	 * before the other segments are added to it. Even though eating a fruit adds
	 * multiple segments to the Snake (see {@link #foodSegmentWorth}), only one
	 * segment is added every timer tick (this method is called once every timer
	 * tick).
	 * 
	 * @param newHeadLoc The new ArrayPoint location of the Snake's head.
	 */
	private void move(ArrayPoint newHeadLoc)
	{
		clearSegmentsFromMap();

		SegmentMap newSegmentLocations = segmentLocations.cloneEmpty();

		if (segmentsToBeAdded > 0)
		{
			segmentsToBeAdded--;
			newSegmentLocations.add(segmentLocations.at(0)); // Append the old tail location into the new segment locations
																				// list if segmentsToBeAdded > 0 (i.e the snake ate a fruit)
			headIndex++; // (this increases the length of the Snake by 1)
		}

		for (int i = 0; i < segmentLocations.size() - 1; i++)
		{
			newSegmentLocations.add(segmentLocations.at(i + 1)); // Move every segment location to the location of the one
																					// in front of it
		}

		/*
		 * Remove in case newHeadLoc is equivalent to another body segment location (i.e
		 * the snake hit itself). This makes it so that instead of replacing the body
		 * segment key with newHeadLoc, it adds newHeadLoc to the end of the list so
		 * that it's actually at the head location.
		 */
		if (newSegmentLocations.containsKey(newHeadLoc.toString()))
		{
			headIndex--;
			newSegmentLocations.remove(newHeadLoc.toString());
		}

		newSegmentLocations.put(newHeadLoc.toString(), null); // Add the new head location to the end of the
																				// newSegmentLocations list since it wasn't added yet
		segmentLocations = newSegmentLocations;
		segmentLocations.setColors();
		numSquaresFilled = segmentLocations.size() + digestMap.size() + segmentsToBeAdded - numStartingSegments;
		updateProgress();
		updateDelay();
		addSegmentsToMap();
	}

	private ArrayList<Direction> directionStrokes = new ArrayList<Direction>();
	private int directionCacheSize = 3;

	/**
	 * If the user presses the up, down, left, or right arrow key, it will add the
	 * keystroke into an ArrayList called {@link #directionStrokes}. Up to
	 * {@link #directionCacheSize} strokes can be in this log at once, and if it is
	 * full it will not add any more keystrokes. It will also not add a specified
	 * keystroke if it already appears at the end of the log (i.e [LEFT], [UP] and
	 * [LEFT] will all be added if the user presses [LEFT] [UP] [LEFT], but only
	 * [LEFT] [UP] will be added if they type [LEFT] [UP] [UP]. <br>
	 * <br>
	 * Every time {@link #tick()} is called, the oldest keystroke in this log will
	 * be used to set the Direction that the Snake is facing before
	 * {@link #preMove()} is called. Afterwards, this keystroke is removed from the
	 * log.
	 */
	private void addDirectionStroke(int keyCode)
	{
		Direction directionToAdd = null;
		switch (keyCode)
		{
		case VK_UP:
			directionToAdd = Direction.UP;
			break;
		case VK_DOWN:
			directionToAdd = Direction.DOWN;
			break;
		case VK_LEFT:
			directionToAdd = Direction.LEFT;
			break;
		case VK_RIGHT:
			directionToAdd = Direction.RIGHT;
			break;
		default:
			break;
		}

		if (directionToAdd != null)
		{
			if (directionStrokes.size() == 0)
				directionStrokes.add(directionToAdd);
			else if (directionStrokes.get(directionStrokes.size() - 1) != directionToAdd
					&& directionStrokes.size() < directionCacheSize)
				directionStrokes.add(directionToAdd);
		}
	}

	/** The delay that the game timer will have when the cannibal buff is active */
	private int CANNIBAL_BUFF_MODIFIED_DELAY = 85;

	/**
	 * Constant that defines the delay of the buff timer. Set to 8 times a second.
	 * Do not change, unless you want to change the blink array to appropriately
	 * match the intervals
	 */
	private static final int BUFF_TIMER_DELAY = 125; // The buff clock ticks 8 times a second //

	/**
	 * The remaining buff times at which there will be a 'blink' to indicate that
	 * any given buff is about to run out
	 */
	private static final double[] BLINK_ARRAY = new double[] { 0.125, 0.375, 0.625, 0.875, 1.125, 1.375, 1.625, 1.875,
			2.125, 2.375, 2.625, 2.750, 3, 3.125, 3.625, 3.750, 3.875, 4.625, 4.750, 4.875, 5 };

	/**
	 * Derived from {@link #BLINK_ARRAY}, this ArrayList contains all of the times
	 * in which, when a buff is about to run out (has less than 6 seconds
	 * remaining), there won't be a blink. Between all of these times, however there
	 * will be a blink where the snake color flashes back to its original color. The
	 * purpose of this blink is to warn the player that the buff is going to run out
	 * soon. The closer the buff is to running out, the faster the blink
	 */
	private static final ArrayList<Double> BLINK_TIMES = arrayToList(BLINK_ARRAY);

	private boolean buffsActive = false;

	/**
	 * This value switches between true and false whenever the remaining time of the
	 * cannibal buff appears in {@link #BLINK_ARRAY}
	 */
	private boolean cannibalBlink = false;

	private double cannibalFruitBuffTimeWorth = 15;
	private double remainingCannibalSeconds = 0;
	private boolean cannibalBuff = false;

	public double getRemainingCannibalSeconds()
	{
		return remainingCannibalSeconds;
	}

	/**
	 * Inner BuffClock class used for ticking all of the buff timers and changing
	 * the state of the game based on which buffs are active. For example if the
	 * cannibal buff is active then it will set {@link #cannibalBuff} to true.
	 * 
	 * @author yeatesg
	 */
	class BuffClock implements ActionListener
	{
		/**
		 * This method is used to call the methods to tick down all buff timers and call
		 * the {@link #updateBuffStatusField()} method. This method is called every
		 * BUFF_TIMER_DELAY milliseconds.
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			doCannibalBuffTick();
			updateBuffStatusField();
		}

		/**
		 * Updates the buffsActive field depending on whether or not there are any
		 * active buffs If any buffs are active, buffsActive will be true. If no buffs
		 * are active it will be set to false
		 */
		private void updateBuffStatusField()
		{
			if (cannibalBuff == false)
				buffsActive = false;
			else
				buffsActive = true;
		}

		/**
		 * Ticks down the remainingCannibalSeconds field by BUFF_TIMER_DELAY
		 * milliseconds. If there are more than 0 remaining seconds on the buff, then
		 * this method will also change the status of the cannibalBuff field to true. If
		 * there are less than 6 seconds left on the buff, it will change the
		 * cannibalBlink field to true depending on if remainingCannibalSeconds is
		 * between any two intervals in the BLINK_TIMES list
		 */
		public void doCannibalBuffTick()
		{
			if (remainingCannibalSeconds > 0)
			{
				cannibalBuff = true;
				remainingCannibalSeconds -= (double) BUFF_TIMER_DELAY / 1000.00;
				if (remainingCannibalSeconds < 6)
				{
					if (BLINK_TIMES.contains(remainingCannibalSeconds))
						cannibalBlink = false;
					else
						cannibalBlink = true;
				} else
					cannibalBlink = false;
			}
			else
			{
				cannibalBlink = false;
				cannibalBuff = false;
			}
		}

	}

	/**
	 * Map that is used to determine what indexes of the Snake have something
	 * digesting in it. The way this map is formatted is by "r,g,b,i,ID". r g and b
	 * represent the respective rgb values of the color that is being digested. i
	 * represents the index in segmentLocations that this stomach content exists at,
	 * and ID is the unique digestion id that this token has. No two digestions have
	 * the same ID, and multiple things can be digesting at the same index in
	 * segmentLocations (which is why this isn't a HashMap).
	 */
	private ArrayList<String> digestMap = new ArrayList<String>();

	/**
	 * Value that is assigned to any token in the {@link #digestMap} whenever it is
	 * added to the map. This value is increased by one for every value that is
	 * added to the map
	 */
	private static int digestID = 0;

	/**
	 * Adds the given Color to the digestion map. It will start digesting at the
	 * head index of the Snake - 1
	 * @param c the Color to be added to the digestion queue
	 */
	private void eat(Color c)
	{
		int index = segmentLocations.size() - 1;
		digestMap.add(c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + index + "," + digestID);
		digestID++;
	}

	/**
	 * Adds multiple instances of one Color to the digestion map. For each color
	 * that is added, the index in {@link #segmentLocations} that the next color
	 * will be put in is shifted up by one. For example if this was called with 3 as
	 * the second parameter then the respective locations of each color are n, n+1,
	 * and n+2
	 * 
	 * @param c
	 * @param amount
	 */
	private void eatMultiple(Color c, int amount)
	{
		int startIndex = segmentLocations.size() + 1;
		for (int i = 0; i < amount; i++, startIndex++)
		{
			digestMap.add(c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + startIndex + "," + digestID);
		}

	}

	/**
	 * How many indexes should each digestion instance be moved down by in
	 * {@link #segmentLocations} whenever digest() is called ? This number gets
	 * higher as the snake grows larger to it doesn't take too long to digest
	 * everything
	 */
	private double digestRate;

	/**
	 * Runs a digestion cycle that pushes all contents of the {@link #digestMap}
	 * down by {@link #digestRate indexes in the {@link #segmentLocations} map. If a
	 * given Color reaches the end of the snake (index 0 in
	 * {@link #segmentLocations}), then it will be removed from the digestion map
	 * and the Snake will grow another segment the next time
	 * {@link #move(ArrayPoint)} is called.
	 */
	private void digest()
	{
		digestRate = 1.5 + segmentLocations.size() * 0.0075;

		ArrayList<String> removeAfter = new ArrayList<>();
		for (int index = 0; index < digestMap.size(); index++)
		{
			String s = digestMap.get(index);
			String[] split = s.split(",");
			int r = Integer.parseInt(split[0]), g = Integer.parseInt(split[1]), b = Integer.parseInt(split[2]);
			double segmentIndex = Double.parseDouble(split[3]);
			int digestID = Integer.parseInt(split[4]);

			segmentIndex -= digestRate;

			if (segmentIndex > 0)
			{
				digestMap.set(index, r + "," + g + "," + b + "," + segmentIndex + "," + digestID);
			} else
			{
				removeAfter.add(r + "," + g + "," + b + "," + (segmentIndex + digestRate) + "," + digestID);
				segmentsToBeAdded++;
			}
		}

		for (String s : removeAfter)
		{
			digestMap.remove(s);
		}
	}

	/**
	 * Returns the color that is being digested at this index of the Snake. Returns
	 * null if nothing is being digested at this index
	 * 
	 * @param index the index to check for digestion at
	 * @return the Color that is being digested here, or null if nothing is being
	 *         digested
	 */
	public Color digestingAt(int index)
	{
		Color spectrumColor = segmentLocations.get(segmentLocations.at(index));
		for (String s : digestMap)
		{
			String[] split = s.split(",");
			int i = (int) (Double.parseDouble(split[3]));
			if (index == i)
			{
				double dR = Integer.parseInt(split[0]), dG = Integer.parseInt(split[1]), dB = Integer.parseInt(split[2]);
				double r = spectrumColor.getRed(), g = spectrumColor.getGreen(), b = spectrumColor.getBlue();

				if (cannibalBuff)
					return Color.DARK_GRAY;

				r = (r * 0.8 + dR * 0.2);
				g = (g * 0.8 + dG * 0.2);
				b = (b * 0.8 + dB * 0.2);

				return new Color((int) r, (int) g, (int) b);

			}
		}
		return null;
	}

	/**
	 * Determines whether anything is being digested at the given index of the snake
	 * 
	 * @param index the index in {@link #segmentLocations} to check for digestion
	 * @return true if something is being digested here
	 */
	public boolean isDigestingAt(int index)
	{
		return (digestingAt(index) == null) ? false : true;
	}

	/**
	 * Decimal value that represents the progress of the game. Updated every game
	 * tick by the {@link #updateProgress()} method
	 */
	private double progress;
	/**
	 * Similar to progress, but will reach the value of 1.00 (100%) faster depending
	 * on the {@link #progressPercentTillTopSpeed} field. See
	 * {@link #updateProgress()}
	 */
	private double speedProgress;

	/**
	 * Speeds up the Snake based on the progress of the game. As the progress of the
	 * game approaches 100%, the timer delay approaches minDelay. This process can
	 * be sped up by changing the value of {@link #progressPercentTillTopSpeed}. For
	 * example, if this value is set to 75%, then the delay will approach minDelay
	 * as the progress approaches 75% (instead of 100%) This means that the Snake
	 * will reach its max speed when the player covers 75% of the map with snake.
	 */
	private void updateDelay()
	{

		int diff = startingDelay - endingDelay;
		delay = (int) (startingDelay - (diff * speedProgress));
		
		if (cannibalBuff)
		{
			delay *= 0.66;
		}
		
		if (speedBoost)
		{
			delay *= 0.66;
		}
		
		timer.setDelay(delay);
	}

	/**
	 * Obtains the current delay of the timer
	 * 
	 * @return the delay of the timer, as an integer
	 */
	public int getDelay()
	{
		return delay;
	}

	/**
	 * Returns 1000 milliseconds (1 second) divided by the delay (in milliseconds).
	 * this value represents the number of ticks per second, or the speed of the
	 * snake
	 * 
	 * @return the speed of the snake, in ticks/squares-moved per second
	 */
	public double getSpeed()
	{
		return (double) 1000.0 / delay;
	}

	/**
	 * Updates the {@link #progress} of the game. This is based on how many tiles
	 * are covered by snake compared to the total amount of tiles (excluding the
	 * border) <br>
	 * <br>
	 * Also updates the {@link #speedProgress} of the game. This is based on how
	 * many tiles are covered by the snake compared to (total amount of tiles) *
	 * ({@link #progressPercentTillTopSpeed} / 100). If
	 * {@link #progressPercentTillTopSpeed} is equal to 100, then this value will be
	 * exactly the same as the real game progress.
	 * 
	 */
	private void updateProgress()
	{
		progress = (double) numSquaresFilled / (double) coverableArea;
		speedProgress = (double) numSquaresFilled
				/ (double) (coverableArea * ((double) progressPercentTillTopSpeed / 100.00));
		if (speedProgress > 1)
			speedProgress = 1;
	}

	/**
	 * Returns the progress (multiply return value by 100 to convert to percent)
	 * 
	 * @return the current progress, as a decimal
	 */
	public double getProgress()
	{
		return progress;
	}

	/**
	 * Returns the number of squares that have been filled in this game
	 * 
	 * @return a double precision number representing {@link #numSquaresFilled}
	 */
	public double getSquaresFilled()
	{
		return numSquaresFilled;
	}

	/**
	 * Determines whether or not a cannibal fruit should spawn, depending on the
	 * game progress coupled with a random chance every time a regular fruit is
	 * picked up. If a cannibal fruit should spawn, it will find an empty space in
	 * the {@link #map} for it and set the value in the map array to "E"
	 **/
	private void addCanniFruitToMap()
	{
		Random rand = new Random();
		if (rand.nextInt(15) == 0 && progress > 0.3)
		{
			boolean found = false;
			while (!found)
			{
				ArrayPoint randomPoint = new ArrayPoint(rand.nextInt(numVerticalSquares),
						rand.nextInt(numHorizontalSquares));
				if (map[randomPoint.getY()][randomPoint.getX()].equals(" "))
				{
					map[randomPoint.getY()][randomPoint.getX()] = "E";
					found = true;
				}
			}
		}

	}

	/**
	 * Generates a new fruit element in the {@link #map} array. The fruit will be
	 * placed in an empty space, and the value of it will be set to "F". This method
	 * also determines if the game should end, by checking to see if the progress
	 * has reached 1.00 before spawning in the fruit
	 */
	private void addFruitToMap()
	{
		if (progress < 1)
		{
			addCanniFruitToMap();
			Random rand = new Random();
			boolean found = false;
			while (!found)
			{
				ArrayPoint randomPoint = new ArrayPoint(rand.nextInt(numVerticalSquares),
						rand.nextInt(numHorizontalSquares));
				if (map[randomPoint.getY()][randomPoint.getX()].equals(" "))
				{
					map[randomPoint.getY()][randomPoint.getX()] = "F";
					found = true;
				}
			}
		} else
		{
			dead = true;
			System.out.println("You win!");
		}
	}

	/**
	 * Every {@link #tick()}, or more specifically, every time
	 * {@link #move(ArrayPoint)} is called, the program checks to see if this value
	 * is greater than 0. If this is the case, it will subtract one from this field
	 * and add a new segment to the snake. Whenever {@link #digest()} is called and
	 * a stomach content reaches the end of the segmentLocations map (that is,
	 * whenever the stomach content reaches below index 0 in
	 * {@link #segmentLocations}, this field will be raised by 1 which will add a
	 * segment next tick. <br>
	 * <br>
	 * Note: {@link #digest()} is called before {@link #move(ArrayPoint)} in the
	 * tick method <br>
	 * <br>
	 * See {@link #digest()} and {@link #move(ArrayPoint)}
	 **/
	private int segmentsToBeAdded = 0;

	private void clearSegmentsFromMap()
	{
		for (String s : segmentLocations.keySet())
		{
			ArrayPoint p = ArrayPoint.fromString(s);
			map[p.getY()][p.getX()] = " ";
		}
	}

	/**
	 * Adds the Snake segments from {@link #segmentLocations} into the {@link #map}.
	 * The body segments will be added as "B" in the map and the head will be added
	 * as "H"
	 */
	private void addSegmentsToMap()
	{
		for (int i = 0; i < segmentLocations.size(); i++)
		{
			ArrayPoint p = segmentLocations.at(i);
			if (i < segmentLocations.size() - 1)
				map[p.getY()][p.getX()] = "B";
			else
				map[p.getY()][p.getX()] = "H";
		}
		repaint();
	}

	/**
	 * Paints the map, with the color of each different type of element in it based
	 * on the fields at the beginning of the class. For example "+" (the border
	 * element) will be painted as a square with the color {@link #borderColor}.
	 */
	@Override
	public void paint(Graphics g)
	{
		g.setColor(spaceColor);
		g.fillRect(0, 0, preferredWidth(), preferredHeight());
		segmentLocations.setColors();
		drawBorder(g);

		int yPos = (int) (spaceSize * 0.5);
		for (int y = 0; y < map.length; y++)
		{
			int xPos = (int) (spaceSize * 0.5);
			for (int x = 0; x < map[y].length; x++)
			{
				String state = map[y][x];

				switch (state)
				{
				case " ":
					g.setColor(spaceColor);
					g.fillRect(xPos, yPos, squareSize, squareSize);
					break;
				case "B":
				case "H":
					drawSegment(g, new ArrayPoint(y, x), xPos, yPos);
					break;
				case "F":
					g.setColor(fruitColor);
					g.fillRect(xPos, yPos, squareSize, squareSize);
					break;
				case "E":
					g.setColor(cannibalFruitColor);
					g.fillRect(xPos, yPos, squareSize, squareSize);
					break;
				}

				xPos += squareSize + spaceSize;
			}

			yPos += squareSize + spaceSize;
		}
	}

	private void drawBorder(Graphics g)
	{
		g.setColor(borderColor);
		g.fillRect(0, 0, preferredWidth(), preferredHeight());

		int gap = (int) (squareSize + spaceSize); // The space between the 2 borders

		// Fill the gameplay area with spaceColor
		g.setColor(spaceColor);
		g.fillRect(gap + 1, gap + 1, preferredWidth() - 2 * gap - 1, preferredHeight() - 2 * gap - 1);

		// Inner border
		g.setColor(Color.WHITE);
		g.drawRect(gap, gap, preferredWidth() - 2 * gap, preferredHeight() - 2 * gap);

		// Outer border
		g.setColor(Color.WHITE);
		g.drawRect(0, 0, preferredWidth(), (int) (preferredHeight()));

		g.setColor(Color.BLACK);
		g.fillRect(preferredWidth() + 1, 0, frame.getWidth() - (preferredWidth() + 1), frame.getHeight());
		g.fillRect(0, preferredHeight() + 1, frame.getWidth(), 10);
	}

	/**
	 * Algorithm that draws the segment at the provided ArrayPoint p. The color of
	 * the segment is determined by many factors including what is being digested at
	 * that segment, whether or not the Snake has buffs active, and whether or not
	 * the Snake is dead.
	 * 
	 * @param graphics the Graphics instance that will be drawing this segment
	 * @param p        the ArrayPoint that this Segment is located at in the
	 *                 2-Dimensional string array that runs this game
	 * @param xPos     the x-position of the square that will be drawn in the game
	 *                 GUI
	 * @param yPos     the y-position of the square that will be drawn in the game
	 *                 GUI
	 */
	private void drawSegment(Graphics graphics, ArrayPoint p, int xPos, int yPos)
	{
		Color spectrumColor = segmentLocations.get(p.toString());
		graphics.setColor(spectrumColor);

		if (cannibalBuff)
		{
			double r = spectrumColor.getRed(), g = spectrumColor.getGreen(), b = spectrumColor.getBlue();

			r = r += 180;
			g = g += 180;
			b = b += 180;

			if (r > 255 || r < 0)
				r = (r > 255) ? 255 : 0;
			if (g > 255 || g < 0)
				g = (g > 255) ? 255 : 0;
			if (b > 255 || b < 0)
				b = (b > 255) ? 255 : 0;

			graphics.setColor(new Color((int) r, (int) g, (int) b));

			if (cannibalBlink)
				graphics.setColor(spectrumColor);
		}

		int index = segmentLocations.indexOf(p);

		int squareSize = this.squareSize;

		if (isDigestingAt(index))
		{
			graphics.setColor(digestingAt(index));

			int squareSizeIncrement = (int) (spaceSize - 2);
			if (squareSizeIncrement < 0)
				squareSizeIncrement = 0;

			int posDecrement = (int) ((double) squareSizeIncrement / 2.00);
			xPos -= posDecrement;
			yPos -= posDecrement;
			squareSize += squareSizeIncrement;
		}

		if (dead && index == headIndex)
			graphics.setColor(deadColor);

		graphics.fillRect(xPos, yPos, squareSize, squareSize);
	}

	/**
	 * Converts any double[] array into an ArrayList<Double>.
	 * 
	 * @param array the array to be converted
	 * @return an ArrayList<Double> containing the contents of the parameter array
	 */
	public static ArrayList<Double> arrayToList(double[] array)
	{
		ArrayList<Double> list = new ArrayList<Double>();
		for (double d : array)
			list.add(d);
		return list;
	}

	/**
	 * Prints any given two dimensional String array. Generally used for printing
	 * map.
	 * 
	 * @param map The array that is to be printed
	 */
	public static void printArray(String[][] map)
	{
		for (int y = 0; y < map.length; y++)
		{
			for (int x = 0; x < map[y].length; x++)
			{
				System.out.print(map[y][x] + " ");
			}

			System.out.println();
		}
	}

	/**
	 * Creates and returns a map of the given height and width. Sets every String in
	 * this map to " ", besides the border which is set to "+".
	 * 
	 * @param height The height of this map
	 * @param width  The width of this map
	 * @return A two dimensional String array representing a map that was created
	 *         with the specified height and width
	 */
	public static String[][] createMap(int height, int width)
	{
		String[][] map = new String[height][width];

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				map[y][x] = " ";

				if (y == 0 || y == height - 1)
				{
					map[y][x] = "+";
				}

				if (x == 0 || x == width - 1)
				{
					map[y][x] = "+";
				}
			}
		}
		return map;
	}

	/**
	 * Initializes the fields that determines the Color of the fruits for this game.
	 * The color of the fruits may change depending on the front and back colors of
	 * the Snake
	 */
	private void initFruitColors()
	{
		int frontR = frontColor.getRed();
		int frontG = frontColor.getGreen();
		int frontB = frontColor.getBlue();

		int canniFruitR = frontR + 200;
		int canniFruitG = frontG + 200;
		int canniFruitB = frontB + 200;

		if (canniFruitR > 255 || canniFruitR < 0)
			canniFruitR = (canniFruitR > 255) ? 255 : 0;
		if (canniFruitG > 255 || canniFruitG < 0)
			canniFruitG = (canniFruitG > 255) ? 255 : 0;
		if (canniFruitB > 255 || canniFruitB < 0)
			canniFruitB = (canniFruitB > 255) ? 255 : 0;

		cannibalFruitColor = new Color(canniFruitR, canniFruitG, canniFruitB);

		fruitColor = backColor;
	}

	/**
	 * Prints information that may be useful to someone who is debugging this
	 * program
	 */
	public void debug()
	{
		System.out.println("Digest Rate: " + digestRate);
		System.out.printf("Progress: %.2f", progress * 100);
		System.out.print("%\n");
		System.out.println("Fruit worth: " + foodSegmentWorth);
	}

	/**
	 * Called whenever the user resizes the window. This method adjusts the
	 * windowWidth and windowHeight fields according to the new height and width
	 * that the user set it to. Every few time intervals,
	 * {@link #updateSpatialFields()} is called by the {@link #tick()} method, which
	 * will update the spatial fields based on the new values of these fields.
	 */
	public void onResize()
	{
		System.out.println("ddd");
		windowWidth = this.preferredWidth();
		windowHeight = this.preferredHeight();
		updateSpatialFields(false);
	}

	/**
	 * Sets the size of the frame depending on the {@link #preferredWidth} and
	 * {@link #preferredHeight} fields.
	 */
	private void setPreferredSize()
	{
		frame.doWindowBuilderStuff(this);
		setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		setSize(new Dimension(preferredWidth, preferredHeight));
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Returns the preferred width of this JPanel
	 * 
	 * @return the preferred width
	 */
	public int preferredWidth()
	{
		return preferredWidth;
	}

	/**
	 * Returns the preferred height of this JPanel
	 * 
	 * @return the preferred height
	 */
	public int preferredHeight()
	{
		return preferredHeight;
	}

	/**
	 * Since this class implements ActionListener, it also works as a timer. Every
	 * time the timer loops, this method calls the {@link #tick()} method within
	 * this class. Why not get rid of the tick method and just paste its contents
	 * here, you ask? Well it's not your fucking program so you can walk your little
	 * spastic ass out of my house. And if you have a problem with that, go talk to
	 * your fucking guidance counselor. And it that's not enough for you, go rope.
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		tick();
	}
	
	private boolean speedBoost = false;

	/**
	 * Whenever the user presses any key on the keyboard, call addDirectionStroke
	 * with that key as the parameter
	 */
	@Override
	public void keyPressed(KeyEvent e)
	{
		if (arrContains(new Integer[] { VK_DOWN, VK_LEFT, VK_RIGHT, VK_UP }, e.getKeyCode()))
		{
			addDirectionStroke(e.getKeyCode());		
		}
		
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			speedBoost = true;
		}
	}
	
	public static boolean arrContains(Object[] arr, Object content)
	{
		for (Object o : arr)
		{
			if (o.equals(content)) return true;
		}
		return false;
	}

	public void keyTyped(KeyEvent e)
	{
	}

	public void keyReleased(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			speedBoost = false;
		}	
	}

	public void componentHidden(ComponentEvent e)
	{
	}

	public void componentMoved(ComponentEvent e)
	{
	}

	public void componentResized(ComponentEvent e)
	{
		// onResize();
	}

	@Override
	public void componentShown(ComponentEvent e)
	{
	}
}
enum Direction
{
	UP, DOWN, LEFT, RIGHT;
}