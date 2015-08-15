/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, Adelaide Uni.
*/

package simulator.navigators;

import java.lang.Math;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

import simulator.interfaces.Navigator;
import simulator.interfaces.NavigatorNode;

/* This is a Vector Field implementation, where obstacles have repulsive forces, and the goal has
   an attractive force. This implementation includes smoothing with cut limits. */
public class VectorFieldSSW implements Navigator{
	
	private double GOAL_FORCE = 0.5;			//The global attractive force
	private int NUM_STEPS = 8;					//How many steps to plan ahead to
	private int MAX_CUT = 5;					//How many nodes can be cut in a row
			
	public VectorFieldSSW(){
		
	}
	
	@Override
	public String getName(){
		return "VectorFieldSSW";
	}
	
	
	/*Given a 2D grid containing obstacle information:
	0 = no obstacle
	1+ = obstacle (the higher the value the more likely this object is to contain an obstacle),
	this function returns the list of nodes (in order) to be traversed to reach the goal
	destination from the start point. 
	
	The grid is accessed by grid[col number][row number], so the start position is at
	grid[startTileX][startTileY].
	
	If a path is unable to be found, this function should return null. */
	@Override
	public List<NavigatorNode> calculatePath(int[][] grid, int startTileX, int startTileY, 
											int destTileX, int destTileY, double startAngle){
		//System.out.println("Calculating path from ("+startTileX+","+startTileY+") to ("+destTileX+","+destTileY+")");
		

			
		//Declare variables
		LinkedList<Node> obstacles;			//List of all obstructions
		LinkedList<NavigatorNode> returnList;//The list of Nodes in order to be traversed
		boolean isReached;					//Whether we have reached the destination
		int currentPosX;					//X position of current location
		int currentPosY;					//Y position of current location
		int gridSizeX;						//How many columns there are
		int gridSizeY;						//How many rows there are
		
		//Prepare for the search
		obstacles = new LinkedList<Node>();
		returnList = new LinkedList<NavigatorNode>();
		isReached = false;
		currentPosX = startTileX;			
		currentPosY = startTileY;
		gridSizeX = grid.length;
		gridSizeY = grid[0].length;
		grid[destTileX][destTileY] = 0;		//If the destination is obstructed we never find an answer
		
		//If we're already at the destination, return an empty list
		if (currentPosX == destTileX && currentPosY == destTileY){
			return returnList;
		}
		
		//Create the obstacle list
		for (int col=0; col<gridSizeX; col++){
			for (int row=0; row<gridSizeY; row++){
				if (grid[col][row] > 0){
					Node obs = new Node(col, row);		//posX, posY, parent
					obstacles.add(obs);
				}
			}
		}
		
		//Initialise first point
		Node startNode = new Node(currentPosX, currentPosY);
		
		//Begin vector field planning
		Node nextNode = startNode;
		for (int i=0; i<NUM_STEPS; i++){
			int currX = nextNode.getX();
			int currY = nextNode.getY();
			//Calculate overall repulsion direction from obstacles
			double repulseX = 0;
			double repulseY = 0;
			ListIterator<Node> iter = obstacles.listIterator();
			while (iter.hasNext()){
				Node next = iter.next();
				int obsX = next.getX();
				int obsY = next.getY();
				
				// Calculate distance
				double dx = (double)(obsX-currX);
				double dy = (double)(obsY-currY);
				double dist2 = dx*dx+dy*dy;
				if (dist2 == 0 || dist2 > 32){
					continue;
				}
				
				// Calculate angle
				double ang = Math.atan2(dy, dx);
				
				// Calculate force
				double force = 1/dist2;
				double forceX = force*Math.cos(ang);
				double forceY = force*Math.sin(ang);
				repulseX-= forceX;
				repulseY-= forceY;
			}
			
			//Calculate attraction direction from goal
			// Calculate distance
			double dx = (double)(destTileX-currX);
			double dy = (double)(destTileY-currY);
			double dist2 = dx*dx+dy*dy;
			if (dist2 != 0){
				// Calculate angle
				double ang = Math.atan2(dy, dx);
				
				// Calculate force
				double force = GOAL_FORCE;
				double forceX = force*Math.cos(ang);
				double forceY = force*Math.sin(ang);
				repulseX+= forceX;
				repulseY+= forceY;
			}
					
			//System.out.println("Repulse: "+repulseX+","+repulseY);
			
			//Calculate which grid to go to next
			if (Math.abs(repulseX) > Math.abs(repulseY) ){
				if (repulseX > 0){
					nextNode = new Node(currX+1, currY);
				} else {
					nextNode = new Node(currX-1, currY);
				}
			} else {
				if (repulseY > 0){
					nextNode = new Node(currX, currY+1);
				} else {
					nextNode = new Node(currX, currY-1);
				}
			}
			returnList.add(nextNode);
		}
		
		//Add the goal node
		Node goalNode = new Node(destTileX, destTileY);
		returnList.add(goalNode);

		//Smooth the path
		returnList = smoothPath(grid, startTileX, startTileY, returnList);
		
		return returnList;
	}
	
	/* Given a path of Nodes, this function will cut out Nodes that are not needed according to 
	the tutorial found here:
	http://www.gamasutra.com/view/feature/131505/toward_more_realistic_pathfinding.php?page=2 */
	private LinkedList<NavigatorNode> smoothPath(int[][] grid, int startTileX, int startTileY, LinkedList<NavigatorNode> path){

		NavigatorNode checkPoint = null;				//The position of the last way point
		NavigatorNode currentPoint = null;				//The current position
		NavigatorNode nextPoint = null;					//The next position
		LinkedList<NavigatorNode> returnList = new LinkedList<NavigatorNode>(path);	//The list to return
												//It's a copy of the original list, then cuts out bad nodes
		ListIterator<NavigatorNode> iterator;	//Iterates over the path

		iterator = path.listIterator(0);	//Create the iterator
		checkPoint = new Node(startTileX, startTileY);				//First way point is our start position
		if (iterator.hasNext()) currentPoint = iterator.next();		//Current position is the start of the path
		int cutCount = 0;
		while (iterator.hasNext()){
			nextPoint = iterator.next();
			if (isWalkable(checkPoint, nextPoint, grid, 4.0) && cutCount < MAX_CUT){	//currentPoint is safe to remove
				returnList.remove(currentPoint);
				currentPoint = nextPoint;
				cutCount++;
			} else {								//currentPoint is NOT safe to remove
				checkPoint = currentPoint;
				currentPoint = nextPoint;
				cutCount = 0;
			}
		}
		return returnList;
	}
	
	/* Given a start node, end node, grid with obstacle information, and the width of the vehicle (in 
	tiles (max 4.0, any larger and it might jump over an obstacle), this function calculates whether a 
	straight line between start and end crosses any obstacles. */
	private boolean isWalkable(NavigatorNode start, NavigatorNode end, int[][] grid, double width){
		double posX;		//Position of point moving from start to end
		double posY;
		int tileX;			//Integer of position of point
		int tileY;
		int	gridSizeX = grid.length;
		int gridSizeY = grid[0].length;
		
		//Set point at start
		posX = (double)start.getX();
		posY = (double)start.getY();

		//Calculate geometry
		double dx = end.getX() - start.getX();
		double dy = end.getY() - start.getY();
		double dist = Math.sqrt(dx*dx + dy*dy);
		double ang = Math.atan2(dy, dx);
		
		//Calculate increments
		double incX = 0.25 * Math.cos(ang);
		double incY = 0.25 * Math.sin(ang);
		int iterations = (int) (dist / 0.25);
		
		//Iterate
		for (int i=0; i<iterations; i++){
			//Check vehicle
			posX+=incX;
			posY+=incY;
			tileX = (int)(posX+0.5); //round instead of floor
			tileY = (int)(posY+0.5);
			if (grid[tileX][tileY] > 0){
				return false;
			}
			
			//Check soft left of vehicle
			double leftPosX = posX + (width/4.0)*Math.sin(ang);
			double leftPosY = posY + (width/4.0)*Math.cos(ang);
			tileX = (int)(leftPosX+0.5); //round instead of floor
			tileY = (int)(leftPosY+0.5);
			if (tileX >= 0 && tileX < gridSizeX && tileY >= 0 && tileY < gridSizeY){
				if (grid[tileX][tileY] > 0){
					return false;
				}
			}
			
			//Check hard left of vehicle
			leftPosX = posX + (width/2.0)*Math.sin(ang);
			leftPosY = posY + (width/2.0)*Math.cos(ang);
			tileX = (int)(leftPosX+0.5); //round instead of floor
			tileY = (int)(leftPosY+0.5);
			if (tileX >= 0 && tileX < gridSizeX && tileY >= 0 && tileY < gridSizeY){
				if (grid[tileX][tileY] > 0){
					return false;
				}
			}
			
			//Check soft right of vehicle
			double rightPosX = posX - (width/4.0)*Math.sin(ang);
			double rightPosY = posY - (width/4.0)*Math.cos(ang);
			tileX = (int)(rightPosX+0.5); //round instead of floor
			tileY = (int)(rightPosY+0.5);
			if (tileX >= 0 && tileX < gridSizeX && tileY >= 0 && tileY < gridSizeY){
				if (grid[tileX][tileY] > 0){
					return false;
				}
			}
			
			//Check hard right of vehicle
			rightPosX = posX - (width/2.0)*Math.sin(ang);
			rightPosY = posY - (width/2.0)*Math.cos(ang);
			tileX = (int)(rightPosX+0.5); //round instead of floor
			tileY = (int)(rightPosY+0.5);
			if (tileX >= 0 && tileX < gridSizeX && tileY >= 0 && tileY < gridSizeY){
				if (grid[tileX][tileY] > 0){
					return false;
				}
			}
			
		}
		return true;
	}
	
	/********************************************
	* Node Implementation						*
	*********************************************/
	private static class Node implements NavigatorNode{
		private int posX;			//This Node's column number in the grid
		private int posY;			//This Node's row number in the grid
		
		public Node(int x, int y){
			posX = x;
			posY = y;
		}
		
		@Override
		public int getX(){
			return posX;
		}
		@Override
		public int getY(){
			return posY;
		}
	}
	
}
