package simulator.navigators;

import java.util.List;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.lang.Comparable;
import java.util.Scanner;
import java.lang.Math;

import simulator.interfaces.Navigator;
import simulator.interfaces.NavigatorNode;

/** D*
 * This is built from LPA*, with the added functionality that the start position
 * can move.
 *
 * Changes from LPA*:
 * start and goal (and succ/pred) are swapped. While the paper swaps the names,
 * for this only the values are changed.
 * 
 * km is introduced to keep track of 'goal' movement, it is effectively a
 * heuristic offset.
 *
 * http://idm-lab.org/bib/abstracts/papers/aaai02b.pdf
 * */
public class DStarLite implements Navigator{

    private static final int INF = -1;
    
    private TreeSet<Node> U;     // Set of inconsistent vertices, a nodes key
                                 // should NOT be altered while inside this list.
    private Node start;                             
    private Node goal;
    private int[][] grid;        // Copy of the obstacle grid (0=clear, 1+=obstacle)
    private Node[][] nodeGrid;   // Stores a pointer to each node, matching the grid
    private boolean firstTime;   // Whether we have initialised
    private int km;

    /* Constructor. Initialise variables that we can. */
    public DStarLite(){
        U = new TreeSet<Node>();
        firstTime = true;
        km = 0;
    }
   
   @Override
	public String getName(){
		return "DStarLite";
	}
	
    /*Given a 2D grid containing obstacle information:
	0 = no obstacle
	1+ = obstacle (the higher the value the more likely this object is to contain an obstacle),
	this function returns the list of nodes (in order) to be traversed to reach the goal
	destination from the start point. 
	
	The grid is accessed by grid[col number][row number], so the start position is at
	grid[startTileX][startTileY].
	
	If a path is unable to be found, this function should return null. */
	@Override
    public List<NavigatorNode> calculatePath(int[][] gri, int startX, int startY, int destX, int destY, 
													double startAngle){
        //System.out.println("Entering calculatePath()");
        LinkedList<NavigatorNode> path = new LinkedList<NavigatorNode>();
        
        // Swap start and dest
        int tempX = startX;
        int tempY = startY;
        startX = destX;
        startY = destY;
        destX = tempX;
        destY = tempY;
        
        // If this is the first time we're called, initialise
        if (firstTime){
            firstTime = false;
            //System.out.println("Create the grid");
            // Create the grid
            nodeGrid = new Node[gri.length][gri[0].length];
            grid = new int[gri.length][gri[0].length];
            for (int i=0; i<gri.length; i++){
                for (int j=0; j<gri[0].length; j++){
                    grid[i][j] = gri[i][j];
                }
            }
            // Create the goal node
            //System.out.println("Create the goal node");
            goal = new Node(destX, destY);
            nodeGrid[destX][destY] = goal;
        
            // Create the start node
            //System.out.println("Create the start node");
            start = new Node(startX, startY);
            nodeGrid[startX][startY] = start;
            start.setRhs(0);
            start.setH( calculateHeur(start) );     
            start.setKey( calculateKey(start) );
        
            // Add the start node to the priority queue
            //System.out.println("Add start to queue");
            U.add(start);

        }
        // If this is not the first time, calculate differences
        else {
            // Create a list of nodes to update
            LinkedList<Node> toUpdate = new LinkedList<Node>();
            
            // Check for movement
            if ( goal.getX() != destX || goal.getY() != destY ){
                // Find how far we've moved
                int oldH = calculateHeur(start);
                goal = nodeGrid[destX][destY];
                if (goal == null){
                    nodeGrid[destX][destY] = new Node(destX, destY);
                    goal = nodeGrid[destX][destY];
                }
                    
                int newH = calculateHeur(start);
                km += oldH - newH;

                //Add to update
                if ( !toUpdate.contains(goal) ){
                    toUpdate.add(goal);
                }
            }

            // Compare each grid index
            for (int i=0; i<gri.length; i++){
                for (int j=0; j<gri[0].length; j++){
                    if (grid[i][j] != gri[i][j]){
                        // Mark nodes for update
                        Node n = nodeGrid[i][j];
						if (n == null){
							nodeGrid[i][j] = new Node(i, j);
							n = nodeGrid[i][j];
						}
                        if ( !toUpdate.contains(n) ){
                            toUpdate.add(n);
                        }
                        
                        // Supposed to add successors, but I don't think it's required
                        //LinkedList<Node> succ = getSuccessors(n);
                        //ListIterator<Node> iter = succ.listIterator();
                        //while(iter.hasNext()){
                        //    Node next = iter.next();
                        //    if ( !toUpdate.contains(next) ) {
                        //        toUpdate.add(next);
                        //    }
                        //}

                        // Copy the new grid
                        grid[i][j] = gri[i][j];
                    }
                }
            }

            // Update
            ListIterator<Node> iter = toUpdate.listIterator();
            while(iter.hasNext()){
                Node next = iter.next();
				//System.out.println(next);
                if ( !U.contains(next) ){
                    updateVertex(next);
                }
            }
        
        }
        
        // Update the path
        computeShortestPath();

        // Store the path
        Node prev = goal;
        //System.out.println("    "+prev.toString());
        while (prev != start){
            LinkedList<Node> pred = prev.getPredecessors();
            ListIterator<Node> iter = pred.listIterator();
            int min = INF;
            Node next = null;
            while( iter.hasNext() ){
                Node n = iter.next();
                int g = n.getG();
                int c = calculateDist(prev, n);
                if (g != INF && c != INF){
                    int val = g + c;
                    if (val < min || min == INF){
                        min = val;
                        next = n;
                    }
                }
            }
            if (next == null) System.out.println("OH NOES");
            prev = next;
            path.add(next);
            System.out.println("    "+prev.toString());
        }
        
		System.out.println("Returning path!");
        return path;
    }

    /* Calculate the key value. Each call will create a new, unique key. Does
     * NOT alter the node.*/
    private Key calculateKey(Node s){
        int g = s.getG();
        int rhs = s.getRhs();
        int h = s.getH();

        // Infinity cases
        if (g == INF && rhs == INF){
            return new Key(INF, INF);
        }
        if (g == INF){
            return new Key(rhs+h+km, rhs);
        }
        if (rhs == INF){
            return new Key(g+h+km, g);
        }

        // Normal case            
        int k0 = Math.min(g,rhs)+h+km;
        int k1 = Math.min(g,rhs);
        
        Key k = new Key(k0, k1);
        return k;
    }

    /* Calculates the distance cost between two vertices. Assumes nodes a and b
     * are next to one another. */
    private int calculateDist(Node a, Node b){
        int ax = a.getX();
        int ay = a.getY();
        int bx = b.getX();
        int by = b.getY();
        
        if ( grid[ax][ay] > 0 || grid[bx][by] > 0 ){
            return INF;
        }
        
        if (ax == bx || ay == by ){
            return 10;
        }
        return 14; 

    }

    /* Calculates the hueristic from a node to the goal. */
    private int calculateHeur(Node a){
        int startX = a.getX();
        int startY = a.getY();
        int destX = goal.getX();
        int destY = goal.getY();
        int h = Math.abs(startX-destX) + Math.abs(startY-destY);
        return h;
    }
    
    /* Updates a vertex. */
    private void updateVertex(Node u){
        //System.out.println("    Entering updateVertex()");
        
        // Remove from priority queue before we alter the value
        if ( U.contains(u) ){
            //System.out.println("removing: " + u.toString() );
            U.remove(u);
        }
        
        // Calculate new rhs
        if ( u != start){
            // Find the minimum rhs+cost from predecessors
            int minRhs = INF;
            LinkedList<Node> pred = u.getPredecessors();
            ListIterator<Node> iter = pred.listIterator();
            while ( iter.hasNext() ){
                Node s = iter.next();
                int g = s.getG();
                int dist = calculateDist(s, u);
                if ( g != INF && dist != INF){
                    int val = g + dist;
                    if (val < minRhs || minRhs == INF){
                        minRhs = val;
                    }
                }
            }
            u.setRhs(minRhs);
        }

        // Set the new key
        u.setKey( calculateKey(u) );

        //printout
        //System.out.println("    Node is now: "+u.toString());
        
        // Add to the priority queue if needed
        if ( u.getG() != u.getRhs() ){
            //System.out.println("adding: " + u.toString() );
            U.add(u);
        }
    }

    /* Get the successors for a node, creating them if need be. Successors are
     * accessable nodes that are not predecessors. If closed, sets all
     * successors as having a new predecessor 'u', otherwise removes 'u' from
     * being the successors' predecessor. */
    private LinkedList<Node> getSuccessors(Node u, boolean closed){
        //System.out.println("Entering getSuccessors()");
        LinkedList<Node> pred = u.getPredecessors();
        LinkedList<Node> succ = new LinkedList<Node>();
        int x = u.getX();
        int y = u.getY();
        if (x-1 >= 0) {                 // left
            if (grid[x-1][y] == 0){
                Node n = nodeGrid[x-1][y];
                if (n == null){
                    n = new Node(x-1, y);
                    n.setH( calculateHeur(n) );
                    n.setKey( calculateKey(n) );
                    nodeGrid[x-1][y] = n;
                }
                if ( !pred.contains(n) ){
                    succ.add(n);
                    if (closed) {
                        n.addPredecessor(u);
                    } else {
                        n.removePredecessor(u);
                    }
                }
            }
        }
        if (x+1 < nodeGrid.length) {    // right
            if (grid[x+1][y] == 0){
                Node n = nodeGrid[x+1][y];
                if (n == null){
                    n = new Node(x+1, y);
                    n.setH( calculateHeur(n) );
                    n.setKey( calculateKey(n) );
                    nodeGrid[x+1][y] = n;
                }
                if ( !pred.contains(n) ){
                    succ.add(n);
                    if (closed) {
                        n.addPredecessor(u);
                    } else {
                        n.removePredecessor(u);
                    }
                }
            }
        }
        if (y-1 >= 0) {                 // up
            if (grid[x][y-1] == 0){
                Node n = nodeGrid[x][y-1];
                if (n == null){
                    n = new Node(x, y-1);
                    n.setH( calculateHeur(n) );
                    n.setKey( calculateKey(n) );
                    nodeGrid[x][y-1] = n;
                }
                if ( !pred.contains(n) ){
                    succ.add(n);
                    if (closed) {
                        n.addPredecessor(u);
                    } else {
                        n.removePredecessor(u);
                    }
                }
            }
        }
        if (y+1 < nodeGrid[0].length) { // down
            if (grid[x][y+1] == 0){
                Node n = nodeGrid[x][y+1];
                if (n == null){
                    n = new Node(x, y+1);
                    n.setH( calculateHeur(n) );
                    n.setKey( calculateKey(n) );
                    nodeGrid[x][y+1] = n;
                }
                if ( !pred.contains(n) ){
                    succ.add(n);
                    if (closed) {
                        n.addPredecessor(u);
                    } else {
                        n.removePredecessor(u);
                    }
                }
            }
        }

        // printout
        //System.out.println("Successors:");
        //ListIterator<Node> iter = succ.listIterator();
        //while ( iter.hasNext() ){
        //    System.out.println("    "+iter.next().toString());
        //}
        
        return succ;  
    }
    
    /* Performs the search. Iterates through the priority queue, closing or
     * clearing nodes until a goal is reached. */
    private void computeShortestPath(){
        //System.out.println("Entering computeShortestPath()");
        if ( !U.isEmpty() ){
            Key topKey = U.first().getKey();
            Key goalKey = calculateKey(goal);
            while ( Key.compareKeys( topKey, goalKey ) < 0 ||      // We're not evaluating goal
                    goal.getRhs() != goal.getG() ){                // Goal isn't complete

                //System.out.println("loop");

                // Store the top key
                Key oldKey = topKey;
                
                // Pop the first inconsistent vertex
                System.out.println("Grabbing first node");
                Node u = U.pollFirst();
                System.out.println("    u: " + u.toString());
                
                // Calculate if g > rhs
                boolean greater = false;   
                int g = u.getG();
                int rhs = u.getRhs();
                if (g == INF){
                    if (rhs == INF){
                        greater = false;
                    } else {
                        greater = true;
                    }
                } else if (rhs == INF){
                    greater = false;
                } else {
                    greater = ( g > rhs );
                }

                // Calculate if the old key is smaller than new one
                Key newKey = calculateKey(u);
                u.setKey(newKey);
                int val = Key.compareKeys(oldKey, newKey);
                // Put the node back
                if (val < -1){      //-1 means same values but smaller ID, -2 is smaller values
                    //System.out.println("Old key was better.");
                    U.add(u);
                } else 
                // This node is becoming closed
                if ( greater ){
                    //System.out.println("G was higher.");
                    u.setG( u.getRhs() );
                    //System.out.println("    Node is now: "+u.toString());

                    // Find and update all successors (add this node as predecessor)
                    LinkedList<Node> succ = getSuccessors(u, true);
                    //System.out.println("Updating successors");
                    ListIterator<Node> iter = succ.listIterator();
                    while ( iter.hasNext() ){
                        Node s = iter.next();
                        updateVertex(s);
                    }
                }
                // This node is becoming open
                else {
                    //System.out.println("G was NOT higher.");
                    u.setG( INF );
                    //System.out.println("    Node is now: "+u.toString());
                    
                    // Find and update all successors+ourselves (remove this node as predecessor)
                    LinkedList<Node> succ = getSuccessors(u, false);
                    succ.add(u);
                    //System.out.println("Updating successors + self");
                    ListIterator<Node> iter = succ.listIterator();
                    while ( iter.hasNext() ){
                        Node s = iter.next();
                        updateVertex(s);
                    }
                }

                // printout
                //System.out.println("U:");
                //Iterator<Node> iter = U.iterator();
                //while ( iter.hasNext() ){
                //    System.out.println("    "+iter.next().toString());
                //}

                
                // Wait for user key press
                //Scanner scan = new Scanner(System.in);
                //String bleh = scan.next();
				
                // Get keys for next iteration, or break early
                if ( U.isEmpty() ){
                    break;
                }

                
                topKey = U.first().getKey();
                goalKey = calculateKey(goal);
            }
        }

        // Finished pathfinding
        if (goal.getRhs() != goal.getG()){
            System.out.println("No path exists.");
            return;
        }
        //System.out.println("Path was successful:");

        // Print the grid
        //for (int cols = 0; cols < grid.length; cols++){
        //    for (int rows = 0; rows < grid.length; rows++){
                //System.out.print(grid[cols][rows] + " ");
        //    }
            //System.out.println("");
        //}
        
        // Print the path
        /*Node prev = goal;
        System.out.println("    "+prev.toString());
        while (prev != start){
            LinkedList<Node> pred = prev.getPredecessors();
            ListIterator<Node> iter = pred.listIterator();
            int min = INF;
            Node next = null;
            while( iter.hasNext() ){
                Node n = iter.next();
                int g = n.getG();
                int c = calculateDist(prev, n);
                if (g != INF && c != INF){
                    int val = g + c;
                    if (val < min || min == INF){
                        min = val;
                        next = n;
                    }
                }
            }
            if (next == null) System.out.println("OH NOES");
            prev = next;
            System.out.println("    "+prev.toString());
        }*/
            
    }
    
    /* Internal node class to represent each vertex. */
    private static class Node implements NavigatorNode, Comparable<Node>{
        private int x;      // col number
        private int y;      // row number
        private int g;      // dist from start to this node (-1=inf)
        private int rhs;    // one-step-ahead dist from start to this node (-1=inf)
        private int h;      // estimated dist from this node to goal
        private Key key;    // sorting values
        private LinkedList<Node> pred;  // list of predecessors

        /* Constructor. */
        public Node(int x, int y){
            this.x = x;
            this.y = y;
            g = INF;             //inf
            rhs = INF;           //inf
            h = 0;
            pred = new LinkedList<Node>();
            key = new Key(INF, INF);
        }
        
        /* Add a predecessor if it isn't already. */
        public void addPredecessor(Node n){
            if ( !pred.contains(n) ){
                pred.add(n);
            }
        }

        /* Remove a predecessor if it exists. */
        public void removePredecessor(Node n){
            pred.remove(n);
        }
        
        /* Printout Override. */
        @Override
        public String toString(){
            return "("+x+","+y+")"+" rhs:"+rhs+" g:"+g+", h:"+h+" key:"+key.toString();
        }

        /* Comparable overrides. */
        
        @Override
        public int compareTo(Node b){
            Key k1 = this.getKey();
            Key k2 = b.getKey();
            int val = Key.compareKeys(k1, k2);
            return val;
        }
        
        @Override
        public boolean equals(Object obj){
            Node n = (Node)obj;
            Key a = this.key;
            Key b = n.getKey();
            int result = Key.compareKeys(a, b);
            if (result == 0){
                return true;
            }
            return false;
        }
        
        /* Setters. */

        public void setG(int g){
            this.g = g;
        }
        public void setRhs(int rhs){
            this.rhs = rhs;
        }
        public void setH(int h){
            this.h = h;
        }
        public void setKey(Key key){
            this.key = key;
        }
        

        /* Getters. */
		@Override
        public int getX(){
            return x;
        }
		@Override
        public int getY(){
            return y;
        }
        public int getG(){
            return g;
        }
        public int getRhs(){
            return rhs;
        }
        public int getH(){
            return h;
        }
        public Key getKey(){
            return key;
        }
        public LinkedList<Node> getPredecessors(){
            return pred;
        }

       

    }

    /* Internal key class to identify node values. */
    private static class Key {
        private static int idCounter = 0;      //increments every time a new key is made
        
        private int k1;     // Min(g, rhs) + h
        private int k2;     // Min(g, rhs)
        private int k3;     // Unique identifier
        
        /* Constructor. */
        public Key(int k1, int k2){
            this.k1 = k1;
            this.k2 = k2;
            this.k3 = idCounter++;
        }

         /* Compares two keys.
          * -2: a goes first
          * -1: a goes first, but values are the same (except for ID)
          * 0: same key
          * 1: b goes first, but values are the same (except for ID)
          * 2: b goes first */
        public static int compareKeys(Key a, Key b){
            int ak1 = a.getK1();
            int bk1 = b.getK1();
            int ak2 = a.getK2();
            int bk2 = b.getK2();
            int ak3 = a.getK3();
            int bk3 = b.getK3();
            
            // Special infinity cases
            if (ak1 == INF && bk1 == INF){
                if (ak2 == INF && bk2 == INF){
                    if (ak3 < bk3){
                        return -1;
                    }
                    if (ak3 > bk3){
                        return 1;
                    }
                    return 0;
                } 
                if (ak2 == INF){
                    return 2;
                }
                if (bk2 == INF){
                    return -2;
                }
            }
            if (ak1 == INF){
                return 2;
            }
            if (bk1 == INF){
                return -2;
            }

            // Normal cases
            if (ak1 < bk1){
                return -2;
            }
            if (ak1 > bk1){
                return 2;
            }
            if (ak2 < bk2){
                return -2;
            }
            if (ak2 > bk2){
                return 2;
            }
            if (ak3 < bk3){
                return -1;
            }
            if (ak3 > bk3){
                return 1;
            }
            return 0;
        }
        
        /* Getters. */

        public int getK1(){
            return k1;
        }
        public int getK2(){
            return k2;
        }
        public int getK3(){
            return k3;
        }

        /* Printout. */
        public String toString(){
            return "("+k1+","+k2+","+k3+")";
        }
    }
    
    /* Main. */
    /*public static void main(String[] args){
        System.out.println("Hello");
        int[][] grid = new int[3][3];
        grid[0][0] = 0;
        grid[1][0] = 0;
        grid[2][0] = 0;
        grid[0][1] = 0;
        grid[1][1] = 0;
        grid[2][1] = 0;
        grid[0][2] = 0;
        grid[1][2] = 0;
        grid[2][2] = 0;
        DStarLite nav = new DStarLite();
        nav.calculatePath(grid, 1, 0, 1, 2);
        // Wait for user key press
        Scanner scan = new Scanner(System.in);
        String bleh = scan.next();
        grid[1][1] = 1;
        nav.calculatePath(grid, 1, 0, 1, 2);
        // Wait for user key press
        bleh = scan.next();
        grid[0][0] = 1;
        nav.calculatePath(grid, 1, 0, 1, 2);
        // Wait for user key press
        bleh = scan.next();
        grid[1][1] = 0;
        nav.calculatePath(grid, 1, 0, 1, 2);
        // Wait for user key press
        bleh = scan.next();
        nav.calculatePath(grid, 2, 0, 1, 2);
        // Wait for user key press
        bleh = scan.next();
        grid[1][1] = 1;
        nav.calculatePath(grid, 1, 0, 1, 2);
        
    }*/
}
