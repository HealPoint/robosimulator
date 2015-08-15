/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, Adelaide Uni.
*/

package simulator;

import java.lang.Math;
import java.util.Random;

import simulator.maps.*;

/** Records the real position and angle of the vehicle, then tells the RealMap the actual values, and the
ObservedMap and DiscreteMap the value with slight errors according to VehicleStats.
This class does not support accumulative errors.
This class does not support estimates such as Kalman filters. 
It is assumed that the MotorMover calls this regularly enough to not warrant its own thread. */
public class PositionEstimator{
	private double vehiclePosX;			//The X position of the vehicle, in m (pixel coordinates)
	private double vehiclePosY;			//The Y position of the vehicle, in m (pixel coordinates)
	private double vehicleAng;			//The orientation of the vehicle, in degrees (CCW from East)
	private double fakePosX;			//The fake X position of the vehicle, in m (pixel coordinates)
	private double fakePosY;			//The fake Y position of the vehicle, in m (pixel coordinates)
	private double fakeAng;				//The orientation of the vehicle, in degrees (CCW from East)
	
	private double distError;			//How much error (in m) can occur for vehicle position
	private double angError;			//How much error (in deg) can occur for vehicle angle
	private long gpsUpdatePeriod;		//The minimum time that must elapse before giving position (in milliseconds)
	private long imuUpdatePeriod;		//The minimum time that must elapse before giving angle (in milliseconds)
	private long lastGpsUpdatePeriod;	//The time of the last GPS update given to the maps
	private long lastImuUpdatePeriod;	//The time of the last IMU update given to the maps
	private boolean updateMap;			//Whether to periodically update the Observed/Discrete Maps, 
										// or to simply hold the information to be obtained via 
										// getter functions
	private Random rand;					//Random number generator, can do Gaussian.
	
	private RealMap rm;					//Links to the maps
	private ObservedMap om;
	private DiscreteMap dm;
	
	/* Constructor, set variables. */
	public PositionEstimator(RealMap rm, ObservedMap om, DiscreteMap dm, double distError, double angError,
						long gpsUpdatePeriod, long imuUpdatePeriod, boolean updateMap){
		this.rm = rm;
		this.om = om;
		this.dm = dm;
		vehiclePosX = 0;
		vehiclePosY = 0;
		vehicleAng = 0.0;
		fakePosX = 0;
		fakePosY = 0;
		fakeAng = 0;
		rand = new Random();
		this.distError = distError;
		this.angError = angError;
		this.gpsUpdatePeriod = gpsUpdatePeriod;
		this.imuUpdatePeriod = imuUpdatePeriod;
		lastGpsUpdatePeriod = 0;
		lastImuUpdatePeriod = 0;
		this.updateMap = updateMap;
	}
	
	/* Called by MotorMover */
	public void moveForward(double dist){
		double x = dist * Math.cos(vehicleAng*Math.PI/180.0);
		double y = dist * Math.sin(vehicleAng*Math.PI/180.0);
		vehiclePosX+= x;
		vehiclePosY-= y;			//flip to pixel coord
		alertMaps();
	}
	
	/* Called by MotorMover */
	public void rotateCCW(double ang){
		vehicleAng+=ang;
		if (vehicleAng > 180.0){
			vehicleAng -= 360.0;
		}
		if (vehicleAng <= -180.0){
			vehicleAng += 360.0;
		}
		alertMaps();
	}
	
	/* Tells the maps our position, with a random amount of error. The alerts are limited by the
		timer in order to simulate GPS and IMU readings.*/
	private void alertMaps(){
		boolean alertPos = false;
		boolean alertAng = false;
		
		//If enough time has passed, create new fake angle
		long timeNow = System.nanoTime();
		if ( (timeNow-lastImuUpdatePeriod)/1e6 >= imuUpdatePeriod){
			fakeAng = vehicleAng + (rand.nextGaussian())*angError/3.0;
			lastImuUpdatePeriod = timeNow;
			alertAng = true;
		}
		//If enough time has passed, create new fake position
		if ( (timeNow-lastGpsUpdatePeriod)/1e6 >= gpsUpdatePeriod){
			fakePosX = vehiclePosX + (rand.nextGaussian())*distError/3.0;
			fakePosY = vehiclePosY + (rand.nextGaussian())*distError/3.0;
			lastGpsUpdatePeriod = timeNow;
			alertPos = true;
		}
		
		//Real Map gets the real position all the time
		rm.setVehiclePos(vehiclePosX,vehiclePosY,vehicleAng);
		
		//Give ObservedMap and DiscreteMap error-filled values if enough time has passed and the 
		// updateMap flag is true.
		if (updateMap){
			if (alertPos){
				om.setVehiclePos(fakePosX, fakePosY);
				dm.setVehiclePos(fakePosX, fakePosY);
			}
			if (alertAng){
				om.setVehicleAng(fakeAng);
				dm.setVehicleAng(fakeAng);
			}
		}
		
	}
	
	
	/********************************************
	* Getters and Setters						*
	*********************************************/
	
	/* Called by Main initially */
	public void setVehiclePos(double x, double y, double ang){
		vehiclePosX = x;
		vehiclePosY = y;
		vehicleAng = ang;
		alertMaps();
	}
	
	
	
	
	
	
}
