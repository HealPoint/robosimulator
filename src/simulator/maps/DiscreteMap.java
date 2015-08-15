/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, Adelaide Uni.
*/

package simulator.maps;

import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

import simulator.interfaces.MapListener;
import simulator.interfaces.Navigator;
import simulator.interfaces.NavigatorNode;

/** The DiscreteMap contains estimated information about the vehicle, obstacles, and lines.
It also provides this information to the DiscreteMapGUI for display. Primitives are provided through
a listener interface, and lists are provided using pointers and mutex locks. **/
public class DiscreteMap{
	private double width;		//The width of the map, in m
	private double height;		//The height of the map, in m
	private int destTileX;		//The position of the destination in the grid
	private int destTileY;
	private int numTilesX;		//How many horizontal tiles there are
	private int numTilesY;		//How many vertical tiles there are
	private int[][] grid;		//A 2-D grid containing obstacle values, if a point falls in a tile the tile is incremented.
	private LinkedList<MapListener> listeners;		//Listeners will be alerted of map changes
	
							
	//These variables are altered or requested by another thread, so all access to them must be done by
	// the thread safe getter and setter functions
	private int vehicleTileX;	//The position of the vehicle in the grid
	private int vehicleTileY;
	private double vehicleAng;	//The orientation of the vehicle, in degrees (CCW from East)	
	private List<NavigatorNode> path;				//The order of nodes to go through to reach the destination (set by calculatePath())
													//Each node contains the x,y position of grid
	private Object mutexLock;	//The mutex lock to prevent thread clashes	
	
	public DiscreteMap(double width, double height, int numTilesX, int numTilesY){
		this.width = width;
		this.height = height;
		this.numTilesX = numTilesX;
		this.numTilesY = numTilesY;
		vehicleTileX = 0;
		vehicleTileY = 0;
		destTileX = 0;
		destTileY = 0;
		vehicleAng = 0.0;
		grid = new int[numTilesX][numTilesY];
		listeners = new LinkedList<MapListener>();
		path = new LinkedList<NavigatorNode>();
		mutexLock = new Object();
	}
	
	
	/* Adds a point to the map by incrementing the tile that the point falls in. 
	Arguments are in metres - pixel coordinates. */
	public void addPoint(double x, double y){
		double normX = x/width;		//normalise values
		double normY = y/height;
		
		int posX = (int)(normX * numTilesX);
		int posY = (int)(normY * numTilesY);
		
		if (posX < 0 || posX >= numTilesX || posY < 0 || posY >= numTilesY){
			System.out.println("DISCRETEMAP ERROR - POINT OUT OF BOUNDS");
			return;
		}
		grid[posX][posY]++;
			
		alertListeners();
	}
	
	/* Given a desired Navigator, we tell it to calculate a path from start to end.
	This path will then be recorded and displayed. Thread safe.*/
	public void calculatePath(Navigator nav){
		//Get a path from the grid, start position, end position
		List<NavigatorNode> p = nav.calculatePath(grid, vehicleTileX, vehicleTileY, destTileX, destTileY, getVehicleAng());
		synchronized(mutexLock){
			if (p == null){
				path = new LinkedList<NavigatorNode>();
			} else {
				path = p;
			}
		}
		alertListeners();
	}
	
	/********************************************
	* Listener methods 							*
	*********************************************/
	
	/* Adds a listener. Listeners will be alerted whenever the grid changes. The listener
		must implement the MapListener interface. */
	public void addListener(MapListener ml){
		if (listeners.contains(ml)) return;
		
		listeners.add(ml);	
	}
	
	/* Removes a listener. Listeners will be alerted whenever the grid changes. The listener
		must implement the MapListener interface. */
	public void removeListener(MapListener ml){
		listeners.remove(ml);
	}
	
	/* Alerts all listeners that the grid has changed.*/
	private void alertListeners(){
		ListIterator<MapListener> iterator = listeners.listIterator(0);
		while (iterator.hasNext()){
			iterator.next().mapHasChanged();
		}
	}

	/********************************************
	* Getters and Setters						*
	*********************************************/
	/* Basic getters and setters */
		
	public int[][] getGrid(){
		return grid;
	}
	public int getNumTilesX(){
		return numTilesX;
	}
	public int getNumTilesY(){
		return numTilesY;
	}
	public int getDestinationTileX(){
		return destTileX;
	}
	public int getDestinationTileY(){
		return destTileY;
	}
	public void setDestinationPos(double x, double y){
		double normX = x/width;		//normalise values
		double normY = y/height;
		
		int posX = (int)(normX * numTilesX);
		int posY = (int)(normY * numTilesY);
		
		if (posX < 0 || posX >= numTilesX || posY < 0 || posY >= numTilesY){
			System.out.println("DISCRETEMAP ERROR - DESTINATION OUT OF BOUNDS");
			return;
		}
		destTileX = posX;
		destTileY = posY;
		alertListeners();
	}
	
	/* Called once by the GUI thread to obtain pointers. */
	
	public Object getMutexLock(){
		return mutexLock;
	}
	
	/* Thread safe getters and setters */
	
	public int getVehicleTileX(){
		int x;
		synchronized (mutexLock){
			x = vehicleTileX;
		}
		return x;
	}
	public int getVehicleTileY(){
		int y;
		synchronized (mutexLock){
			y = vehicleTileY;
		}
		return y;
	}
	public double getVehicleAng(){
		double ang;
		synchronized (mutexLock){
			ang = vehicleAng;
		}
		return ang;
	}
	public void setVehiclePos(double x, double y){
		synchronized (mutexLock){
			double normX = x/width;		//normalise values
			double normY = y/height;
			
			int posX = (int)(normX * numTilesX);
			int posY = (int)(normY * numTilesY);
			
			if (posX < 0 || posX >= numTilesX || posY < 0 || posY >= numTilesY){
				System.out.println("DISCRETEMAP ERROR - VEHICLE OUT OF BOUNDS");
				return;
			}
			vehicleTileX = posX;
			vehicleTileY = posY;
		}
		alertListeners();
	}
	public void setVehicleAng(double ang){
		synchronized (mutexLock){
			vehicleAng = ang;
		}
		alertListeners();
	}
	public List<NavigatorNode> getPath(){
		synchronized (mutexLock){
			return path;
		}
	}
}
