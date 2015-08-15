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
   an attractive force. */
public class VectorField implements Navigator{
	
	
	public VectorField(){
		
	}
	
	@Override
	public String getName(){
		return "VectorField";
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
		
		double GOAL_FORCE = 0.5;			//The global attractive force
		int NUM_STEPS = 5;					//How many steps to plan ahead to
		
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

		return returnList;
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
