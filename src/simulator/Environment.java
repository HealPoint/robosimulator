package simulator;


import java.util.LinkedList;
import java.util.ListIterator;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;

/** Contains all values needed to describe an environment for a vehicle to
 * drive on.
 * Assumes:
 *  Ground is flat.
 *  All objects and lines are made up of ellipses. */
public class Environment implements Serializable{

    private static final long serialVersionUID = 42L;
    private LinkedList<Ellipse2D.Double> obstacles;
    private LinkedList<Ellipse2D.Double> lines;
    private double startX;
    private double startY;
    private double goalX;
    private double goalY;
    
    /* Constructor. */
    public Environment(){
        obstacles = new LinkedList<Ellipse2D.Double>();
        lines = new LinkedList<Ellipse2D.Double>();
        startX = 0.1;
        startY = 0.1;
        goalX = 2.0;
        goalY = 2.0;
    }

    /* Getters. */
    public LinkedList<Ellipse2D.Double> getObstacles(){
        return obstacles;
    }
    public LinkedList<Ellipse2D.Double> getLines(){
        return lines;
    }
    public double getStartX(){
        return startX;
    }
    public double getStartY(){
        return startY;
    }
    public double getGoalX(){
        return goalX;
    }
    public double getGoalY(){
        return goalY;
    }
    
    /* Setters. If a value is not acceptable, returns false.*/

    public boolean setObstacles(LinkedList<Ellipse2D.Double> obstacles){
        if (obstacles == null) return false;
        this.obstacles = obstacles;
        return true;
    }
    public boolean setLines(LinkedList<Ellipse2D.Double> lines){
        if (lines == null) return false;
        this.lines = lines;
        return true;
    }
    public boolean setStart(double startX, double startY){
        if (startX < 0) return false;
        if (startY < 0) return false;
        this.startX = startX;
        this.startY = startY;
        return true;
    }
    public boolean setGoal(double goalX, double goalY){
        if (goalX < 0) return false;
        if (goalY < 0) return false;
        this.goalX = goalX;
        this.goalY = goalY;
        return true;
    }
}
