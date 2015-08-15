package simulator;


import java.util.LinkedList;
import java.util.ListIterator;
import java.lang.Class;
import java.io.File;

import simulator.interfaces.Navigator;

/** Purely static, should not be instanciated. Contains a list of all navigators,
 * which is filled using the filenames in the navigators folder.  */
public class Navigators{
    /** Static side - has a list of all Navigators. **/

    private static LinkedList<Navigator> navigators;

    /* Initialise. Loads in all navigators. that appear in the bin location.*/
    static {
        String PACKAGE = "simulator";
        String SUB_PACKAGE = "navigators";

        // Create a list to store all navigators
        navigators = new LinkedList<Navigator>();
        
        // Get list of all files
        File folder = new File("./bin/"+PACKAGE+"/"+SUB_PACKAGE);
        File[] listOfFiles = folder.listFiles();
        
        // Read all the potential Navigators
        for (File file : listOfFiles){
            if ( file.isFile() ){
                // Get file name without ".class"
                String fileName = file.getName();
                fileName = PACKAGE+"."+SUB_PACKAGE+"."+fileName.substring(0, fileName.length()-6);
				if ( !fileName.contains("$") ) {
					// Convert to a Navigator object
					try {
						Class<? extends Object> c = Class.forName(fileName);
						Navigator nav = (Navigator) c.newInstance();
						navigators.add(nav);
						
						//System.out.println(nav.getName());
					} catch (Exception ex){
						 System.out.println("Exception "+ex);
						 //ex.printStackTrace();
					}
				}
            }
        }
    }

    /* Provides a list containing all Navigator names, in the same order as
     * stored. */
    public static String[] getNames(){
        String[] names = new String[navigators.size()];
        int i = 0;
        ListIterator<Navigator> iter = navigators.listIterator();
        while ( iter.hasNext() ){
            
            Navigator nav = iter.next();
            names[i++] = nav.getName();
        }
        return names;
    }
    
    /* Searches for a Navigator by name, returns the Navigator if one is found,
     * otherwise returns null. */
    public static Navigator getNavigator(String name){
        ListIterator<Navigator> iter = navigators.listIterator();
        while ( iter.hasNext() ){
            Navigator nav = iter.next();
            if ( nav.getName().equals(name) ){
                return nav;
            }
        }
        return null;
    }

    /* Returns the first navigator for use by default. */
    public static Navigator getFirstNavigator(){
        if (navigators.size() > 0){
            return navigators.getFirst();
        }
        return null;
    }

    /* Returns the index of a navigator in our list, or -1. */
    public static int indexOf(Navigator nav){
        int i = 0;
        ListIterator<Navigator> iter = navigators.listIterator();
        while ( iter.hasNext() ){
            Navigator n = iter.next();
            if (n.getClass() == nav.getClass() ){
                return i;
            }
            i++;
        }
        return -1;
    }   
        
}
