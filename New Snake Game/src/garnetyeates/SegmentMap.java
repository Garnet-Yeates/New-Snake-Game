package garnetyeates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * A SegmentMap is an instance of a LinkedHashMap with a String key and Color
 * value. The string key represents an ArrayPoint. The string is formatted as
 * "y,x", with y being {@link ArrayPoint#getY()} and x being
 * {@link ArrayPoint#getX()}. The reason why it uses String instead of
 * ArrayPoint is to make it easier to test equality. Now that I look at it, I am
 * an idiot for converting it for a string and whenever I have time I am going
 * to make it use ArrayPoints. The reason why the SegmentMap is a HashMap is
 * because I found it easier to pair each segment with a respective color to
 * easily access the Color of any segment point in the map, especially since the
 * Color at each point is constantly changing as a result of the unique Color
 * fading that this Snake game has
 * 
 * @author yeatesg
 */
public class SegmentMap extends LinkedHashMap<String, Color>
{
	private static final long serialVersionUID = 1L;

	// size() - 1 is front, 0 is back
	private HashMap<Color, Integer> fadeMap = new LinkedHashMap<Color, Integer>();
	private Color[] colors;

	/**
	 * Initializes this SegmentMap with a fadeMap that is determined by the
	 * parameter if this constructor
	 * 
	 * @param colors An array of colors that will determine the fadeMap of this
	 *               SegmentMap. The first index will be the back color of the
	 *               Snake, and the last index will be the front. There can be
	 *               multiple colors in the fadeMap. For example if there were 3
	 *               colors in the fadeMap, the back of the snake will be index 0 of
	 *               the fade map, the middle will be index 1 and the front will be
	 *               index 2.
	 */
	public SegmentMap(Color[] colors)
	{
		this.colors = colors;
		for (Color c : colors)
			addFadeColor(c);
	}
	
	public int move(ArrayPoint newHeadLoc, boolean addSegment)
	{
		int ΔheadIndex = 0;
		SegmentMap newSegmentLocations = this.cloneEmpty();

		if (addSegment)
		{
			newSegmentLocations.add(at(0)); // Append the old tail location into the new segment locations
			ΔheadIndex++;				        // list if addSegment = true (i.e the snake finished digesting something)
		}

		for (int i = 0; i < size() - 1; i++)
		{
			newSegmentLocations.add(at(i + 1)); // Move every segment location to the location of the segment in front of it

		}

		/*
		 * Remove in case newHeadLoc is equivalent to another body segment location (i.e
		 * the snake hit itself). This makes it so that instead of replacing the body
		 * segment key with newHeadLoc, it adds newHeadLoc to the end of the list so
		 * that it's actually at the head location.
		 */
		if (newSegmentLocations.containsKey(newHeadLoc.toString()))
		{
			ΔheadIndex--;
			newSegmentLocations.remove(newHeadLoc.toString());
		}

		newSegmentLocations.add(newHeadLoc); // Add the new head location to the end of the newSegmentLocations list since it wasn't added yet
		
		for (String s : getKeySetArray())
			remove(s);
		
		for (String s : newSegmentLocations.getKeySetArray()) // Move all keys from newSegmentLocations into this map
			put(s, newSegmentLocations.get(s));
				
		setColors();
		
		return ΔheadIndex;
	}

	/**
	 * Sets the Colors of every key in this SegmentMap. These colors depend on the
	 * colors that are in fadeMap. For example if the fadeMap includes { BLUE, RED
	 * }, the first index in this SegmentMap will be assigned to blue and over the
	 * span of all of the indexes in the SegmentMap, this Color will gradually
	 * change to red until it reaches the last index in SegmentMap, in which it will
	 * be red. If there were three Colors in the fadeMap, { BLUE, GREEN, RED }, it
	 * will fade from blue to green over index 0 to the halfway-index of the
	 * SegmentMap. Then from the halfway-index to the last index, it will fade from
	 * green to red. This makes the snake very pretty
	 */
	public void setColors()
	{
		updateFadeMapRanges();

		Color[] colorArray = getFadeKeySetArray();

		int i = 0, j = 1;
		do
		{
			Color startColor = colorArray[i];
			Color endColor = colorArray[j]; // Get the starting Color and ending Color

			int startIndex = fadeMap.get(colorArray[i]);
			int endIndex = fadeMap.get(colorArray[j]); // Get the indexes in segmentMap of the start color and the end
																		// color

			int range = endIndex - startIndex; // How many segments are there between the two colors?

			double r = startColor.getRed();
			double g = startColor.getGreen();
			double b = startColor.getBlue();

			double[] changeRateArray = getColorChangeRate(startColor, endColor, range);
			double rChangeRate = changeRateArray[0];
			double gChangeRate = changeRateArray[1];
			double bChangeRate = changeRateArray[2];

			String[] segmentLocations = getKeySetArray();

			for (int l = startIndex; l <= endIndex; l++)
			{
				this.put(segmentLocations[l], new Color((int) r, (int) g, (int) b));

				r += rChangeRate;
				g += gChangeRate;
				b += bChangeRate;
			}

			i++;
			j++;
		} while (j < fadeMap.size());
	}

	/**
	 * Calculates how much the RGB values of a given startColor should change each
	 * index if it is going to become endColor over a span of indexes determined by
	 * range.
	 * 
	 * @param startColor The initial color
	 * @param endColor   The color that this color is going to fade into
	 * @param range      The range over which this Color change is going to occur.
	 *                   The longer the range, the more gradual the color change
	 * @return A double array containing the change rate of the RGB values. [0] =
	 *         red, [1] = green, [2] = blue
	 */
	private double[] getColorChangeRate(Color startColor, Color endColor, int range)
	{
		double startR = startColor.getRed();
		double startG = startColor.getGreen();
		double startB = startColor.getBlue();

		double endR = endColor.getRed();
		double endG = endColor.getGreen();
		double endB = endColor.getBlue();

		double rChangeRate = (endR - startR) / (range + 0);
		double gChangeRate = (endG - startG) / (range + 0);
		double bChangeRate = (endB - startB) / (range + 0);

		double[] changeRateArray = new double[3];
		changeRateArray[0] = rChangeRate; // r
		changeRateArray[1] = gChangeRate;
		changeRateArray[2] = bChangeRate;

		return changeRateArray;

	}

	/**
	 * Updates the SegmentMap indexes associated with each Color in the
	 * {@link #fadeMap}. If there are only two colors in the fadeMap, these indexes
	 * will always be the same (0 and size() - 1). But if there are more than two
	 * colors in the fade map (say 3, for example), the third Color needs to be
	 * associated with the middle index of the SegmentMap.
	 */
	private void updateFadeMapRanges()
	{
		int fadeMapSize = fadeMap.size();

		double numerator = 0;
		double rangePercent = numerator / ((double) fadeMapSize - 1);

		for (Color c : fadeMap.keySet())
		{
			if (rangePercent < 1)
			{
				fadeMap.put(c, (int) (rangePercent * size()));
				rangePercent = ++numerator / ((double) fadeMapSize - 1);
			} else
			{
				fadeMap.put(c, size() - 1);
			}
		}

	}

	/**
	 * Similar to {@link LinkedHashMap#get(Object)}, but allows you to use an
	 * ArrayPoint as the parameter instead of a String. It does this by converting
	 * the ArrayPoint to a String before adding it to the map
	 * 
	 * @param p The ArrayPoint to get the value of from the SegmentMap
	 * @return The Color associated with this ArrayPoint in the SegmentMap///
	 */
	public Color get(ArrayPoint p)
	{
		return get(p.toString());
	}

	/**
	 * Exactly the same as {@link LinkedHashMap#put(Object, Object)}, but instead of
	 * putting a String you can put an ArrayPoint which will be converted to a
	 * String automatically
	 * 
	 * @param p The ArrayPoint to put into the SegmentMap
	 * @param c The color that this ArrayPoint will be associated with
	 * @return the Color that was put into the map
	 */
	public Color put(ArrayPoint p, Color c)
	{
		return super.put(p.toString(), c);
	}

	/**
	 * Puts the given ArrayPoint in this SegmentMap with null as the value. First
	 * converts the ArrayPoint to a string, since this map has a String key set.
	 * <br>
	 * <br>
	 * Similar to ArrayList add, except it will replace if this element already
	 * exists in this SegmentMap
	 * 
	 * @param p The ArrayPoint that is being added
	 */
	public void add(ArrayPoint p)
	{
		put(p.toString(), null);
	}

	/**
	 * Similar to {@link ArrayList#get(int)}, except this is for a LinkedHashMap
	 * instead of an ArrayList
	 * 
	 * @param index the index
	 * @return
	 */
	public ArrayPoint at(int index)
	{
		String[] segmentLocations = getKeySetArray();
		return ArrayPoint.fromString(segmentLocations[index]);
	}

	/**
	 * Similar to {@link ArrayList#indexOf(Object)}. Obtains the index of this
	 * String in the key set.
	 * 
	 * @param s the String to find the index of
	 * @return the index of String s in {@link SegmentMap#getKeySetArray()}
	 */
	public int indexOf(String s)
	{
		int index = -1;
		String[] pointStringArray = getKeySetArray();
		for (index = 0; index < pointStringArray.length; index++)
			if (pointStringArray[index].equals(s))
				break;
		return index;
	}

	/**
	 * Similar to {@link ArrayList#indexOf(Object)}. Obtains the index of this
	 * ArrayPoint in the key set. The ArrayPoint is first converted to a String
	 * since segment maps take String keys to represent array points
	 * 
	 * @param p the ArrayPoint to find the index of
	 * @return the index of ArrayPoint p in {@link SegmentMap#getKeySetArray()}
	 */
	public int indexOf(ArrayPoint p)
	{
		return indexOf(p.getY() + "," + p.getX());
	}

	/**
	 * Returns an empty clone of this SegmentMap. This means that the key set of
	 * this SegmentMap is empty but the {@link #fadeMap} is copied over.
	 * 
	 * @return an empty clone of this SegmentMap
	 */
	public SegmentMap cloneEmpty()
	{
		SegmentMap clone = new SegmentMap(colors);
		for (Color c : fadeMap.keySet())
		{
			clone.addFadeColor(c);
		}
		return clone;
	}

	/**
	 * Adds a color to the end of the fade map
	 * 
	 * @param color the Color to add to the fade map
	 */
	public void addFadeColor(Color color)
	{
		fadeMap.put(color, null);
		colors = getFadeKeySetArray();
	}

	/**
	 * Converts the {@link #keySet()} of this SegmentMap to a String array and
	 * returns it. Useful for iterating through the key set of this SegmentMap if
	 * you need to keep track of the indexes using a for loop
	 * 
	 * @return a String array representing the key set of this SegmentMap
	 */
	public String[] getKeySetArray()
	{
		String[] array = new String[keySet().size()];
		int index = 0;
		for (String s : keySet())
		{
			array[index] = s;
			index++;
		}
		return array;
	}

	/**
	 * Converts the key set of the fade map of this SegmentMap to a Color array and
	 * returns it. Useful for iterating through the key set of the fade map if you
	 * need to keep track of the indexes using a for loop
	 * 
	 * @return a Color array representing the key set of this SegmentMap's fade map
	 */
	public Color[] getFadeKeySetArray()
	{
		Color[] colorArray = new Color[fadeMap.keySet().size()];
		int index = 0;
		for (Color col : fadeMap.keySet()) // Converts fadeMap.keySet() into a Color arraY
		{
			colorArray[index] = col;
			index++;
		}
		return colorArray;
	}
}

