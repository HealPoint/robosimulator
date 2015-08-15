/**This class is part of the Autonomous Navigation Simulator 2015,
Written by Nick Sullivan, University of Adelaide.
*/

package simulator.gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ConcurrentModificationException;

import simulator.maps.DiscreteMap;
import simulator.interfaces.MapListener;
import simulator.interfaces.NavigatorNode;


public class DiscreteMapGUI implements MapListener{
	private DiscreteMap map;			//Link to the map we're displaying
	
	private JFrame window;				//The window
	private JPanel panel;				//panel inside window
	private Grid grid;
	
	public DiscreteMapGUI(DiscreteMap map){
		//Establish connections
		this.map = map;
		map.addListener(this);
		grid = new Grid(map.getNumTilesY(), map.getNumTilesX());
		grid.setMutexLock(map.getMutexLock());
		grid.setGrid(map.getGrid());
	}
	
	/*Displays the GUI. Should only be called once, after initialisation.*/
	public void display(){
		//Create the window
		window = new JFrame("Discrete Map");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocation(1100, 50);
		window.setResizable(true);
		
		//Create the window panel
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setPreferredSize(new Dimension(500, 500));
		window.setContentPane(panel);
		
		//Add the grid
		panel.add(grid);
		
		//Display the window
		window.pack();
		window.setVisible(true);	
	}
	
	/********************************************
	* Listener methods 							*
	*********************************************/
	public void mapHasChanged(){
		grid.setPath(map.getPath());
		grid.setVehiclePosition(map.getVehicleTileX(), map.getVehicleTileY());
		grid.setDestinationPosition(map.getDestinationTileX(), map.getDestinationTileY());
		grid.repaint();
	}
	
	/********************************************
	* Canvas to draw on							*
	*********************************************/
	private class Grid extends JComponent{
		
		private int rows;					//The number of rows
		private int cols;					//The number of columns
		private int tileWidth;				//Pixel width of each tile
		private int tileHeight;				//Pixel height of each tile
		private int vehicleTileX;			//Vehicle position in the grid
		private int vehicleTileY;
		private int destTileX;				//The position of the destination in the grid
		private int destTileY;
		
		//These variables are altered by the map thread, so all reading of them must be done with
		// the mutex lock.
		private List<NavigatorNode> path;			//The order of nodes to go through to reach the destination (set by calculatePath())
													//Each node contains the x,y position of grid
		private int[][] grid;						//DiscreteMap's grid
		private Object mutexLock;
		
		/* Constructor, set defaults. */
		public Grid(int rows, int cols){
			this.rows = rows;
			this.cols = cols;
			vehicleTileX = 0;
			vehicleTileY = 0;
			destTileX = 0;
			destTileY = 0;
			grid = new int[cols][rows];
			path = new LinkedList<NavigatorNode>();
			mutexLock = new Object();
			setBackground(Color.BLACK);
		}
		
		/* Paints the map. This should never be called directly. Use repaint(). */
		@Override
		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			Dimension size = getSize();
			tileWidth = (int) (size.getWidth() / cols);
			tileHeight = (int) (size.getHeight() / rows);
			
			Color[][] gridColors = new Color[cols][rows];
			
			//Calculate colors from grid
			for (int i=0; i<rows; i++){
				for (int j=0; j<cols; j++){
					Color color = Color.WHITE;
					
					if (grid[j][i] == 0){
						color = Color.WHITE;
					}
					if (grid[j][i] > 0){
						color = Color.GRAY;
					}
					
					gridColors[j][i] = color;
				}
			}
			//Calculate color from path
			synchronized(mutexLock){
				ListIterator<NavigatorNode> iterator = path.listIterator(0);
				while (iterator.hasNext()){
					NavigatorNode n = iterator.next();
					int x = n.getX();
					int y = n.getY();
					gridColors[x][y] = Color.YELLOW;
				}
			}
			
			//Calculate color from destination
			gridColors[destTileX][destTileY] = Color.GREEN;
			
			//Calculate color from vehicle
			gridColors[vehicleTileX][vehicleTileY] = Color.BLUE;
			
			//Paint the grid
			for (int i=0; i<rows; i++){
				for (int j=0; j<cols; j++){
					g2d.setColor(gridColors[j][i]);
					int x = j*tileWidth;
					int y = i*tileHeight;
					Rectangle rect = new Rectangle(x, y, tileWidth, tileHeight);
					g2d.fill(rect);
					
					//Draw the outline
					g2d.setColor(Color.BLACK);
					g2d.draw(rect);
					
				}
			}	
		}
		
		/********************************************
		* Getters and Setters						*
		*********************************************/
		/* These are called once to set the pointers. */
		
		public void setMutexLock(Object mutexLock){
			this.mutexLock = mutexLock;
		}
		public void setGrid(int[][] grid){
			this.grid = grid;
		}
		
		
		/* These are called every time map is changed. */
		
		/* A new path is generated each time, (not altered in place). */
		public void setPath(List<NavigatorNode> path){
			this.path = path;
		}
		public void setVehiclePosition(int tileX, int tileY){
			vehicleTileX = tileX;
			vehicleTileY = tileY;
		}
		public void setDestinationPosition(int tileX, int tileY){
			destTileX = tileX;
			destTileY = tileY;
		}
		
	}
}
