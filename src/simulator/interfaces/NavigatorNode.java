/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, Adelaide Uni.
*/

package simulator.interfaces;

import java.util.List;

/** This interface specifies the requirements of the nodes to be used for a Navigator.
Each position in the 2D map is to be represented by a Node.
This was not made its own class as certain methods may require additional functionality
for their nodes. **/
public interface NavigatorNode{
	
	//Returns the Node position in the grid
	public int getX();
	public int getY();
	
	
	
}
