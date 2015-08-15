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

/* This is A* implementation that includes: No cuts, smoothing. See the README for details.*/
public class AStarCS implements Navigator{
	
	
	public AStarCS(){
		
	}
	@Override
	public String getName(){
		return "AStarCS";
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
			
			//Add all 4 Nodes around the chosen one to a potential node list, the potential node list
			// will be filtered to remove obstacles and already added nodes. The filtered potential 
			// node list will be added to the open list.
			currentPosX = chosen.getX();
			currentPosY = chosen.getY();
			LinkedList<Node> potentialNodeList = new LinkedList<Node>();
			if (currentPosX > 0) potentialNodeList.add(new Node(currentPosX-1, currentPosY, chosen));
			if (currentPosX < gridSizeX) potentialNodeList.add(new Node(currentPosX+1, currentPosY, chosen));
			if (currentPosY > 0) potentialNodeList.add(new Node(currentPosX, currentPosY-1, chosen));	//add node above
			if (currentPosY < gridSizeY) potentialNodeList.add(new Node(currentPosX, currentPosY+1, chosen)); //add node below
			
			//Add diagonal (no cuts)
			if (currentPosX > 0){
				if (currentPosY > 0){
					if (grid[currentPosX-1][currentPosY]==0 && grid[currentPosX][currentPosY-1]==0){
						 potentialNodeList.add(new Node(currentPosX-1, currentPosY-1, chosen));
					}
				}
				if (currentPosY < gridSizeY){
					if (grid[currentPosX-1][currentPosY]==0 && grid[currentPosX][currentPosY+1]==0){
						 potentialNodeList.add(new Node(currentPosX-1, currentPosY+1, chosen));
					}
				}
			}
			if (currentPosX < gridSizeX){
				if (currentPosY > 0){
					if (grid[currentPosX+1][currentPosY]==0 && grid[currentPosX][currentPosY-1]==0){
						 potentialNodeList.add(new Node(currentPosX+1, currentPosY-1, chosen));
					}
				}
				if (currentPosY < gridSizeY){
					if (grid[currentPosX+1][currentPosY]==0 && grid[currentPosX][currentPosY+1]==0){
						 potentialNodeList.add(new Node(currentPosX+1, currentPosY+1, chosen));
					}
				}
			}
			
			//Filter the potential node list - remove obstacles
			potentialNodeList = nodeFilter(potentialNodeList, obstacles);
			
			//Filter the potential node list - remove open list
			potentialNodeList = nodeFilter(potentialNodeList, openList);
			
			//Filter the potential node list - remove closed list
			potentialNodeList = nodeFilter(potentialNodeList, closedList);
			
			//Set the distance and heuristic for each node to be added to the open list
			for (int i=0; i<potentialNodeList.size(); i++){
				Node node = potentialNodeList.get(i);
				int x = node.getX();
				int y = node.getY();
				int parentDist = chosen.getDistance();
				int parentX = chosen.getX();
				int parentY = chosen.getY();
				//Set distance
				if ( x==parentX || y==parentY){		//check if diagonal
					node.setDistance(parentDist + 10);
				} else {
					node.setDistance(parentDist + 14);
				}
				
				//Set heuristic (Manhattan)
				int h = HEURISTIC_WEIGHTING* (Math.abs(x - destTileX) + Math.abs(y - destTileY));
				node.setHeuristic(h);
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
		
		//Smooth the path
		returnList = smoothPath(grid, startTileX, startTileY, returnList);
		return returnList;
	}
	
	/* Given a list containing Nodes, this function will iterate through the list and remove
	any elements that are present in the blackList.
	Returns a list with the blacklisted elements removed. */
	LinkedList<Node> nodeFilter(LinkedList<Node> list, LinkedList<Node> blackList){
		//System.out.println("Beginning filter");
		//Nodes from list are added to the return list if they pass the filter
		LinkedList<Node> returnList = new LinkedList<Node>();
		
		//Filter the list
		for (int i=0; i<list.size(); i++){
			boolean nodeOkay = true;		//Set to false if there is a match
			Node listNode = list.get(i);
			int listX = listNode.getX();
			int listY = listNode.getY();
			
			//Compare list node with the black list
			for (int j=0; j<blackList.size(); j++){
				Node blackListNode = blackList.get(j);
				if ( listX == blackListNode.getX() && listY == blackListNode.getY() ){
					//System.out.println("Filtering out "+listX + ","+listY);
					nodeOkay = false;
				}
			}
			
			//Add it to the return list if it was okay
			if (nodeOkay){
				returnList.add(listNode);
			}
		}
		//System.out.println("Completed filter");
		return returnList;
	}
	
	/* Given a path of Nodes, this function will cut out Nodes that are not needed according to 
	the tutorial found here:
	http://www.gamasutra.com/view/feature/131505/toward_more_realistic_pathfinding.php?page=2 */
	private LinkedList<NavigatorNode> smoothPath(int[][] grid, int startTileX, int startTileY, LinkedList<NavigatorNode> path){
		NavigatorNode checkPoint = null;				//The position of the last way point
		NavigatorNode currentPoint = null;				//The current position
		NavigatorNode nextPoint = null;				//The next position
		LinkedList<NavigatorNode> returnList = new LinkedList<NavigatorNode>(path);	//The list to return
												//It's a copy of the original list, then cuts out bad nodes
		ListIterator<NavigatorNode> iterator;	//Iterates over the path

		iterator = path.listIterator(0);	//Create the iterator
		checkPoint = new Node(startTileX, startTileY, null);				//First way point is our start position
		if (iterator.hasNext()) currentPoint = iterator.next();		//Current position is the start of the path
		while (iterator.hasNext()){
			nextPoint = iterator.next();
			if (isWalkable(checkPoint, nextPoint, grid)){	//currentPoint is safe to remove
				returnList.remove(currentPoint);
				currentPoint = nextPoint;
			} else {								//currentPoint is NOT safe to remove
				checkPoint = currentPoint;
				currentPoint = nextPoint;
			}
		}
		return returnList;
	}
	
	/* Given a start node, end node, and grid with obstacle information, this function calculates 
	whether a straight line between start and end crosses any obstacles. */
	private boolean isWalkable(NavigatorNode start, NavigatorNode end, int[][] grid){
		double posX;		//Position of point moving from start to end
		double posY;
		int tileX;			//Integer of position of point
		int tileY;
		
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
			posX+=incX;
			posY+=incY;
			tileX = (int)(posX+0.5); //round instead of floor
			tileY = (int)(posY+0.5);
			if (grid[tileX][tileY] > 0){
				return false;
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
		private int distance;		//This Node's distance from the start point
		private int heuristic;		//This Node's distance from the end point (Manhattan)
		
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
		
		
		
		
	
	}
	
	
}
