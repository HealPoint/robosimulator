/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, Adelaide Uni.
*/

package simulator.maps;

import java.util.LinkedList;
import java.util.ListIterator;
import java.awt.Point;
import simulator.interfaces.MapListener;

/** The ObservedMap contains estimated information about the vehicle, obstacles, and lines.
It also provides this information to the ObservedMapGUI for display. Primitives are provided through
a listener interface, and lists are provided using pointers and mutex locks. **/
public class ObservedMap{
	private double width;		//The width of the map, in m
	private double height;		//The height of the map, in m
	private double destPosX;		//The X position of the destination, in m (pixel coordinates);
	private double destPosY;		//The Y position of the destination, in m (pixel coordinates);
	
	private LinkedList<MapListener> listeners;		//Listeners will be alerted of map changes
	
	//These variables are read by the GUI thread, so all changes to them must be done with the 
	// mutex lock.
	private LinkedList<Point> oldPoints;	//Stores the x,y coordinates (mm,mm -pixel coordinates),
											// old points will be marked as black .
	private LinkedList<Point> newPoints;	//Stores the x,y coordinates (mm,mm -pixel coordinates),
											// new points will be marked as red.
	private Object mutexLockGui;			//Mutual exclusion lock
	
	//These three variables are altered by another thread, so all access to them must be done by
	// the thread safe getter and setter functions
	private double vehiclePosX;	//The X position of the vehicle, in m (pixel coordinates)
	private double vehiclePosY;	//The Y position of the vehicle, in m (pixel coordinates)
	private double vehicleAng;	//The orientation of the vehicle, in degrees (CCW from East)
	private Object mutexLockVeh;	//The mutex lock to prevent thread clashes
	
	public ObservedMap(double width, double height){
		this.width = width;
		this.height = height;
		vehiclePosX = 0;
		vehiclePosY = 0;
		vehicleAng = 0;
		destPosX = 0;
		destPosY = 0;
		oldPoints = new LinkedList<Point>();
		newPoints = new LinkedList<Point>();
		listeners = new LinkedList<MapListener>();
		mutexLockVeh = new Object();
		mutexLockGui = new Object();
	}
	
	
	/* Adds a point of an obstacle (metres,metres - pixel coordinates), to the observed map.
	The point will be marked as new, until @refreshObsPoints() is called. Thread safe.*/
	public void addObstaclePoint(double x, double y){
		synchronized(mutexLockGui){
			newPoints.add(new Point((int)(x*1000.0), (int)(y*1000.0)));		//points are in mm
		}
		alertListeners();
	}
	
	/* Adds a point of a line (metres,metres - pixel coordinates), to the observed map.
	The point will be marked as new, until @refreshLinePoints() is called. Thread safe.*/
	public void addLinePoint(double x, double y){
		synchronized(mutexLockGui){
			newPoints.add(new Point((int)(x*1000.0), (int)(y*1000.0)));	//convert to mm
		}
		alertListeners();
	}
	
	/* All obstacle points that are marked as new will be marked as old. Thread safe*/
	public void refreshObsPoints(){
		if (newPoints.size() <= 0) return;
		
		synchronized(mutexLockGui){
			while (newPoints.size() > 0){
				Point p = newPoints.pop();
				oldPoints.add(p);
			}
		}
		alertListeners();
	}
	
	/* All line points that are marked as new will be marked as old. Thread safe.*/
	public void refreshLinePoints(){
		if (newPoints.size() <= 0) return;
		synchronized(mutexLockGui){
			while (newPoints.size() > 0){
				Point p = newPoints.pop();
				oldPoints.add(p);
			}
		}
		alertListeners();
	}
	
	/********************************************
	* Listener methods 							*
	*********************************************/
	
	/* Adds a listener. Listeners will be alerted whenever a point is added or refreshed. The listener
		must implement the MapListener interface. */
	public void addListener(MapListener ml){
		if (listeners.contains(ml)) return;
		
		listeners.add(ml);	
	}
	
	/* Removes a listener. Listeners will be alerted whenever a point is added or refreshed. The listener
		must implement the MapListener interface. */
	public void removeListener(MapListener ml){
		listeners.remove(ml);
	
	}
	
	/* Alerts all listeners that a point has been added, or points have been refreshed.*/
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
	
	public double getWidth(){
		return width;
	}
	public double getHeight(){
		return height;
	}
	public double getDestinationPosX(){
		return destPosX;
	}
	public double getDestinationPosY(){
		return destPosY;
	}
	public void setDestinationPos(double x, double y){
		destPosX = x;
		destPosY = y;
		alertListeners();
	}
	
	/* Called once by the GUI thread to obtain pointers. */
	
	public LinkedList<Point> getOldPoints(){
		return oldPoints;
	}
	public LinkedList<Point> getNewPoints(){
		return newPoints;
	}
	public Object getMutexLockGui(){
		return mutexLockGui;
	}
	
	/* Threadsafe getters and setters */
	
	public double getVehiclePosX(){
		double x;
		synchronized (mutexLockVeh){
			x = vehiclePosX;
		}
		return x;
	}
	public double getVehiclePosY(){
		double y;
		synchronized (mutexLockVeh){
			y = vehiclePosY;
		}
		return y;
	}
	public double getVehicleAng(){
		double ang;
		synchronized (mutexLockVeh){
			ang = vehicleAng;
		}
		return ang;
	}

	public void setVehiclePos(double x, double y){
		synchronized (mutexLockVeh){
			vehiclePosX = x;
			vehiclePosY = y;
		}
		alertListeners();
	}
	public void setVehicleAng(double ang){
		synchronized (mutexLockVeh){
			vehicleAng = ang;
		}
		alertListeners();
	}
	
	
}
