package simulator;


import java.util.LinkedList;
import java.util.ListIterator;
import java.io.Serializable;

/** Contains all values needed to describe a vehicle to be driven on an
 * environment
 * Assumes:
 *  Constant values.
 *  Robot is rectangular.
 *  Robot can rotate on the spot. */
public class Vehicle implements Serializable{

    private static final long serialVersionUID = 42L;
    private double width;
    private double height;
    private double linVelocity;
    private double rotVelocity;
    private double lidarRange;
    private double lidarIncrement;
    private double lidarDistance;
    private double lidarPeriod;
    private double cameraRange;
    private double cameraIncrement;
    private double cameraDistance;
    private double cameraPeriod;
    private double gpsError;
    private double gpsUpdatePeriod;
    private double imuError;
    private double imuUpdatePeriod;
    private String navigatorName;    
        
    /* Constructor. */
    public Vehicle(){
       width = 0.3;
       height = 0.5;
       linVelocity = 0.5;
       rotVelocity = 60.0;
       lidarRange = 180.0;
       lidarIncrement = 1.0;
       lidarDistance = 8.0;
       lidarPeriod = 1.0;
       cameraRange = 180.0;
       cameraIncrement = 1.0;
       cameraDistance = 2.0;
       cameraPeriod = 0.6;
       gpsError = 0;
       gpsUpdatePeriod = 0.5;
       imuError = 0;
       imuUpdatePeriod = 0.01;
       navigatorName = Navigators.getNames()[0];
    }

    /* Getters. */
    
    public double getWidth(){
        return width;
    }
    public double getHeight(){
        return height;
    }
    public double getLinearVelocity(){
        return linVelocity;
    }
    public double getRotationalVelocity(){
        return rotVelocity;
    }
    public double getLidarRange(){
        return lidarRange;
    }
    public double getLidarIncrement(){
        return lidarIncrement;
    }
    public double getLidarDistance(){
        return lidarDistance;
    }
    public double getLidarPeriod(){
        return cameraPeriod;
    }
    public double getCameraRange(){
        return cameraRange;
    }
    public double getCameraIncrement(){
        return cameraIncrement;
    }
    public double getCameraDistance(){
        return cameraDistance;
    }
    public double getCameraPeriod(){
        return cameraPeriod;
    }
    public double getGpsError(){
        return gpsError;
    }
    public double getGpsUpdatePeriod(){
        return gpsUpdatePeriod;
    }
    public double getImuError(){
        return imuError;
    }
    public double getImuUpdatePeriod(){
        return imuUpdatePeriod;
    }
    public String getNavigatorName(){
        return navigatorName;
    }
    
    /* Setters. If a value is not acceptable, returns false.*/

    public boolean setWidth(double width){
        if (width <= 0.0)   return false;
        this.width = width;
        return true;
    }
    public boolean setHeight(double height){
        if (height <= 0.0)  return false;
        this.height = height;
        return true;
    }
    public boolean setLinearVelocity(double linVelocity){
        if (linVelocity <= 0.0)  return false;
        this.linVelocity = linVelocity;
        return true;
    }
    public boolean setRotationalVelocity(double rotVelocity){
        if (rotVelocity <= 0.0)  return false;
        this.rotVelocity = rotVelocity;
        return true;
    }
    public boolean setLidarRange(double lidarRange){
        if (lidarRange <= 0.0)  return false;
        if (lidarRange > 359.0) return false;
        this.lidarRange = lidarRange;
        return true;
    }
    public boolean setLidarIncrement(double lidarIncrement){
        if (lidarIncrement <= 0.0)  return false;
        this.lidarIncrement = lidarIncrement;
        return true;
    }
    public boolean setLidarDistance(double lidarDistance){
        if (lidarDistance <= 0.0)  return false;
        this.lidarDistance = lidarDistance;
        return true;
    }
    public boolean setLidarPeriod(double lidarPeriod){
        if (lidarPeriod <= 0.0)  return false;
        this.lidarPeriod = lidarPeriod;
        return true;
    }
    public boolean setCameraRange(double cameraRange){
        if (cameraRange <= 0.0)  return false;
        this.cameraRange = cameraRange;
        return true;
    }
    public boolean setCameraIncrement(double cameraIncrement){
        if (cameraIncrement <= 0.0)  return false;
        this.cameraIncrement = cameraIncrement;
        return true;
    }
    public boolean setCameraDistance(double cameraDistance){
        if (cameraDistance <= 0.0)  return false;
        this.cameraDistance = cameraDistance;
        return true;
    }
    public boolean setCameraPeriod(double cameraPeriod){
        if (cameraPeriod <= 0.0)  return false;
        this.cameraPeriod = cameraPeriod;
        return true;
    }
    public boolean setGpsError(double gpsError){
        if (gpsError < 0.0)  return false;
        this.gpsError = gpsError;
        return true;
    }
    public boolean setGpsUpdatePeriod(double gpsUpdatePeriod){
        if (gpsUpdatePeriod <= 0.0)  return false;
        this.gpsUpdatePeriod = gpsUpdatePeriod;
        return true;
    }
    public boolean setImuError(double imuError){
        if (imuError < 0.0)  return false;
        this.imuError = imuError;
        return true;
    }
    public boolean setImuUpdatePeriod(double imuUpdatePeriod){
        if (imuUpdatePeriod <= 0.0)  return false;
        this.imuUpdatePeriod = imuUpdatePeriod;
        return true;
    }
    public boolean setNavigatorName(String navigatorName){
        if (navigatorName == null)  return false;
        if (Navigators.getNavigator(navigatorName) == null ) return false;
        this.navigatorName = navigatorName;
        return true;
    }        
}
