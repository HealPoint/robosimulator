/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, Adelaide Uni.
*/

package simulator.navigators;

import java.lang.Math;
import java.util.List;
import java.util.LinkedList;

import simulator.interfaces.Navigator;
import simulator.interfaces.NavigatorNode;

/* This is A* implementation that includes: low turning. See the README for details.*/
public class AStarT implements Navigator{
	
	
	public AStarT(){}
	
	/* The direction from a parent node to a child node. Used to calculate turning score. */
	public enum Direction{
		N, NE, E, SE, S, SW, W, NW, NONE
	}
	
	@Override
	public String getName(){
		return "AStarT";
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
		System.out.println("Calculating path from ("+startTileX+","+startTileY+") to ("+destTileX+","+destTileY+")");
		
		//Declare variables
		int HEURISTIC_WEIGHTING = 10;		//10 = same as dist, >10 means a node that's closer to the end will
											// be prioritised over a node that's not far from the start
		int TURNING_WEIGHTING = 10;			//10 = altering 45 degrees is the same as being one node away
		LinkedList<Node> obstacles;			//List of all obstructions
		LinkedList<Node> openList;			//Nodes that need checking
		LinkedList<Node> closedList;		//Nodes that have been checked
		LinkedList<NavigatorNode> returnList;//The list of Nodes in order to be traversed
		boolean isReached;					//Whether we have reached the destination
		int currentPosX;					//X position of current location
		int currentPosY;					//Y position of current location
		int gridSizeX;						//How many columns there are
		int gridSizeY;						//How many rows there are
		
		//Prepare for the search
		obstacles = new LinkedList<Node>();
		openList = new LinkedList<Node>();
		closedList = new LinkedList<Node>();
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
					Node obs = new Node(col, row, null);		//posX, posY, parent
					obstacles.add(obs);
				}
			}
		}
		
		//Initialise first point
		Node startNode = new Node(currentPosX, currentPosY, null);
		int heuristic = HEURISTIC_WEIGHTING*(Math.abs(startNode.getX()-destTileX) + Math.abs(startNode.getY()-destTileY));
		startNode.setHeuristic(heuristic);
		startNode.setDistance(0);
		startNode.setDirection(Direction.E);
		openList.add(startNode);
		
		//Begin A* search
		while (!isReached){
			//Search the open list to find the next most suitable Node
			int min = -1;					
			Node chosen = null;				//The chosen one shall lead us to salvation!
			for (int i=0; i<openList.size(); i++){
				Node n = openList.get(i);
				int f = n.getF();
				if (min == -1 || f < min){
					min = f;
					chosen = n;
				}
			}
			
			//Move the chosen one from the closed list to the open list
			openList.remove(chosen);
			closedList.add(chosen);
			
			//Check if we've reached the destination
			if (chosen.getX() == destTileX && chosen.getY() == destTileY ){
				isReached = true;
				break;
			}
			
			//Add all 8 Nodes around the chosen one to a potential node list, the potential node list
			// will be filtered to remove obstacles and already added nodes. The filtered potential 
			// node list will be added to the open list.
			currentPosX = chosen.getX();
			currentPosY = chosen.getY();
			LinkedList<Node> potentialNodeList = new LinkedList<Node>();
			if (currentPosX > 0){		//Add the 3 nodes to the left of the chosen one
				potentialNodeList.add(new Node(currentPosX-1, currentPosY, chosen));
				if (currentPosY > 0) potentialNodeList.add(new Node(currentPosX-1, currentPosY-1, chosen));
				if (currentPosY < gridSizeY) potentialNodeList.add(new Node(currentPosX-1, currentPosY+1, chosen));
			}
			if (currentPosX < gridSizeX){		//Add the 3 nodes to the right of the chosen one
				potentialNodeList.add(new Node(currentPosX+1, currentPosY, chosen));
				if (currentPosY > 0) potentialNodeList.add(new Node(currentPosX+1, currentPosY-1, chosen));
				if (currentPosY < gridSizeY) potentialNodeList.add(new Node(currentPosX+1, currentPosY+1, chosen));
			}
			if (currentPosY > 0) potentialNodeList.add(new Node(currentPosX, currentPosY-1, chosen));	//add node above
			if (currentPosY < gridSizeY) potentialNodeList.add(new Node(currentPosX, currentPosY+1, chosen)); //add node below
			
			//Filter the potential node list - remove obstacles
			potentialNodeList = nodeFilter(potentialNodeList, obstacles);
			
			//Filter the potential node list - remove open list
			potentialNodeList = nodeFilter(potentialNodeList, openList);
			
			//Filter the potential node list - remove closed list
			potentialNodeList = nodeFilter(potentialNodeList, closedList);
			
			//Set the distance, heuristic, direction, and turning value for each node to be added to the 
			//  open list
			for (int i=0; i<potentialNodeList.size(); i++){
				Node node = potentialNodeList.get(i);
				int x = node.getX();
				int y = node.getY();
				int parentDist = chosen.getDistance();
				int parentX = chosen.getX();
				int parentY = chosen.getY();
				Direction parentDir = chosen.getDirection();
				
				//Set distance
				if ( x==parentX || y==parentY){		//check if diagonal
					node.setDistance(parentDist + 10);
				} else {
					node.setDistance(parentDist + 14);
				}
				
				//Set heuristic (Manhattan)
				int h = HEURISTIC_WEIGHTING* (Math.abs(x - destTileX) + Math.abs(y - destTileY));
				node.setHeuristic(h);
				
				//Set direction
				if (x > parentX){ //right
					if (y > parentY)  node.setDirection(Direction.SE);
					if (y == parentY) node.setDirection(Direction.E);
					if (y < parentY)  node.setDirection(Direction.NE);
				}
				if (x == parentX){
					if (y > parentY)  node.setDirection(Direction.N);
					if (y < parentY)  node.setDirection(Direction.S);
				}
				if (x < parentX){ //left
					if (y > parentY)  node.setDirection(Direction.SW);
					if (y == parentY) node.setDirection(Direction.W);
					if (y < parentY)  node.setDirection(Direction.NW);
				}
				Direction dir = node.getDirection();
				
				//Add the turning score to the distance
				int turning = TURNING_WEIGHTING * getTurningScore(dir, parentDir);
				int dist = node.getDistance();
				node.setDistance(dist + turning);
			}
			
			
			//Add the filtered list to the open list
			for (int i=0; i<potentialNodeList.size(); i++){
				openList.add(potentialNodeList.get(i));
			}
			
			//If the open list is empty then we cannot find a solution
			if (openList.isEmpty()){
				return null;
			}
						
		}
		//Progress back up through parents until we're back at the start
		Node traceNode = closedList.getLast();			//The destination node was most recently added
		while ( !(traceNode.getX() == startTileX && traceNode.getY() == startTileY) ){
			returnList.addFirst(traceNode);
			traceNode = traceNode.getParent();
		}
		
		return returnList;
	}
	
	/* Given a list containing Nodes, this function will iterate through the list and remove
	any elements that are present in the blackList.
	Returns a list with the blacklisted elements removed. */
	LinkedList<Node> nodeFilter(LinkedList<Node> list, LinkedList<Node> blackList){
		//Nodes from list are added to the return list if they pass the filter
		LinkedList<Node> returnList = new LinkedList<Node>();
		
		//Filter the list
		for (int i=0; i<list.size(); i++){
			boolean nodeOkay = true;		//Set to false if there is a match
			Node listNode = list.get(i);
			int listX = listNode.getX();
			int listY = listNode.getY();
			Direction listDir = listNode.getDirection();
			
			//Compare list node with the black list
			for (int j=0; j<blackList.size(); j++){
				Node blackListNode = blackList.get(j);
				Direction blackListDir = blackListNode.getDirection();
				
				if ( listX == blackListNode.getX() && listY == blackListNode.getY() ){
					if ( blackListDir == Direction.NONE || blackListDir == listDir){
						nodeOkay = false;
					}
				}
			}
			
			//Add it to the return list if it was okay
			if (nodeOkay){
				returnList.add(listNode);
			}
		}
		return returnList;
	}
	
	/* Returns a score based on the difference in directions, 0deg = 0, 45deg turn = 1, 90deg = 2, 
	135deg = 3, 180deg = 4. This score is unscaled. */
	int getTurningScore(Direction d1, Direction d2){
		switch (d1){
			case N:
				switch (d2){
					case N:  return 0;
					case NE: return 1;
					case E:  return 2;
					case SE: return 3;
					case S:  return 4;
					case SW: return 3;
					case W:  return 2;
					case NW: return 1;
				}
			case NE:
				switch (d2){
					case N:  return 1;
					case NE: return 0;
					case E:  return 1;
					case SE: return 2;
					case S:  return 3;
					case SW: return 4;
					case W:  return 3;
					case NW: return 2;
				}
			case E:
				switch (d2){
					case N:  return 2;
					case NE: return 1;
					case E:  return 0;
					case SE: return 1;
					case S:  return 2;
					case SW: return 3;
					case W:  return 4;
					case NW: return 3;
				}
			case SE:
				switch (d2){
					case N:  return 3;
					case NE: return 2;
					case E:  return 1;
					case SE: return 0;
					case S:  return 1;
					case SW: return 2;
					case W:  return 3;
					case NW: return 4;
				}
			case S:
				switch (d2){
					case N:  return 4;
					case NE: return 3;
					case E:  return 2;
					case SE: return 1;
					case S:  return 0;
					case SW: return 1;
					case W:  return 2;
					case NW: return 3;
				}
			case SW:
				switch (d2){
					case N:  return 3;
					case NE: return 4;
					case E:  return 3;
					case SE: return 2;
					case S:  return 1;
					case SW: return 0;
					case W:  return 1;
					case NW: return 2;
				}
			case W:
				switch (d2){
					case N:  return 2;
					case NE: return 3;
					case E:  return 4;
					case SE: return 3;
					case S:  return 2;
					case SW: return 1;
					case W:  return 0;
					case NW: return 1;
				}
			case NW:
				switch (d2){
					case N:  return 1;
					case NE: return 2;
					case E:  return 3;
					case SE: return 4;
					case S:  return 3;
					case SW: return 2;
					case W:  return 1;
					case NW: return 0;
				}
		}		
		return -1;
	}
	
	/********************************************
	* Node Implementation						*
	*********************************************/
	private static class Node implements NavigatorNode{
		private int posX;			//This Node's column number in the grid
		private int posY;			//This Node's row number in the grid
		private Node parent;		//The previous Node in the path (null for obstacles and the start point)
		private int distance;		//This Node's distance from the start point
		private int heuristic;		//This Node's distance from the end point (Manhattan)
		private Direction direction;//The direction from the parent Node to this one
	
		public Node(int x, int y, Node p){
			posX = x;
			posY = y;
			parent = p;
			distance = 0;
			heuristic = 0;
		}
		
		@Override
		public int getX(){
			return posX;
		}
		@Override
		public int getY(){
			return posY;
		}
		public int getDistance(){
			return distance;
		}
		public Direction getDirection(){
			return direction;
		}
		public int getF(){
			return (distance + heuristic);
		}
		public Node getParent(){
			return parent;
		}
		
		public void setDistance(int d){
			distance = d;
		}
		public void setHeuristic(int h){
			heuristic = h;
		}
		public void setDirection(Direction d){
			direction = d;
		}
		
		
		
		
	
	}
	
	
}
