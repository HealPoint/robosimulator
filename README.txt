Autonomous Navigation Simulator
Written by Nick Sullivan, University of Adelaide, 2015

OVERVIEW
This program simulates an autonomous robot attempting to move to a position while navigating static obstacles.
It can be used to compare different navigational methods, as well as the theoretical performance of a vehicle.

USER INFORMATION
This program requires the Java Runtime Environment (JRE), and was tested on a Windows 7/8, and Linux(Ubuntu) 
machine. It should be able to be executed in other operating systems with some small changes to the bat/bash
files, but this is untested.

Execute run.bat or run.bash to run the program.

Optionally, execute compile.bat or compile.bash to recompile the program (requires Java Development Kit and 
should not be necessary unless the program was altered).


DEVELOPER INFORMATION
This program has three maps, the actual map - which stores all obstacles and white lines, the observed map - 
which stores all points that have been detected by the robot (generated at run time), and the discrete map - 
which takes the observed map and places the points into a grid. Each map has an assosiated GUI. Each GUI listens 
to the map and is alerted to changes. The GUI's then take a snapshot of the Map and display the information.

The program is split into two threads, the sensor thread - performing LIDAR scan, Camera scan, and path 
calculation, and the mover thread - performing path smoothing and vehicle moving. 
The Main class initialises variables, then acts as the sensor thread. The LIDAR scan and Camera scan uses object 
information in the RealMap, and passes these values to the ObservedMap and DiscreteMap. The path calculation is 
done by a Navigator using the DiscreteMap grid. The Navigator can be any class that implements the Navigator 
interface, allowing plug and play functionality for different navigation algorithms. 
The MotorMover class acts as the mover thread, it grabs the DiscreteMap's path, then alters the position of the 
vehicle using the PositionEstimator class. PositionEstimator gives RealMap the real vehicle position, and gives 
ObservedMap and DiscreteMap data with accuracy and update frequency according to the vehicle statistics. These 
statistics are stored in the class VehicleStats.