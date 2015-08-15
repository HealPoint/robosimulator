package simulator;


import java.util.LinkedList;
import java.util.ListIterator;
import java.io.Serializable;

/** Instances of this class contain a name and an Environment. The class itself
 * contains a static list of all instances, and a number of methods to access
 * them.
 * Note:
 *   Static initialisation occurs whenever this class is first interacted with.
 *   To create an instance, use the factory constructor. */
public class EnvironmentProfile implements Serializable{
    /** Static side - has a list of all EnvironmentProfiles. **/

    private static LinkedList<EnvironmentProfile> environmentProfiles;

    /* Initialise. */
    static {
        environmentProfiles = new LinkedList<EnvironmentProfile>();
    }
    
    /* Create a new EnvironmentProfile. Name must be unique or this will return
     * null. */
    public static EnvironmentProfile createNewEnvironmentProfile(String name){
        EnvironmentProfile ep = getEnvironmentProfile(name);
        if ( ep!= null){
            return null;
        }
        ep = new EnvironmentProfile(name);
        environmentProfiles.add(ep);
        return ep;
    }
    
    /* Searches for a EnvironmentProfile by name, returns the EnvironmentProfile if
     * one is found, otherwise returns null. */
    public static EnvironmentProfile getEnvironmentProfile(String name){
        ListIterator<EnvironmentProfile> iter = environmentProfiles.listIterator();
        while ( iter.hasNext() ){
            EnvironmentProfile ep = iter.next();
            if ( ep.getName().equals(name) ){
                return ep;
            }
        }
        return null;
    }

    /* Adds an environment profile to the list. Should only be called when a
     * profile was not made with the factory constructor (e.g read from file).*/
    public static void addEnvironmentProfile(EnvironmentProfile env){
        EnvironmentProfile ep = getEnvironmentProfile(env.getName());
        if ( ep!= null){
            return;
        }
        environmentProfiles.add(env);
    }

    /* Obtains a pointer to the vehicle profiles. Treat this as read-only. */
    public static LinkedList<EnvironmentProfile> getEnvironmentProfiles(){
        return environmentProfiles;
    }

    /* Returns a new list with environment profile names, in order. */
    public static LinkedList<String> getNames(){
        LinkedList<String> ret = new LinkedList<String>();
        
        ListIterator<EnvironmentProfile> iter = environmentProfiles.listIterator();
        while ( iter.hasNext() ){
            EnvironmentProfile ep = iter.next();
            ret.add(ep.getName());
        }
        return ret;
    }
    
    /* Removes an environment profile from the list. */
    public static void deleteEnvironmentProfile(String name){
        EnvironmentProfile ep = getEnvironmentProfile(name);
        environmentProfiles.remove(ep);
    }

    /** Non-Static side. **/
    
    private static final long serialVersionUID = 42L;
    private String name;
    private Environment environment;

    /* Constructor. This should not be called outside this class.*/
    public EnvironmentProfile(String name){
       this.name = name;
       environment = new Environment();
    }

    /* Getters. */
    
    public String getName(){
        return name;
    }
    public Environment getEnvironment(){
        return environment;
    }

    /* Setters. If a value is not acceptable, returns false.*/

    public boolean setName(String name){
        if (name == null)   return false;
        if (name.length() < 1)  return false;
        if (this.name.equals(name)) return true;
        if (getEnvironmentProfile(name) != null)    return false;
        
        this.name = name;
        return true;
    }
    public boolean setEnvironment(Environment environment){
        if (environment == null) return false;
        this.environment = environment;
        return true;
    }
    
        
}
