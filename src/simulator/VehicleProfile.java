package simulator;


import java.util.LinkedList;
import java.util.ListIterator;
import java.io.Serializable;

/** Instances of this class contain a name and a Vehicle. The class itself
 * contains a static list of all instances, and a number of methods to access
 * them.
 * Note:
 *   Static initialisation occurs whenever this class is first interacted with.
 *   To create an instance, use the factory constructor. */
public class VehicleProfile implements Serializable{
    /** Static side - has a list of all VehicleProfiles. **/

    private static LinkedList<VehicleProfile> vehicleProfiles;

    /* Initialse. */
    static {
        vehicleProfiles = new LinkedList<VehicleProfile>();
    }
    
    /* Create a new VehicleProfile. Name must be unique or this will return
     * null. */
    public static VehicleProfile createNewVehicleProfile(String name){
        VehicleProfile vp = getVehicleProfile(name);
        if ( vp!= null){
            return null;
        }
        vp = new VehicleProfile(name);
        vehicleProfiles.add(vp);
        return vp;
    }
    
    /* Searches for a VehicleProfile by name, returns the VehicleProfile if
     * one is found, otherwise returns null. */
    public static VehicleProfile getVehicleProfile(String name){
        ListIterator<VehicleProfile> iter = vehicleProfiles.listIterator();
        while ( iter.hasNext() ){
            VehicleProfile vp = iter.next();
            if ( vp.getName().equals(name) ){
                return vp;
            }
        }
        return null;
    }

    /* Adds a vehicle profile to the list. Should only be called when a
     * profile was not made with the factory constructor (e.g read from file).*/
    public static void addVehicleProfile(VehicleProfile vehicle){
        VehicleProfile vp = getVehicleProfile(vehicle.getName());
        if ( vp!= null){
            return;
        }
        vehicleProfiles.add(vehicle);
    }

    /* Obtains a pointer to the vehicle profiles. Treat this as read-only. */
    public static LinkedList<VehicleProfile> getVehicleProfiles(){
        return vehicleProfiles;
    }

    /* Returns a new list with vehicle profile names, in order. */
    public static LinkedList<String> getNames(){
        LinkedList<String> ret = new LinkedList<String>();
        
        ListIterator<VehicleProfile> iter = vehicleProfiles.listIterator();
        while ( iter.hasNext() ){
            VehicleProfile vp = iter.next();
            ret.add(vp.getName());
        }
        return ret;
    }   

    /* Removes a vehicle profile from the list. */
    public static void deleteVehicleProfile(String name){
        VehicleProfile vp = getVehicleProfile(name);
        vehicleProfiles.remove(vp);
    }
    
    /** Non-Static side. **/

    private String name;
    private Vehicle vehicle;
    private static final long serialVersionUID = 42L;
    
    /* Constructor. This should not be called outside this class.*/
    public VehicleProfile(String name){
       this.name = name;
       vehicle = new Vehicle();
    }

    /* Getters. */
    
    public String getName(){
        return name;
    }
    public Vehicle getVehicle(){
        return vehicle;
    }

    /* Setters. If a value is not acceptable, returns false.*/

    public boolean setName(String name){
        if (name == null)   return false;
        if (name.length() < 1)  return false;
        if (this.name.equals(name)) return true;
        if (getVehicleProfile(name) != null)    return false;
        
        this.name = name;
        return true;
    }
    public boolean setVehicle(Vehicle vehicle){
        if (vehicle == null) return false;
        this.vehicle = vehicle;
        return true;
    }
    
        
}
