/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, University of Adelaide.
*/

package simulator.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ConcurrentModificationException;

import simulator.maps.ObservedMap;
import simulator.interfaces.MapListener;


public class ObservedMapGUI implements MapListener{
	private static final double DIAMETER = 3.0;		//Circle size in pixels
	private static final double DIAMETER_VEH = 10.0;	//Circle size of the vehicle in pixels

	private ObservedMap map;			//Link to the map we're displaying
	private MyCanvas canvas;			//An overriden JComponent for drawing on
	
	private JFrame window;				//The window
	private JPanel panel;				//panel inside window

	
	public ObservedMapGUI(ObservedMap map){
		//Establish connections
		this.map = map;
		map.addListener(this);
		canvas = new MyCanvas(map.getWidth(), map.getHeight());
		canvas.setMutexLock(map.getMutexLockGui());
		canvas.setNewPoints(map.getNewPoints());
		canvas.setOldPoints(map.getOldPoints());
	}
	
	/*Displays the GUI. Should only be called once, after initialisation.*/
	public void display(){
		//Create the window
		window = new JFrame("Observed Map");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocation(575, 50);
		window.setResizable(true);
		
		//Create the window panel
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setPreferredSize(new Dimension(500, 500));
		panel.setBackground(Color.WHITE);
		window.setContentPane(panel);
		
		//Add the display
		panel.add(canvas);
		
		//Display the window
		window.pack();
		window.setVisible(true);	
	}
	
	/********************************************
	* Listener methods 							*
	*********************************************/
	
	/* Update primitives and repaint. Objects do not need to be updated as they use pointers. */
	public void mapHasChanged(){
		canvas.setVehiclePosition(map.getVehiclePosX(), map.getVehiclePosY(), map.getVehicleAng());
		canvas.setDestinationPosition(map.getDestinationPosX(), map.getDestinationPosY());
		canvas.repaint();
	}
	
	
	/********************************************
	* Canvas to draw on							*
	*********************************************/
	private class MyCanvas extends JComponent {
		private double width;		//The width of the map, in m
		private double height;		//The height of the map, in m
		private double scalingX;	// = frameWidth / mapWidth. Changes from map coord to pixel coord for points in mm
		private double scalingY;
		private double vehiclePosX;	//The X position of the vehicle, in m (pixel coordinates)
		private double vehiclePosY;	//The Y position of the vehicle, in m (pixel coordinates)
		private double vehicleAng;	//The orientation of the vehicle, in degrees (CCW from East)
		private double destPosX;		//The X position of the destination, in m (pixel coordinates);
		private double destPosY;		//The Y position of the destination, in m (pixel coordinates);
		
		//These variables are altered by the map thread, so all reading of them must be done with
		// the mutex lock.
		private LinkedList<Point> oldPoints;	//Old points will be marked as black
		private LinkedList<Point> newPoints;	//New points will be marked as red
		private Object mutexLock;
	
		/* Constructor, set defaults. */
		public MyCanvas(double width, double height){
			this.width = width;
			this.height = height;
			oldPoints = new LinkedList<Point>();
			newPoints = new LinkedList<Point>();
			mutexLock = new Object();
			vehiclePosX = 0;
			vehiclePosY = 0;
			vehicleAng = 0;
			destPosX = 0;
			destPosY = 0;
		}
		
		/* Paints the map. This should never be called directly. Use repaint(). */
		@Override
		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			Dimension size = getSize();
			scalingX = size.getWidth() / (width*1000.0);		//The width is given in metres, but the points are stored in mm
			scalingY = size.getHeight() / (height*1000.0);
			double diameter = DIAMETER;	//in pixels
			
			//Displaying objects must be thread safe
			synchronized(mutexLock){

				//Add all the old points
				g2d.setColor(Color.BLACK);
				ListIterator<Point> iterator = oldPoints.listIterator(0);
				while (iterator.hasNext()){
					Point p = iterator.next();
					double x = p.getX()*scalingX - diameter/2.0;
					double y = p.getY()*scalingY - diameter/2.0;
					Ellipse2D.Double circ = new Ellipse2D.Double(x, y, diameter, diameter);
					g2d.fill(circ);
				}
				
				//Add all the new points
				g2d.setColor(Color.RED);
				iterator = newPoints.listIterator(0);
				while (iterator.hasNext()){
					Point p = iterator.next();
					double x = p.getX()*scalingX - diameter/2.0;
					double y = p.getY()*scalingY - diameter/2.0;
					Ellipse2D.Double circ = new Ellipse2D.Double(x, y, diameter, diameter);
					g2d.fill(circ);
				}
			}
			//Add the destination
			g2d.setColor(Color.GREEN);
			double destX = destPosX*scalingX*1000 - DIAMETER_VEH/2.0;
			double destY = destPosY*scalingY*1000 - DIAMETER_VEH/2.0;
			g2d.fill(new Ellipse2D.Double(destX, destY, DIAMETER_VEH, DIAMETER_VEH));
			
			//Add the vehicle
			g2d.setColor(Color.BLUE);
			double x = vehiclePosX*scalingX*1000 - DIAMETER_VEH/2.0;
			double y = vehiclePosY*scalingY*1000 - DIAMETER_VEH/2.0;
			g2d.fill(new Ellipse2D.Double(x, y, DIAMETER_VEH, DIAMETER_VEH));
		}
		
		/********************************************
		* Getters and Setters						*
		*********************************************/
		/* These are called once to set the pointers. */
		
		public void setMutexLock(Object mutexLock){
			this.mutexLock = mutexLock;
		}
		public void setNewPoints(LinkedList<Point> newPoints){
			this.newPoints = newPoints;
			repaint();
		}
		public void setOldPoints(LinkedList<Point> oldPoints){
			this.oldPoints = oldPoints;
			repaint();
		}
		
		/* These are called every time map is changed. */
		
		public void setVehiclePosition(double posX, double posY, double ang){
			vehiclePosX = posX;
			vehiclePosY = posY;
			vehicleAng = ang;
		}
		public void setDestinationPosition(double posX, double posY){
			destPosX = posX;
			destPosY = posY;
		}
		
	}
}
