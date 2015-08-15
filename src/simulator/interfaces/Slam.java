/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, Adelaide Uni.
*/

package simulator.interfaces;

public interface Slam{
	
	/*Given a list containing sensor points (metres, metres - pixel coordinates), this function
	will determine the location of old and new landmarks relative to the vehicle. From this, an 
	estimate for vehicle movement since last call can be made. */
	public void addPoints(double[] points);
	
	/*This function	will determine the location of old and new landmarks relative to the vehicle.
	From this, an estimate for vehicle movement since last call can be made. Potentially, other 
	localisation sensors will have information to be used. These will either be	static (such as 
	GPS), or a relative change since our last position (such as an Encoder). A dynamic number of 
	these estimates can be provided, but they will all be in the form x, y (metres, metres - pixel 
	coordinates).
	Returns the final estimate for vehicle position in the form x,y (metres, metres - pixel 
	coordinates)*/
	public double[] calculatePosition(int numStatic, double[] staticEstimates,
								int numRelative, double[] relativeEstimates);
								
	
	
	
}
