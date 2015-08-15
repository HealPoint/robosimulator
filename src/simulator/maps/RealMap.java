/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, Adelaide Uni.
*/

package simulator.maps;

import java.lang.Math;
import java.util.LinkedList;
import java.util.ListIterator;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import simulator.interfaces.MapListener;

/** The RealMap contains the perfect information about the vehicle, obstacles, lines, and lasers.
It also provides this information to the RealMapGUI for display. Primitives are provided through
a listener interface, and lists are provided using pointers and mutex locks. **/
public class RealMap{
	private double width;	//The width of the map, in m
	private double height;	//The height of the map, in m
	private double destPosX;		//The X position of the destination, in m (pixel coordinates);
	private double destPosY;		//The Y position of the destination, in m (pixel coordinates);
	
	private LinkedList<MapListener> listeners;		//Listeners will be alerted of map changes
	
	//These variables are read by the GUI thread, so all changes to them must be done with the 
	// mutex lock.
	private LinkedList<Ellipse2D.Double> obstacles;	//Obstacles with their position in m,m - pixel coordinates - obtained from Darryl's part
	private LinkedList<Ellipse2D.Double> lines;		//Lines with their position in m,m - pixel coordinates - obtained from Darryl's part
	private LinkedList<Line2D.Double> lasers;		//Contains lines indicating collision detection
	private Object mutexLockGui;					//Mutual exclusion lock
	
	//These three variables are altered by the mover thread, so all access to them must be done by
	// the thread safe getter and setter functions.
	private double vehiclePosX;	//The X position of the vehicle, in m (pixel coordinates)
	private double vehiclePosY;	//The Y position of the vehicle, in m (pixel coordinates)
	private double vehicleAng;	//The orientation of the vehicle, in degrees (CCW from East)
	private Object mutexLockVeh;				//The mutex lock to prevent thread clashes
	
	public RealMap(double width, double height, LinkedList<Ellipse2D.Double> obstacles, LinkedList<Ellipse2D.Double> lines){
		//Set variables to defaults
		this.width = width;
		this.height = height;
		vehiclePosX = 0;
		vehiclePosY = 0;
		vehicleAng = 0;
		destPosX = 0;
		destPosY = 0;
		listeners = new LinkedList<MapListener>();
		this.obstacles = obstacles; //new LinkedList<Ellipse2D.Double>();
		this.lines = lines; //new LinkedList<Ellipse2D.Double>();
		lasers = new LinkedList<Line2D.Double>();
		mutexLockVeh = new Object();
		mutexLockGui = new Object();
	}
	
	/*Adds an obstacle to the list. Ellipse values should be in metres.*/
	//public void addObstacle(Ellipse2D.Double obs){
	//	obstacles.add(obs);
	//}
	
	/*Given an angle (degrees, CCW from East), this function will return the position (in metres,metres - pixel coordinates)
	of where an imaginary particle would collide with an obstacle, as though it were fired from
	the vehicle. Note that the angle given is relative to the vehicle orientation. If the maximum distance or boundaries of
	the map	are reached, returns null. 
	Also creates a laser from the vehicle to the collision point.*/
	public double[] calculateObstacleCollision(double ang, double maxDist){
		double NUDGE = 0.01;	//How far the projectile moves each iteration, in m
		double posX = getVehiclePosX();
		double posY = getVehiclePosY();
		double totAng = (vehicleAng+ang)*Math.PI/180.0;
		double incX = NUDGE*Math.cos(totAng);	//projectile increment
		double incY = -NUDGE*Math.sin(totAng);
		double retX = -1;						//return values
		double retY = -1;
		
		//Check for collision
		boolean complete = false;
		while (!complete){
			//Nudge the projectile
			posX += incX;
			posY += incY;
			maxDist -= NUDGE;
			
			//Check bounds and add laser if bounds are reached
			if (maxDist < 0 || posX < 0 || posX > width || posY < 0 || posY > height){
				addLaser(getVehiclePosX(), getVehiclePosY(), posX, posY);
				return null;
			}
			//Compare each object
			ListIterator<Ellipse2D.Double> iterator = obstacles.listIterator(0);
			while (iterator.hasNext() && !complete){
				Ellipse2D.Double circ = iterator.next();
				double circW = circ.getWidth();
				double circH = circ.getHeight();
				double circX = circ.getX() + circW/2.0;			//Centre of the circle
				double circY = circ.getY() + circH/2.0;
				
				double xBound = (posX - circX)*(posX - circX) / (circW*circW/4.0);
				double yBound = (posY - circY)*(posY - circY) / (circH*circH/4.0);
				boolean contained = xBound+yBound <= 1.0;
				
				//If the point is within the object, we can return
				if (contained){
					complete = true;
					retX = posX;
					retY = posY;
				}
			}
		}

		//Add a laser
		double[] xy = {retX, retY};
		addLaser(getVehiclePosX(), getVehiclePosY(), retX, retY);
		
		return xy;
	}
	
	/*Given an angle (degrees, CCW from East), this function will return the position (in metres,metres - pixel coordinates)
	of where an imaginary particle would collide with a line, as though it were fired from
	the vehicle. Note that the angle given is relative to the vehicle orientation. If the maximum distance or boundaries of
	the map	are reached, returns null. */
	public double[] calculateLineCollision(double ang, double maxDist){
		double NUDGE = 0.01;	//How far the projectile moves each iteration, in m
		double posX = getVehiclePosX();
		double posY = getVehiclePosY();
		double totAng = (vehicleAng+ang)*Math.PI/180.0;
		double incX = NUDGE*Math.cos(totAng);	//projectile increment
		double incY = -NUDGE*Math.sin(totAng);
		double retX = -1;						//return values
		double retY = -1;
		
		//Check for collision
		boolean complete = false;
		while (!complete){
			//Nudge the projectile
			posX += incX;
			posY += incY;
			maxDist -= NUDGE;
			
			//Check bounds
			if (maxDist < 0 || posX < 0 || posX > width || posY < 0 || posY > height){
				addLaser(getVehiclePosX(), getVehiclePosY(), posX, posY);
				return null;
			}
			//Compare each object
			ListIterator<Ellipse2D.Double> iterator = lines.listIterator(0);
			while (iterator.hasNext() && !complete){
				Ellipse2D.Double circ = iterator.next();
				double circW = circ.getWidth();
				double circH = circ.getHeight();
				double circX = circ.getX() + circW/2.0;			//Centre of the circle
				double circY = circ.getY() + circH/2.0;
				
				double xBound = (posX - circX)*(posX - circX) / (circW*circW/4.0);
				double yBound = (posY - circY)*(posY - circY) / (circH*circH/4.0);
				boolean contained = xBound+yBound <= 1.0;
				
				//If the point is within the object, we can return
				if (contained){
					complete = true;
					retX = posX;
					retY = posY;
				}
			}
		}

		//Add a laser
		double[] xy = {retX, retY};
		addLaser(getVehiclePosX(), getVehiclePosY(), retX, retY);

		return xy;
	}
	
	/*Adds a laser from (x1,y1) to (x2,y2) to the laser list. Thread safe. */
	private void addLaser(double x1, double y1, double x2, double y2){
		synchronized (mutexLockGui){
			lasers.add(new Line2D.Double(x1, y1, x2, y2));
		}
		alertListeners();
	}
	
	/*Empties the list containing all the laser lines. Thread safe.*/
	public void clearLasers(){
		if (lasers.size() <= 0) return;
		
		synchronized (mutexLockGui){
			lasers.clear();
		}
		alertListeners();
	}
	
	/********************************************
	* Listener methods 							*
	*********************************************/
	
	/* Adds a listener. Listeners will be alerted whenever a circle or line is added. The listener
		must implement the MapListener interface. */
	public void addListener(MapListener ml){
		if (listeners.contains(ml)) return;
		
		listeners.add(ml);	
	}
	
	/* Removes a listener. Listeners will be alerted whenever a circle or line is added. The listener
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
	
	public LinkedList<Ellipse2D.Double> getObstacles(){
		return obstacles;
	}
	public LinkedList<Ellipse2D.Double> getLines(){
		return lines;
	}
	public LinkedList<Line2D.Double> getLasers(){
		return lasers;
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

	public void setVehiclePos(double x, double y, double ang){
		synchronized (mutexLockVeh){
			vehiclePosX = x;
			vehiclePosY = y;
			vehicleAng = ang;
		}
		alertListeners();
	}
	
	
	
	
	
}
