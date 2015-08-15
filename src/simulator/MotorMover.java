/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, Adelaide Uni.
*/

package simulator;

import java.lang.Runnable;
import java.util.List;
import java.lang.Math;

import simulator.maps.DiscreteMap;
import simulator.interfaces.MapListener;
import simulator.interfaces.NavigatorNode;

/* This class moves the vehicle. It observes the discrete map to find our estimated position and our
path, then alters our real position.
To Improve: Only move when a region has been looked at and a new path has been found. */
public class MotorMover implements Runnable, MapListener {
	private double DEST_ACCEPT_DIST = 0.3;	//How close the vehicle needs to be to a destination, in m
	private double DEST_ACCEPT_ANG = 2.0;	//How close the angle needs to be to go in a desired direction, in deg
	private long WAIT_TIME = 5;			//How many milliseconds wait between movements
	
	private DiscreteMap dMap;			//Link to the map with the path we're tracking
	private PositionEstimator posEst;	//Link to the class that records the real position
	
	private double linVelocity;			//The velocity of the vehicle in a straight line, in m/cycle
	private double rotVelocity;			//The velocity of the vehicle rotating on the spot, in rad/cycle
	
	//These variables are altered by another thread, so all access to them must be done by
	// the thread safe getter and setter functions
	private int vehicleTileX;			//The position of the vehicle in the grid
	private int vehicleTileY;
	private double vehicleAng;			//The orientation of the vehicle, in degrees (CCW from East)
	private List<NavigatorNode> path;	//The order of nodes to go through to reach the destination (set by calculatePath())
										//Each node contains the x,y position of grid
	private Object mutexLock;			//The mutex lock to prevent thread clashes	
	
	/* Constructor, set variables. */
	public MotorMover(DiscreteMap dMap, PositionEstimator posEst, double linVelocity, double rotVelocity){
		this.dMap = dMap;
		this.posEst = posEst;
		this.linVelocity = linVelocity * (double)WAIT_TIME / 1000.0; //convert from m/s to m/cycle
		this.rotVelocity = rotVelocity * (double)WAIT_TIME / 1000.0; //convert from rad/s to rad/cycle
		mutexLock = new Object();
		
		dMap.addListener(this);
	}
	
	/********************************************
	* Threading methods 							*
	*********************************************/
	
	/* At the moment this just follows the path as closely as possible, does not have smoothing or take
	 into account anything beyond the next step. If the GPS/IMU is not updated regularly enough there is 
	 a chance that we appear to jump over our destination, so it tries to turn us back to go there. 
	 This could be the case if the robot appears to turn on the spot for no reason. */
	@Override
	public void run(){
		while (true){
			if (path != null && !path.isEmpty()){
				boolean gotANode = false;
				
				//Get the destination node
				NavigatorNode node = path.get(0);
				int destX = node.getX();
				int destY = node.getY();
				
				//Get our position
				int x = getVehicleTileX();
				int y = getVehicleTileY();
				double ang = getVehicleAng();
				
				//If we are close enough to the dest node, remove it from the list
				int distX = Math.abs(destX-x);
				int distY = Math.abs(destY-y);
				if ( distX*distX + distY*distY < DEST_ACCEPT_DIST*DEST_ACCEPT_DIST ){
					path.remove(0);
					gotANode=false;
				} else {
					gotANode=true;
				}
				
				if (gotANode){
					//Calculate angle from us to destination
					double dy = destY - y;
					double dx = destX - x;
					double destAng = (180.0/Math.PI) * Math.atan2(-dy, dx);		//flip y because of pixel coord
					double changeAng = destAng-ang;
					if (changeAng > 180.0) changeAng -= 360.0;
					if (changeAng <= -180.0) changeAng += 360.0;
					
					//Move accordingly
					if ( Math.abs(changeAng) < DEST_ACCEPT_ANG ){		//acceptable angle
						posEst.moveForward(linVelocity);
					} else if (changeAng > 0){
						posEst.rotateCCW(rotVelocity);
					} else {
						posEst.rotateCCW(-rotVelocity);
					}
				}
				
				
			} else { //what to do if no path?
			
			}
			
			//Wait for a bit
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e){
				System.out.println(e);
			}
		}
	}
	
	/********************************************
	* Listener methods 							*
	*********************************************/
	
	/*Updates our snapshot of where the vehicle is, and the path we need to track. */
	@Override
	public void mapHasChanged(){
		setVehicleTileX(dMap.getVehicleTileX());
		setVehicleTileY(dMap.getVehicleTileY());
		setVehicleAng(dMap.getVehicleAng());
		setPath(dMap.getPath());
	}
		
	/********************************************
	* Getters and Setters (must be thread safe!) *
	*********************************************/
	
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
	public void setVehicleTileX(int x){
		synchronized (mutexLock){
			vehicleTileX = x;
		}
	}
	public void setVehicleTileY(int y){
		synchronized (mutexLock){
			vehicleTileY = y;
		}
	}
	public void setVehicleAng(double ang){
		synchronized (mutexLock){
			vehicleAng = ang;
		}
	}
		
	//fix this!!!
	public void setPath(List<NavigatorNode> path){
		synchronized (mutexLock){
			this.path = path;
		}
	}
}
