/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, University of Adelaide.
*/

package simulator;

import java.util.Scanner;
import javax.swing.SwingUtilities;
import javax.swing.*;
import simulator.gui.*;
import simulator.maps.*;
import simulator.interfaces.Navigator;
import simulator.interfaces.Slam;
import simulator.navigators.*;

/** Hitting 'simulator' on the GUI creates an instance of this class, handing over all required
information. */
public class Simulator implements Runnable{
	private Vehicle veh;
	private Environment env;
	private boolean trails;
	private boolean timer;
	
	public Simulator(Vehicle veh, Environment env, boolean trails, boolean timer){
		this.veh = veh;
		this.env = env;
		this.trails = trails;
		this.timer = timer;
	}
	
	@Override
	public void run() {
		simulate();
	}
	public void simulate(){
		double[] lidarData;			//Array of distances, simulates real LIDAR data
		double width;				//Width of the map in metres
		double height;				//Height of the map in metres
		double vehiclePosX;			//Vehicle position in metres
		double vehiclePosY;			
		double vehicleAng;			//Orientation, CCW from East in degrees
		double destX;				//Destination, in metres
		double destY;
		int numTilesX;				//The grid size of the discrete map
		int numTilesY;
		
		//Set the map in metres
		width = 2.8;
		height = 2.8;
		vehiclePosX = env.getStartX();
		vehiclePosY = env.getStartY();
		vehicleAng = 0.0;
		destX = env.getGoalX();
		destY = env.getGoalY();
		numTilesX = 50;
		numTilesY = 50;
		
		//Create objects
		RealMap rm = new RealMap(width, height, env.getObstacles(), env.getLines());
		ObservedMap om = new ObservedMap(width, height);
		DiscreteMap dm = new DiscreteMap(width, height, numTilesX, numTilesY);
		PositionEstimator posEst = new PositionEstimator(rm, om, dm, veh.getGpsError(), veh.getImuError(), (long)(1000*veh.getGpsUpdatePeriod()), (long)(1000*veh.getImuUpdatePeriod()), true);
		(new Thread(new MotorMover(dm, posEst, veh.getLinearVelocity(), veh.getRotationalVelocity()))).start();
		final RealMapGUI rmgui = new RealMapGUI(rm, trails);
		final ObservedMapGUI omgui = new ObservedMapGUI(om);
		final DiscreteMapGUI dmgui = new DiscreteMapGUI(dm);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				rmgui.display();
				omgui.display();
				dmgui.display();
			}
		 });
		
		Navigator nav = Navigators.getNavigator(veh.getNavigatorName() ); //new AStarRRSSW();
		//Navigator nav = new AStar();
		
		//Set vehicle position
		posEst.setVehiclePos(vehiclePosX, vehiclePosY, vehicleAng);
		
		//Set goal position (move into position estimator)
		rm.setDestinationPos(destX, destY);
		om.setDestinationPos(destX, destY);
		dm.setDestinationPos(destX, destY);
		//waitFor(-1);
		
		//Loop until complete
		boolean found = false;
		while(!found){
			//Perform a LIDAR sweep
			performLidarSweep(veh, rm, om, dm);
			om.refreshObsPoints();
			
			//Perform a camera sweep
			performCameraSweep(veh, rm, om, dm);
			om.refreshLinePoints();
			
			//Perform a SLAM iteration
			//performSlam();
			
			//Perform navigation on the DiscreteMap
			dm.calculatePath(nav);
						
			//Check if we're at the end
			if (Math.abs(destX-rm.getVehiclePosX()) + Math.abs(destY-rm.getVehiclePosY()) < 0.2){
				found = true;
			}

		}
	
	}
	
	/*public static void main(String args[]){
		
		System.out.println("MAIN: Hello");
		double[] lidarData;			//Array of distances, simulates real LIDAR data
		double width;				//Width of the map in metres
		double height;				//Height of the map in metres
		double vehiclePosX;			//Vehicle position in metres
		double vehiclePosY;			
		double vehicleAng;			//Orientation, CCW from East in degrees
		double destX;				//Destination, in metres
		double destY;
		int numTilesX;				//The grid size of the discrete map
		int numTilesY;
		
		//Set the map in metres
		width = 8.0;
		height = 8.0;
		vehiclePosX = 1.0;
		vehiclePosY = 1.0;
		vehicleAng = 0.0;
		destX = 6.0;
		destY = 6.0;
		numTilesX = 20;
		numTilesY = 20;
		
		//Create objects
		VehicleStats veh = new VehicleStats();
		RealMap rm = new RealMap(width, height);
		ObservedMap om = new ObservedMap(width, height);
		DiscreteMap dm = new DiscreteMap(width, height, numTilesX, numTilesY);
		PositionEstimator posEst = new PositionEstimator(rm, om, dm, veh.getGpsError(), veh.getImuError(), veh.getGpsUpdatePeriod(), veh.getImuUpdatePeriod(), true);
		(new Thread(new MotorMover(dm, posEst, veh.getLinVelocity(), veh.getRotVelocity()))).start();
		RealMapGUI rmgui = new RealMapGUI(rm);
		ObservedMapGUI omgui = new ObservedMapGUI(om);
		DiscreteMapGUI dmgui = new DiscreteMapGUI(dm);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				rmgui.display();
				omgui.display();
				dmgui.display();
			}
		 });
		//Navigator nav = new AStarRRSSW();
		Navigator nav = new DStarLite();
		
		//Set vehicle position
		posEst.setVehiclePos(vehiclePosX, vehiclePosY, vehicleAng);
		
		//Set goal position (move into position estimator)
		rm.setDestinationPos(destX, destY);
		om.setDestinationPos(destX, destY);
		dm.setDestinationPos(destX, destY);
		waitFor(-1);
		
		//Loop until complete
		boolean found = false;
		while(!found){
			//Perform a LIDAR sweep
			performLidarSweep(veh, rm, om, dm);
			om.refreshObsPoints();
			
			//Perform a camera sweep
			performCameraSweep(veh, rm, om, dm);
			om.refreshLinePoints();
			
			//Perform a SLAM iteration
			performSlam();
			
			//Perform navigation on the DiscreteMap
			dm.calculatePath(nav);
						
			//Check if we're at the end
			if (Math.abs(destX-rm.getVehiclePosX()) + Math.abs(destY-rm.getVehiclePosY()) < 0.3){
				found = true;
			}

		}
	}*/
	

	/* Performs a LIDAR sweep using a Vehicle inside a RealMap. The data is then added to 
	the ObservedMap and DiscreteMap */
	public static void performLidarSweep(Vehicle veh, RealMap rm, ObservedMap om, DiscreteMap dm){
		double lidarRange = veh.getLidarRange();				//Get the angle the sweep spans
		double lidarIncrement = veh.getLidarIncrement();		//Get sweep precision
		double lidarDistance = veh.getLidarDistance();			//Get the acceptable distance
		int iterations = (int)(lidarRange / lidarIncrement) + 1;//For 180deg sweep with 1.0deg inc, there are 181 values
		int waitTime = (int)(veh.getLidarPeriod()*1000.0 / (double)iterations);
		double angle;											//The angle that is iterated
		double[] obsPos;										//The detected obstacle, in metres,metres - pixel coordinates
		for (int i=0; i<iterations; i++){
			angle = ((double)i)*lidarIncrement - lidarRange/2.0;	//The sweep goes CCW
			obsPos = rm.calculateObstacleCollision(angle, lidarDistance);
			if (obsPos != null){	//only continue if an obstacle was found
				om.addObstaclePoint(obsPos[0], obsPos[1]);
				dm.addPoint(obsPos[0], obsPos[1]);
			}
			waitFor(waitTime);
			rm.clearLasers();
		}
	}
	
	/* Performs a camera sweep using a Vehicle inside a RealMap. The data is then added to 
	the ObservedMap and DiscreteMap */
	public static void performCameraSweep(Vehicle veh, RealMap rm, ObservedMap om, DiscreteMap dm){
		double cameraRange = veh.getCameraRange();				//Get the angle the sweep spans
		double cameraIncrement = veh.getCameraIncrement();		//Get sweep precision
		double cameraDistance = veh.getCameraDistance();		//Get the acceptable distance
		int iterations = (int)(cameraRange / cameraIncrement) + 1;//For 180deg sweep with 1.0deg inc, there are 181 values
		int waitTime = (int)(veh.getCameraPeriod()*1000.0);
		double angle;											//The angle that is iterated
		double[] linePos;										//The detected line, in metres,metres - pixel coordinates
		for (int i=0; i<iterations; i++){
			angle = (double)(i)*cameraIncrement - cameraRange/2.0;	//The sweep goes CCW
			linePos = rm.calculateLineCollision(angle, cameraDistance);
			if (linePos != null){	//only continue if an obstacle was found
				om.addLinePoint(linePos[0], linePos[1]);
				dm.addPoint(linePos[0], linePos[1]);
			}
			
		}
		waitFor(waitTime);
		rm.clearLasers();
	
	
	}

	/* Performs a localisation estimate using SLAM. The data is then added to 
	the ObservedMap and DiscreteMap */
	//public static void performSlam(){
		//Update the points
		
		//Get all the relative and static estimations
		
		//Calculate the position using SLAM and its filter
		
		//Provide the position estimate to maps
		
	//}
	
	/* Waits depending on the settings. 
	time == 0: Do nothing
	time == -1: Waits for user input
	time > 0: Wait for this many milliseconds.*/
	public static void waitFor(int time){
		if (time == -1){
			waitForInput();
			return;
		}
		try {
			Thread.sleep(time);
		} catch (InterruptedException e){
			System.out.println(e);
		}
	}
	
	/* Waits for user input. Used by waitFor(-1). */
	public static void waitForInput(){
		System.out.println("Enter any key to continue: ");
		Scanner scan = new Scanner(System.in);
		String s = scan.next();
	}
}
