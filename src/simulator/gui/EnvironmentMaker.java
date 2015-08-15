package simulator.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Collections;
import java.lang.Comparable;
import net.miginfocom.swing.MigLayout;

import simulator.*;

/** Contains two components - a Canvas to draw on and a Palette to select
 * drawing utensils. Contains three utensils: select, line, and obstacle.
 * Start and Goal are created initially and can be moved around. Lines and
 * obstacles can be resized using the scroll wheel, or deleted using the delete
 * key. Uses a seperate variable system to Environment, so to obtain a usable
 * Environment, save() must be called.*/
@SuppressWarnings("serial")
public class EnvironmentMaker extends JPanel{

	private static final double METRES_TO_PIXELS = 200.0;	
	
    /* The Canvas must be in one of the following states. */
    private static enum State {
        NONE, SELECT, OBSTACLE, LINE
    }

    /* An ellipse must be one of the following types. */
    private static enum ObjectType {
        LINE, OBSTACLE, START, GOAL
    }

    /* Instance variables. */
    private boolean locked;
    private Canvas canvas;
    private Palette palette;
    
    /* Constructor. */
    public EnvironmentMaker(Environment e){
        // Set window details
        setBorder(BorderFactory.createLineBorder(Color.BLACK) );
        MigLayout layout = new MigLayout(
                "fill",                         // Layout constraints
                "[fill][fill, 100!]",           // Column constraints
                "[fill]");                      // Row constraints
        this.setLayout(layout);
        
        // Parse Environment variables
        LinkedList<MyEllipse> initList = new LinkedList<MyEllipse>();
        LinkedList<Ellipse2D.Double> toAdd = e.getObstacles();
        ListIterator<Ellipse2D.Double> iter = toAdd.listIterator();
        while ( iter.hasNext() ){
            Ellipse2D.Double next = iter.next();
            initList.add(new MyEllipse(ObjectType.OBSTACLE, next) );
        }
        toAdd = e.getLines();
        iter = toAdd.listIterator();
        while ( iter.hasNext() ){
            Ellipse2D.Double next = iter.next();
            initList.add(new MyEllipse(ObjectType.LINE, next) );
        }
        initList.add(new MyEllipse(ObjectType.START, e.getStartX()*METRES_TO_PIXELS-10.0, e.getStartY()*METRES_TO_PIXELS-10.0, 20.0, 20.0));
        initList.add(new MyEllipse(ObjectType.GOAL, e.getGoalX()*METRES_TO_PIXELS-10.0, e.getGoalY()*METRES_TO_PIXELS-10.0, 20.0, 20.0));
        
        // Initialise local variables
        locked = false;
        canvas = new Canvas(initList);
        this.add(canvas, "cell 0 0, grow");
        palette = new Palette(canvas);
        this.add(palette, "cell 1 0");
    }

    /* Parses and stores canvas data and places it into the provided environment. */
    public void save(Environment e){
        // Initialise data to be filled
        LinkedList<Ellipse2D.Double> obstacles = new LinkedList<Ellipse2D.Double>();
        LinkedList<Ellipse2D.Double> lines = new LinkedList<Ellipse2D.Double>();
        double startX = 0;
        double startY = 0;
        double goalX = 0;
        double goalY= 0;

        // Convert canvas data into environment data
        LinkedList<MyEllipse> objects = canvas.getObjects();
        ListIterator<MyEllipse> iter = objects.listIterator();
        while ( iter.hasNext() ){
            MyEllipse next = iter.next();
			double x = next.ellipse.getX() / METRES_TO_PIXELS;
			double y = next.ellipse.getY() / METRES_TO_PIXELS;
			double w = next.ellipse.getWidth() / METRES_TO_PIXELS;
			double h = next.ellipse.getHeight() / METRES_TO_PIXELS;
			Ellipse2D.Double ellipse = new Ellipse2D.Double(x, y, w, h);
            switch (next.type){
                case OBSTACLE:
                    obstacles.add(ellipse);
                    break;
                case LINE:
                    lines.add(ellipse);
                    break;
                case START:
                    startX = ellipse.getX() + ellipse.getWidth()/2.0;
                    startY = ellipse.getY() + ellipse.getHeight()/2.0;
                    break;
                case GOAL:
                    goalX = ellipse.getX() + ellipse.getWidth()/2.0;
                    goalY = ellipse.getY() + ellipse.getHeight()/2.0;
                    break;
            }
        }

        // Set the environment
        e.setObstacles(obstacles);
        e.setLines(lines);
        e.setStart(startX, startY);
        e.setGoal(goalX, goalY);
    }

    /* Prevents alteration. */
    public void lock(){
        canvas.setState(State.NONE);
        palette.lock();
    }

    /*******************
     * MyEllipse class *
     *******************/
    private class MyEllipse implements Comparable<MyEllipse>{
        public Ellipse2D.Double ellipse;
        public ObjectType type;

        /* Constructors. basically an Ellipse2D.Double but with a classification. Values are given
		for pixel coordinates.*/
        public MyEllipse(ObjectType type, double x, double y, double w, double h){
            ellipse = new Ellipse2D.Double(x, y, w, h);
            this.type = type;
        }
		/* Ellipse is given with values in metres, then converted to pixels. */
        public MyEllipse(ObjectType type, Ellipse2D.Double ellipse){
			double x = ellipse.getX() * METRES_TO_PIXELS;
			double y = ellipse.getY() * METRES_TO_PIXELS;
			double w = ellipse.getWidth() * METRES_TO_PIXELS;
			double h = ellipse.getHeight() * METRES_TO_PIXELS;
			Ellipse2D.Double newEllipse = new Ellipse2D.Double(x, y, w, h);
            this.ellipse = newEllipse;
            this.type = type;
        }

        /* Adjusts the ellipses position. */
        public void move(double dx, double dy){
            double x = ellipse.getX();
            double y = ellipse.getY();
            double w = ellipse.getWidth();
            double h = ellipse.getHeight();
            ellipse = new Ellipse2D.Double(x+dx, y+dy, w, h);
        }

        /* Comparable overrides. */

        /* Compares two ellipses for ordering. The ordering reflects which ones
         * should be displayed first. */
        @Override
        public int compareTo(MyEllipse e){
            switch (e.type){
                case LINE:
                    switch (type){
                        case LINE:
                            return 0;
                        case OBSTACLE:
                            return 1;
                        case START:
                            return 2;
                        case GOAL:
                            return 3;
                    }
                    break;
                case OBSTACLE:
                    switch (type){
                        case LINE:
                            return -1;
                        case OBSTACLE:
                            return 0;
                        case START:
                            return 1;
                        case GOAL:
                            return 2;
                    }
                    break;
                case START:
                    switch (type){
                        case LINE:
                            return -2;
                        case OBSTACLE:
                            return -1;
                        case START:
                            return 0;
                        case GOAL:
                            return 1;
                    }
                    break;
                 case GOAL:
                    switch (type){
                        case LINE:
                            return -3;
                        case OBSTACLE:
                            return -2;
                        case START:
                            return -1;
                        case GOAL:
                            return 0;
                    }
                    break;  
            }
            return 0;
        }
    }
    
    /****************
     * Canvas class *
     ****************/
    private class Canvas extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener{
        private double pointerSize;
        private LinkedList<MyEllipse> objects;  // Obstacles, lines, start or goal
        private boolean circleSelected;         // True if any circle is selected
        private Point previousSelectedPoint;    // Used to calculate distance moved for click-drag
        private LinkedList<MyEllipse> selectedCircles;    // Each element is a pointer to an ellipse
        private boolean newlySelected;          // When a new circle is added upon press, we dont want to remove it from click
        private Point selectedWindowStart;      // Click-drag window start point
        private Rectangle2D selectedWindow;     // Click-drag window
        private State state;  

        /* Constructor. Set defaults. */
        public Canvas(){
            // Window stuff
            setBackground(Color.WHITE);
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
            addKeyListener(this);
            setFocusable(true);

            // Initial values
            pointerSize = 1.0;
            objects = new LinkedList<MyEllipse>();
            objects.add(new MyEllipse(ObjectType.START, 10.0, 10.0, 20.0, 20.0));
            objects.add(new MyEllipse(ObjectType.GOAL, 500.0, 500.0, 20.0, 20.0));
            circleSelected = false;
            previousSelectedPoint = null;
            selectedCircles = new LinkedList<MyEllipse>();
            selectedWindowStart = null;
            selectedWindow = null;
            state = State.SELECT;
        }

        /* Constructor. Copy an already made objects list. Must contain a start
         * and goal, as there is no check that both exist. */
        public Canvas(LinkedList<MyEllipse> initObjects){
            // Window stuff
            setBackground(Color.WHITE);
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
            addKeyListener(this);
            setFocusable(true);
            pointerSize = 1.0;

            // Initial values
            objects = initObjects;
            circleSelected = false;
            previousSelectedPoint = null;
            selectedCircles = new LinkedList<MyEllipse>();
            selectedWindowStart = null;
            selectedWindow = null;
            state = State.SELECT;

            Collections.sort(objects);
        }
        
        /* JPanel overrides. */
        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Draw objects (list is sorted so objects on the bottom get painted
            // first).
            ListIterator<MyEllipse> iter = objects.listIterator();
            while (iter.hasNext() ){
                MyEllipse next = iter.next();
                if ( next.type == ObjectType.LINE ){
                    g2d.setColor(Color.LIGHT_GRAY);
                }
                if ( next.type == ObjectType.OBSTACLE ){
                    g2d.setColor(Color.BLACK);
                }
                if ( next.type == ObjectType.START ){
                    g2d.setColor(Color.BLUE);
                }
                if ( next.type == ObjectType.GOAL ){
                    g2d.setColor(Color.GREEN);
                }
                g2d.fill(next.ellipse);
            }

            // Draw selection indicators
            if ( !circleSelected ){
                if ( selectedWindow != null){
                    g2d.setColor(Color.BLUE);
                    g2d.draw(selectedWindow);
                }
            } 
            ListIterator<MyEllipse> selIter = selectedCircles.listIterator();
            while ( selIter.hasNext() ){
                MyEllipse selectedCircle = selIter.next();
                Rectangle2D bounds = selectedCircle.ellipse.getBounds2D();
                g2d.setColor(Color.BLUE);
                g2d.draw(bounds);
            }
                
            // Draw the mouse follower
            Point p = this.getMousePosition();
            if (p == null) return;
            double x = p.getX();
            double y = p.getY();
            switch (state){
                case OBSTACLE:
                    Ellipse2D.Double obs = new Ellipse2D.Double(x-pointerSize*10.0, y-pointerSize*10.0, pointerSize*20.0, pointerSize*20.0);
                    g2d.setColor(Color.BLACK);
                    g2d.draw(obs);
                    break;
                case LINE:
                    Ellipse2D.Double lin = new Ellipse2D.Double(x-pointerSize*3.0, y-pointerSize*3.0, pointerSize*6.0, pointerSize*6.0);
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.draw(lin);
                    break;
                default:
                    break;
            }
        }

        /* Mouse Listener overrides. */

        // Mouse press and release with no drag in between.
        //      SELECT: Deselect an object or clear selection.
        @Override
        public void mouseClicked(MouseEvent e){
            // Don't deselect a newly selected object
            if (newlySelected){
                newlySelected = false;
                return;
            }
            
            // Handle click
            Point p = this.getMousePosition();
            if (p == null) return;
            double x = p.getX();
            double y = p.getY();
            switch (state){
                case SELECT:
                    // Find what object was clicked
                    ListIterator<MyEllipse> iter = objects.listIterator();
                    while ( iter.hasNext() ){
                        MyEllipse next = iter.next();
                        
                        double radius = next.ellipse.getWidth()/2.0;
                        double centerX = next.ellipse.getX() + radius;
                        double centerY = next.ellipse.getY() + radius;
                        if ( (x-centerX)*(x-centerX) + (y-centerY)*(y-centerY) <= radius*radius){
                            if ( selectedCircles.contains(next) ){
                                selectedCircles.remove(next);
                            }
                            break;
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        // Mouse enters the canvas.
        @Override
        public void mouseEntered(MouseEvent e){}

        // Mouse leaves the window: deselect everything.
        @Override
        public void mouseExited(MouseEvent e){
            selectedCircles.clear();
            selectedWindow = null;
            repaint();
        }

        // Mouse is pressed:
        //      SELECT: Establish a new selection window or selected object.
        @Override
        public void mousePressed(MouseEvent e){
            // Handle press
            Point p = this.getMousePosition();
            if (p == null) return;
            requestFocusInWindow();
            double x = p.getX();
            double y = p.getY();
            switch (state){
                case SELECT:
                    previousSelectedPoint = p;
                    boolean toClear = true;
                    // Find what object was clicked
                    ListIterator<MyEllipse> iter = objects.listIterator();
                    while ( iter.hasNext() ){
                        MyEllipse next = iter.next();
                        double radius = next.ellipse.getWidth()/2.0;
                        double centerX = next.ellipse.getX() + radius;
                        double centerY = next.ellipse.getY() + radius;
                        if ( (x-centerX)*(x-centerX) + (y-centerY)*(y-centerY) <= radius*radius){
                            if ( selectedCircles.contains(next) ){
                                newlySelected = false;
                                toClear = false;
                            } else {
                                selectedCircles.add(next);
                                newlySelected = true;
                                toClear = false;
                            }
                            break;
                        }
                    }

                    // Give up and draw a window
                    circleSelected = true;
                    if (selectedCircles.isEmpty() || toClear){
                        circleSelected = false;
                        selectedCircles.clear();
                        selectedWindowStart = p;
                        selectedWindow = new Rectangle2D.Double(x, y, 0, 0);
                    }
                    break;
                default:
                    break;
            }
        }

        // Mouse is released:
        //      SELECT: Possibly clear a selection window
        //      OBSTACLE: Draw a new obstacle.
        //      LINE: Draw a new line.
        @Override
        public void mouseReleased(MouseEvent e){
            // Handle release
            Point p = this.getMousePosition();
            if (p == null) return;
            double x = p.getX();
            double y = p.getY();
            switch (state){
                case SELECT:
                    selectedWindowStart = null;
                    selectedWindow = null;
                    break;
                case OBSTACLE:
                    MyEllipse obs = new MyEllipse(ObjectType.OBSTACLE, x-pointerSize*10.0, y-pointerSize*10.0, pointerSize*20.0, pointerSize*20.0);
                    objects.add(obs);
                    Collections.sort(objects);
                    break;
                case LINE:
                    MyEllipse lin = new MyEllipse(ObjectType.LINE, x-pointerSize*3.0, y-pointerSize*3.0, pointerSize*6.0, pointerSize*6.0);
                    objects.add(lin);
                    Collections.sort(objects);
                    break;
                default:
                    break;
            }
            repaint();
        }

        /* Mouse Motion Listener overrides. */

        // Mouse moves: repaint to allow pointer following.
        @Override
        public void mouseMoved(MouseEvent me){
            repaint();
        }

        // Mouse is dragged:
        //      SELECT: Resizes a selection window, or moves the currently selected object.
        //      LINE: Draw new lines.
        @Override
        public void mouseDragged(MouseEvent me){
            // Handle drag
            Point p = this.getMousePosition();
            if (p == null) return;
            double x = p.getX();
            double y = p.getY();
            switch (state){
                case SELECT:
                    if ( !circleSelected){  // Draw window
                        double startX = selectedWindowStart.getX();
                        double startY = selectedWindowStart.getY();
                        double w = (x - startX);
                        double h = (y - startY);
                        double topLeftX = startX;
                        double topLeftY = startY;
                        if (startY > y){
                            topLeftY = y;
                            h = -h;
                        }
                        if (startX > x){
                            topLeftX = x;
                            w = -w;
                        }
                        selectedWindow = new Rectangle2D.Double(topLeftX, topLeftY, w, h);

                        // Detect collision with other objects
                        selectedCircles.clear();
                        ListIterator<MyEllipse> iter = objects.listIterator();
                        while ( iter.hasNext() ){
                            MyEllipse next = iter.next();
                            Rectangle2D rect = next.ellipse.getBounds2D();
                            if ( selectedWindow.intersects(rect) ){
                                selectedCircles.add(next);
                            }
                        }


                    } else {  // Move selected circles
                        ListIterator<MyEllipse> circIter = selectedCircles.listIterator();
                        while (circIter.hasNext() ){
                            MyEllipse selectedCircle = circIter.next();
                            ObjectType type = selectedCircle.type;
                            double dx = x - previousSelectedPoint.getX();
                            double dy = y - previousSelectedPoint.getY();
                            
                            selectedCircle.move(dx, dy);
                        }
                    }
                    previousSelectedPoint = p;
                    break;
                case LINE:
                    MyEllipse lin = new MyEllipse(ObjectType.LINE, x-pointerSize*3.0, y-pointerSize*3.0, pointerSize*6.0, pointerSize*6.0);
                    objects.add(lin);
                    Collections.sort(objects);
                    break;
                default:
                    break;
            }
            repaint();
        }

        /* Mouse Wheel Listener overrides. */

        // Mouse wheel is scrolled.
        //      OBSTACLE: resize obstacle following pointer.
        //      LINE: resize line following pointer.
        @Override
        public void mouseWheelMoved(MouseWheelEvent e){
            int notches = e.getWheelRotation();
            if (notches < 0) {
                pointerSize+=0.1;
            } else {
                if (pointerSize > 0.1) pointerSize-=0.1;
                
            }
            repaint();
        }

        /* KeyListener overrides. */

        // A key is pressed.
        @Override
        public void keyPressed(KeyEvent e){}

        // Delete is released:
        //      SELECT: Deletes the currently selected object (not start/goal)
        @Override
        public void keyReleased(KeyEvent e){
            int code = e.getKeyCode();
            if (code == KeyEvent.VK_DELETE){
                switch (state){
                    case SELECT:
                        ListIterator<MyEllipse> circIter = selectedCircles.listIterator();
                    
                        while (circIter.hasNext() ){
                            MyEllipse selectedCircle = circIter.next();
                            if (selectedCircle.type == ObjectType.START) continue;
                            if (selectedCircle.type == ObjectType.GOAL) continue;
                        
                            objects.remove(selectedCircle);
                        }
                        selectedCircles.clear();
                        break;
                    default:
                        break;
                }
                repaint();
            }
        }

        //A key is typed.
        @Override
        public void keyTyped(KeyEvent e){}
        
        /* Getters. */
        public LinkedList<MyEllipse> getObjects(){
            return objects;
        }
        
        /* Setters.*/
        public void setState(State state){
            this.state = state;
            pointerSize = 1.0;
            
        }
    }

    /*****************
     * Palette class *
     *****************/
    private class Palette extends JPanel implements ActionListener {
        // Pointer to the canvas
        private Canvas canvas;

        // Display variables
        private JRadioButton selectRButton;
        private JRadioButton obstacleRButton;
        private JRadioButton lineRButton;

        /* Constructor. */
        public Palette(Canvas canvas){
            this.canvas = canvas;

            // Set up window
            setBackground(Color.WHITE);
            MigLayout layout = new MigLayout(
                "fill",                         // Layout constraints
                "[fill]",           // Column constraints
                "[sg, 40!][sg][sg][]");   // Row constraints
            this.setLayout(layout);

            // Create components
            selectRButton = new JRadioButton("Select", null, true);
            selectRButton.addActionListener(this);
            selectRButton.setActionCommand("selectRButton");
            obstacleRButton = new JRadioButton("Obstacle");
            obstacleRButton.addActionListener(this);
            obstacleRButton.setActionCommand("obstacleRButton");
            lineRButton = new JRadioButton("Line");
            lineRButton.addActionListener(this);
            lineRButton.setActionCommand("lineRButton");

            // Display
            this.add(selectRButton, "cell 0 0");
            this.add(obstacleRButton, "cell 0 1");
            this.add(lineRButton, "cell 0 2");
        }

        /* Prevent alteration. */
        public void lock(){
            selectRButton.setEnabled(false);
            selectRButton.setSelected(false);
            obstacleRButton.setEnabled(false);
            obstacleRButton.setSelected(false);
            lineRButton.setEnabled(false);
            lineRButton.setSelected(false);
        }
        
        /* When buttons are pressed, this is where it's handled. All events have a
         * command that matches the variable name of the source. */
        @Override
        public void actionPerformed( ActionEvent e){
            String event = e.getActionCommand();

            // Handle button change - if selected, deselect all others and set
            // state. If deselected, set state to none.
            if (event.equals("selectRButton") ){
                if (selectRButton.isSelected() ){
                    obstacleRButton.setSelected(false);
                    lineRButton.setSelected(false);
                    canvas.setState(State.SELECT);
                } else {
                    canvas.setState(State.NONE);
                }
            }
            if (event.equals("obstacleRButton") ){
                if (obstacleRButton.isSelected() ){
                    selectRButton.setSelected(false);
                    lineRButton.setSelected(false);
                    canvas.setState(State.OBSTACLE);
                } else {
                    canvas.setState(State.NONE);
                }
            }
            if (event.equals("lineRButton") ){
                if (lineRButton.isSelected() ){
                    selectRButton.setSelected(false);
                    obstacleRButton.setSelected(false);
                    canvas.setState(State.LINE);
                } else {
                    canvas.setState(State.NONE);
                }
            }
        }
    }
}
