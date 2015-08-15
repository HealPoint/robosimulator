/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, Adelaide Uni.
*/

package simulator.navigators;

import java.lang.Math;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

import java.util.Scanner; //DELETE ME

import simulator.interfaces.Navigator;
import simulator.interfaces.NavigatorNode;

/* This is a standard RapidExploringRandomTree implementation */
public class RapidExploringRandomTree implements Navigator{
	
	
	public RapidExploringRandomTree(){
		
	}
	
	@Override
	public String getName(){
		return "RapidExploringRandomTree";
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
		//Initialise
		LinkedList<Node> vertices = new LinkedList<Node>();
		LinkedList<NavigatorNode> returnList = new LinkedList<NavigatorNode>();
		Node startNode = new Node(startTileX, startTileY, null);
		vertices.add(startNode);
		
		boolean finished = false;
		while (!finished){
			
			//Scanner scan = new Scanner(System.in);
			//String s = scan.next();
		
			//Determine a 'random' point in space
			int randX = (int)( Math.random() * grid.length );
			int randY = (int)( Math.random() * grid[0].length );

			//Find the point's nearest neighbour
			int min = -1;
			Node nearNode = null;
			ListIterator<Node> iter = vertices.listIterator();
			while (iter.hasNext()){
				Node next = iter.next();
				int dx = randX - next.getX();
				int dy = randY - next.getY();
				int dist = dx*dx + dy*dy;		//actually dist^2 but that's okay
				if (min == -1 || dist < min){
					min = dist;
					nearNode = next;
				}
			}
			
			//Check that we're not duplicating a node
			if (min == 0){
				continue;
			}
			
			//Determine a new point between the random point and nearest neighbour that meets 
			//  distance-to-neighbour and distance-from-obstacles criteria
			int dx = randX - nearNode.getX();		//Calc distance and angle between nodes
			int dy = randY - nearNode.getY();
			double ang = Math.atan2(dy, dx);
			double dist2 = dx*dx + dy*dy;
			double dist = Math.sqrt( (double)dist2 );
			
			Node newNode = null;
			if (dist > 4.0){
				double normX = (double)dx / dist;		//Normalise the distances
				double normY = (double)dy / dist;
				double scaleX = normX * 4.0;			//Scale the distances
				double scaleY = normY * 4.0;
				int newX = (int) scaleX + nearNode.getX();		//Create a new point	
				int newY = (int) scaleY + nearNode.getY();

				newNode = new Node(newX, newY, nearNode);	//Create new node with parent
			} else {
				newNode = new Node(randX, randY, nearNode); //Create new node with parent
			}
			
			//Add the new point to the list if it avoids obstacles
			if (isWalkable(nearNode, newNode, grid, 4.0) ){
				vertices.add(newNode);
			} else {
				continue;
			}
			
			//Check completion
			if (newNode.getX() == destTileX && newNode.getY() == destTileY){
				finished = true;
			}
		}
		
		//Progress back up through parents until we're back at the start
		Node traceNode = vertices.getLast();			//The destination node was most recently added
		while ( !(traceNode.getX() == startTileX && traceNode.getY() == startTileY) ){
			returnList.addFirst(traceNode);
			traceNode = traceNode.getParent();
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
		private Node parent;		//The previous Node in the path (null for obstacles and the start point)
		
		public Node(int x, int y, Node p){
			posX = x;
			posY = y;
			parent = p;
		}
		
		@Override
		public int getX(){
			return posX;
		}
		@Override
		public int getY(){
			return posY;
		}
		
		public Node getParent(){
			return parent;
		}
	}
	
	
}
