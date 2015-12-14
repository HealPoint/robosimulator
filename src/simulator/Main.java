package simulator;

import java.util.LinkedList;
import java.util.ListIterator;

import simulator.gui.*;
/** Contains the main function to start up the autonomous vehicle simulator. */
public class Main {


    /* Main. Navigator loads are done statically. */
    public static void main(String args[]){

        // Load Vehicle and Environment profiles
        Logger.readAll();

        // Create the interface
        new UserInterface();
    }
}
