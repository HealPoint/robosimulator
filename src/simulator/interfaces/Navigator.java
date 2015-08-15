/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, Adelaide Uni.
*/

package simulator.interfaces;

import java.util.List;
import simulator.interfaces.NavigatorNode;

public interface Navigator{
	
	/* The class name. */
	public String getName();
	
	/*Given a 2D grid containing obstacle information:
	0 = no obstacle
	1+ = obstacle (the higher the value the more likely this object is to contain an obstacle),
	this function returns the list of nodes (in order) to be traversed to reach the goal
	destination from the start point. 
	
	The grid is accessed by grid[col number][row number], so the start position is at
	grid[startTileX][startTileY].
	
	If a path is unable to be found, this function should return an empty List. */
	public List<NavigatorNode> calculatePath(int[][] grid, 
											int startTileX, 
											int startTileY, 
											int destTileX, 
											int destTileY,
											double startAngle);
	
	
	
}
