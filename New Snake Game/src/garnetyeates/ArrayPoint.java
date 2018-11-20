package garnetyeates;
/**
 * An instance of this class represents a point (y, x) in a two dimensional
 * Array.
 * 
 * @author yeatesg
 */
public class ArrayPoint
{
	/** The y index of this point in the two dimensional array */
	private int y;
	/** The x index of this point in the two dimensional array */
	private int x;

	/**
	 * Constructs a new ArrayPoint at the specified location
	 * 
	 * @param y the y index of this ArrayPoint
	 * @param x the x index of this ArrayPoint
	 */
	public ArrayPoint(int y, int x)
	{
		this.y = y;
		this.x = x;
	}

	/**
	 * Converts this ArrayPoint to a String, formatted as "y,x"
	 */
	@Override
	public String toString()
	{
		return y + "," + x;
	}

	/**
	 * @return the x-index of this ArrayPoint
	 */
	public int getX()
	{
		return x;
	}

	/**
	 * @return the y-index of this ArrayPoint
	 */
	public int getY()
	{
		return y;
	}

	/**
	 * Constructs an ArrayPoint from the given String and returns it.
	 * 
	 * @param s The string that should be converted to an ArrayPoint
	 * @return An ArrayPoint created from the String that the user entered as the
	 *         parameter
	 */
	public static ArrayPoint fromString(String s)
	{
		String[] split = s.split(",");
		return new ArrayPoint(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
	}
}
