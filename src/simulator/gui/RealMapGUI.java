/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, University of Adelaide.
*/

package simulator.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ConcurrentModificationException;

import simulator.maps.RealMap;
import simulator.interfaces.MapListener;

/** The map GUI's display changes that are made to the map. Objects (such as lists), use pointers
 with a mutex lock. Primitives are updated through listeners. **/
public class RealMapGUI implements MapListener{
	private RealMap map;				//Link to the map we're displaying
	private MyCanvas canvas;			//An overriden JComponent for drawing on
	
	private JFrame window;				//The window
	private JPanel panel;				//panel inside window

	public RealMapGUI(RealMap map, boolean trails){
		//Establish connections
		this.map = map;
		map.addListener(this);
		canvas = new MyCanvas(map.getWidth(), map.getHeight());
		
		//Get pointers
		canvas.setMutexLock(map.getMutexLockGui());
		canvas.setObstacles(map.getObstacles());
		canvas.setLines(map.getLines());
		canvas.setLasers(map.getLasers());
		canvas.setVehiclePosition(map.getVehiclePosX(), map.getVehiclePosY(), map.getVehicleAng());
		canvas.setTrails(trails);
	}
	
	/*Displays the GUI. Should only be called once, after initialisation.*/
	public void display(){
		
		//Create the window
		window = new JFrame("Real Map");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocation(50, 50);
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
		private double vehiclePosX;		//The X position of the vehicle, in m (pixel coordinates)
		private double vehiclePosY;		//The Y position of the vehicle, in m (pixel coordinates)
		private double vehicleAng;		//The orientation of the vehicle, in degrees (CCW from East)
		private double destPosX;		//The X position of the destination, in m (pixel coordinates)
		private double destPosY;		//The Y position of the destination, in m (pixel coordinates)
		private boolean trails;		//If true, display a blue trail behind the vehicle
		
		
		//These variables are altered by the map thread, so all reading of them must be done with
		// the mutex lock.
		private LinkedList<Ellipse2D.Double> obstacles;
		private LinkedList<Ellipse2D.Double> lines;		
		private LinkedList<Line2D.Double> lasers;		//In m,m - pixel coordinates
		private LinkedList<Ellipse2D.Double> trail;		// A list of where the vehicle has been, in
														// m (pixel coordinate)
		private Object mutexLock;
		
		/* Constructor. Set defaults. */
		public MyCanvas(double width, double height){
			this.width = width;
			this.height = height;
			obstacles = new LinkedList<Ellipse2D.Double>();
			lines = new LinkedList<Ellipse2D.Double>();
			lasers = new LinkedList<Line2D.Double>();
			trail = new LinkedList<Ellipse2D.Double>();
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
			
			//Displaying objects must be thread safe
			synchronized(mutexLock){
				//Add all the obstacles
				g2d.setColor(Color.BLACK);
				ListIterator<Ellipse2D.Double> iter = obstacles.listIterator(0);
				while (iter.hasNext()){
					Ellipse2D.Double circ = iter.next();
					double normX = circ.getX() / width;	//normalise position
					double normW = circ.getWidth() / width;
					double normY = circ.getY() / height;
					double normH = circ.getHeight() / height;
					
					double x = normX * size.getWidth();
					double w = normW * size.getWidth();
					double y = normY * size.getHeight();
					double h = normH * size.getHeight();
					g2d.fill(new Ellipse2D.Double(x, y, w, h));
				}
				
				//Add all the lines
				g2d.setColor(Color.GRAY);
				iter = lines.listIterator(0);
				while (iter.hasNext()){
					Ellipse2D.Double circ = iter.next();
					double normX = circ.getX() / width;	//normalise position
					double normW = circ.getWidth() / width;
					double normY = circ.getY() / height;
					double normH = circ.getHeight() / height;
					
					double x = normX * size.getWidth();
					double w = normW * size.getWidth();
					double y = normY * size.getHeight();
					double h = normH * size.getHeight();
					g2d.fill(new Ellipse2D.Double(x, y, w, h));
				}
				
				//Add the trail
				if (trails){
					g2d.setColor(Color.BLUE);
					iter = trail.listIterator(0);
					while (iter.hasNext()){
						Ellipse2D.Double circ = iter.next();
						double normX = circ.getX() / width;	//normalise position
						double normW = circ.getWidth() / width;
						double normY = circ.getY() / height;
						double normH = circ.getHeight() / height;
						
						double x = normX * size.getWidth();
						double w = normW * size.getWidth();
						double y = normY * size.getHeight();
						double h = normH * size.getHeight();
						g2d.fill(new Ellipse2D.Double(x, y, w, h));
					}
				}

				//Add all the lasers
				g2d.setColor(Color.RED);
				ListIterator<Line2D.Double> iter2 = lasers.listIterator(0);
				while (iter2.hasNext()){
					Line2D.Double laser = iter2.next();
					double normX1 = laser.getX1() / width;	//normalise position
					double normX2 = laser.getX2() / width;
					double normY1 = laser.getY1() / height;
					double normY2 = laser.getY2() / height;
					
					double x1 = normX1 * size.getWidth();
					double x2 = normX2 * size.getWidth();
					double y1 = normY1 * size.getHeight();
					double y2 = normY2 * size.getHeight();
									
					g2d.draw(new Line2D.Double(x1, y1, x2, y2));
				}
			}

			//Add the destination
			double diameter = 10.0;
			double normX = destPosX / width;		//normalise position
			double normY = destPosY / height;
			double destX = normX * size.getWidth() - diameter/2.0;			//set to pixel values
			double destY = normY * size.getHeight() - diameter/2.0;
			g2d.setColor(Color.GREEN);
			g2d.fill(new Ellipse2D.Double(destX, destY, diameter, diameter));
				
			//Add the vehicle
			normX = vehiclePosX / width;		//normalise position
			normY = vehiclePosY / height;
			double vehX = normX * size.getWidth();			//set to pixel values
			double vehY = normY * size.getHeight();
			g2d.setColor(Color.BLUE);
			Rectangle2D.Double rectangle = new Rectangle2D.Double(vehX-10.0/2.0, vehY-5.0/2.0, 10.0, 5.0);
			AffineTransform transform = new AffineTransform();
			transform.rotate(-Math.toRadians(vehicleAng), rectangle.getX() + rectangle.width/2, rectangle.getY() + rectangle.height/2);
			Shape transformed = transform.createTransformedShape(rectangle);
			g2d.fill(transformed);
		}
		
		/********************************************
		* Getters and Setters						*
		*********************************************/
		
		/* These are called once to set the pointers. */
		
		public void setMutexLock(Object mutexLock){
			this.mutexLock = mutexLock;
		}
		public void setObstacles(LinkedList<Ellipse2D.Double> obstacles){
			this.obstacles = obstacles;
		}
		public void setLines(LinkedList<Ellipse2D.Double> lines){
			this.lines = lines;
		}
		public void setLasers(LinkedList<Line2D.Double> lasers){
			this.lasers = lasers;
		}
		public void setTrails(boolean trails){
			this.trails = trails;
		}
		
		/* These are called every time map is changed. */
		
		public void setVehiclePosition(double posX, double posY, double ang){
			vehiclePosX = posX;
			vehiclePosY = posY;
			vehicleAng = ang;
			// Add it to the trail list if the vehicle has moved far enough
			synchronized(mutexLock){
				if (trail.size() > 0){
					Ellipse2D.Double old = trail.getLast();
					double oldX = old.getX();
					double oldY = old.getY();
					double dist = (oldX-posX)*(oldX-posX) + (oldY-posY)*(oldY-posY);
					if ( dist > 0.001){
						trail.add(new Ellipse2D.Double(posX, posY, 0.01, 0.01));
					}
				} else {
					trail.add(new Ellipse2D.Double(posX, posY, 0.01, 0.01));
				}
			}
		}
		public void setDestinationPosition(double posX, double posY){
			destPosX = posX;
			destPosY = posY;
		}
		
	}
}
