package simulator.gui;

import java.io.*;
import java.util.LinkedList;
import java.util.ListIterator;

import simulator.*;

/** Reads and writes Vehicle and Environment profiles from their respective log
 * files. When reading, the static lists contained in VehicleProfile and
 * EnvironmentProfile will be updated. When writing, the log file will be
 * overriden with the lists contained in VehicleProfile and EnvironmentProfile.*/
public class Logger {

    /* Reads in all VehicleProfile and EnvironmentProfile objects and set them
     * to the static lists. */
    public static void readAll(){
        // Vehicle Profiles
        try {
            FileInputStream fis = new FileInputStream("log/vehLog");
            ObjectInputStream ois = new ObjectInputStream(fis);

            while (fis.available() > 0 ){
                VehicleProfile vp = (VehicleProfile) ois.readObject();
                VehicleProfile.addVehicleProfile(vp);
                //System.out.println(vp);
            }
            ois.close();
        } catch (IOException | ClassNotFoundException e){
            System.out.println(e);
        }

        // Environment Profiles
        try {
            FileInputStream fis = new FileInputStream("log/envLog");
            ObjectInputStream ois = new ObjectInputStream(fis);

            while (fis.available() > 0 ){
                EnvironmentProfile ep = (EnvironmentProfile) ois.readObject();
                EnvironmentProfile.addEnvironmentProfile(ep);
                //System.out.println(ep);
            }
            ois.close();
        } catch (IOException e){
            System.out.println(e);
        } catch (ClassNotFoundException e){
            System.out.println("ERROR Save data does not match available options:");
            System.out.println(e);
        }
    }

    /* Writes all vehicle profiles to their file. */
    public static void writeVehicleProfiles(){
        try {
            FileOutputStream fos = new FileOutputStream("log/vehLog");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            LinkedList<VehicleProfile> vehProfiles = VehicleProfile.getVehicleProfiles();
            ListIterator<VehicleProfile> vehIter = vehProfiles.listIterator();
            while ( vehIter.hasNext() ){
                VehicleProfile vp = vehIter.next();
                oos.writeObject(vp);
            }
            oos.close();
        } catch (IOException e){
            System.out.println(e);
        }
    }

    /* Writes all environment profiles to their file. */
    public static void writeEnvironmentProfiles(){
        try {
            FileOutputStream fos = new FileOutputStream("log/envLog");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            LinkedList<EnvironmentProfile> envProfiles = EnvironmentProfile.getEnvironmentProfiles();
            ListIterator<EnvironmentProfile> envIter = envProfiles.listIterator();
            while ( envIter.hasNext() ){
                EnvironmentProfile ep = envIter.next();
                oos.writeObject(ep);
            }
            oos.close();
        } catch (IOException e){
            System.out.println(e);
        }
    }

}
