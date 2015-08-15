package simulator.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import net.miginfocom.swing.MigLayout;
import java.util.LinkedList;
import java.util.ListIterator;
import java.lang.NumberFormatException;
import java.util.Arrays;

import simulator.*;

/** Displays a GUI to be used to create, edit, and delete vehicle and
 * environment profiles. Handles all button selection. Uses MIG Layout. */
public class UserInterface implements ActionListener, ListSelectionListener{

    /* Display variables. */
    // All states
    private JFrame mainFrame;   // Contains backPanel
    private JPanel backPanel;   // Contains all other panels
    private MigLayout backLayout;  
    private JPanel vehPanel;    // Vehicle profile selection panel
    private JLabel vehLabel;     
    private JButton vehNewButton;
    private JButton vehEditButton;
    private JButton vehDeleteButton;
    private JList<String> vehList;  // List of vehicle profiles
    private JPopupMenu vehMenu;
    private JLabel envLabel;
    private JButton envNewButton;
    private JButton envEditButton;
    private JButton envDeleteButton;
    private JList<String> envList;
    private JPopupMenu envMenu;
    private JPanel envPanel;    // Environment profile selection panel
    private JPanel dispPanel;   // Extra display panel
    private JButton simButton;
    
    // Displaying Vehicle Profile
    private JTextField vNameTField; // Name of the vehicle profile
    private JTextField vWidthTField; 
    private JTextField vHeightTField;
    private JTextField vLinVelTField;
    private JTextField vRotVelTField; 
    private JTextField vLidRangeTField;
    private JTextField vLidIncTField;
    private JTextField vLidDistTField;
    private JTextField vLidPeriodTField;
    private JTextField vCamRangeTField;
    private JTextField vCamIncTField;
    private JTextField vCamDistTField;
    private JTextField vCamPeriodTField;
    private JTextField vGpsErrorTField;
    private JTextField vGpsPeriodTField;
    private JTextField vImuErrorTField;
    private JTextField vImuPeriodTField;
    private JComboBox<String> vNavMethodCBox;
    
    // Displaying Environment Profile
    private JTextField eNameTField; // Name of the vehicle profile
    private EnvironmentMaker eEnvMaker;
    
    /* State variables. */
    private int vehSelectedIndex;
    private int envSelectedIndex;
    private VehicleProfile vehSelectedProfile;
    private EnvironmentProfile envSelectedProfile;
    
    /* Create the GUI. */
    public UserInterface(){
        // Set non-display variables
        vehSelectedIndex = -1;
        envSelectedIndex = -1;
        vehSelectedProfile = null;

        // Initialise the GUI
        initialiseGUI();

        // Show the GUI
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(0);
            }
        });
    }

    /* Sets GUI variables. Only called once. All JPanel's that do not change
     * will be created here. */
    private void initialiseGUI(){
        // Create the main frame
        mainFrame = new JFrame("Autonomous Vehicle Simulator");
        mainFrame.setSize(950,800);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                
        // Create the background
        backLayout = new MigLayout(
                "fill",                         // Layout constraints
                "[fill, 240!][fill]",           // Column constraints
                "[sg, fill][sg, fill][30!]");   // Row constraints
        backPanel = new JPanel(backLayout);
        backPanel.setBackground(Color.GRAY);

        // Create the vehicle profile panel
        MigLayout vehLayout = new MigLayout(
                "fill",
                "[fill, sg][fill, sg][fill]",
                "[20!][40!][]");
        vehPanel = new JPanel(vehLayout);
        vehPanel.setBackground(Color.WHITE);
        
        // Create the stuff inside the vehicle profile panel
        vehLabel = new JLabel("Vehicle Profiles", SwingConstants.CENTER);
        vehLabel.setOpaque(true);
        vehLabel.setBackground(Color.LIGHT_GRAY);
        vehLabel.setForeground(Color.BLACK);
        vehNewButton = new JButton("New");
        vehNewButton.addActionListener(this);
        vehNewButton.setActionCommand("vehNewButton");
        vehEditButton = new JButton("Edit");
        vehEditButton.addActionListener(this);
        vehEditButton.setActionCommand("vehEditButton");
        vehEditButton.setEnabled(false);
        vehDeleteButton = new JButton("Delete");
        vehDeleteButton.addActionListener(this);
        vehDeleteButton.setActionCommand("vehDeleteButton");
        vehDeleteButton.setEnabled(false);
        DefaultListModel<String> model = new DefaultListModel<String>();
        LinkedList<String> names = VehicleProfile.getNames();
        ListIterator<String> iter = names.listIterator();
        while ( iter.hasNext() ){
            String name = iter.next();
            model.addElement(name);
        }
        vehList = new JList<String>(model);
        // popup
        vehMenu = new JPopupMenu();
        JMenuItem vehCopy = new JMenuItem("Copy");
        vehCopy.addActionListener(this);
        vehCopy.setActionCommand("vehCopy");
        vehMenu.add(vehCopy);
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
            private void maybeShowPopup(MouseEvent e){
                if (e.isPopupTrigger()) {
                    vehList.setSelectedIndex(vehList.locationToIndex(e.getPoint())); //select the item
                    vehMenu.show(e.getComponent(), e.getX(), e.getY()); //and show the menu
                }
            }};
        vehList.addMouseListener( ml );
        vehList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        vehList.addListSelectionListener(this);
        vehList.setName("vehList");

        // Create the environment profile panel
        MigLayout envLayout = new MigLayout(
                "fill",
                "[fill, sg][fill, sg][fill]",
                "[20!][40!][]");
        envPanel = new JPanel(envLayout);
        envPanel.setBackground(Color.WHITE);

        // Create the stuff inside the environment profile panel
        envLabel = new JLabel("Environment Profiles", SwingConstants.CENTER);
        envLabel.setOpaque(true);
        envLabel.setBackground(Color.LIGHT_GRAY);
        envLabel.setForeground(Color.BLACK);
        envNewButton = new JButton("New");
        envNewButton.addActionListener(this);
        envNewButton.setActionCommand("envNewButton");
        envEditButton = new JButton("Edit");
        envEditButton.addActionListener(this);
        envEditButton.setActionCommand("envEditButton");
        envEditButton.setEnabled(false);
        envDeleteButton = new JButton("Delete");
        envDeleteButton.addActionListener(this);
        envDeleteButton.setActionCommand("envDeleteButton");
        envDeleteButton.setEnabled(false);
        DefaultListModel<String> envModel = new DefaultListModel<String>();
        LinkedList<String> envNames = EnvironmentProfile.getNames();
        ListIterator<String> envIter = envNames.listIterator();
        while ( envIter.hasNext() ){
            String name = envIter.next();
            envModel.addElement(name);
        }
        envList = new JList<String>(envModel);
        // popup
        envMenu = new JPopupMenu();
        JMenuItem envCopy = new JMenuItem("Copy");
        envCopy.addActionListener(this);
        envCopy.setActionCommand("envCopy");
        envMenu.add(envCopy);
        MouseListener ml2 = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
            private void maybeShowPopup(MouseEvent e){
                if (e.isPopupTrigger()) {
                    envList.setSelectedIndex(envList.locationToIndex(e.getPoint())); //select the item
                    envMenu.show(e.getComponent(), e.getX(), e.getY()); //and show the menu
                }
            }};
        envList.addMouseListener( ml2 );
        envList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        envList.addListSelectionListener(this);
        envList.setName("envList");

        // Create the extra display panel
        dispPanel = new JPanel();
        dispPanel.setBackground(Color.WHITE);

        // Create the simulate button
        simButton = new JButton("SIMULATE");
        simButton.addActionListener(this);
        simButton.setActionCommand("simButton");
        if (   !( (DefaultListModel<String>)vehList.getModel() ).isEmpty()
            && !( (DefaultListModel<String>)envList.getModel() ).isEmpty()){
            simButton.setEnabled(true);
        } else {
            simButton.setEnabled(false);
        }
        
        // Pack JPanels that will not change
        vehPanel.add(new JScrollPane(vehList), "cell 0 2, span 3, grow");
        vehPanel.add(vehLabel, "cell 0 0, span 3, grow");
        vehPanel.add(vehNewButton, "cell 0 1");
        vehPanel.add(vehEditButton, "cell 1 1");
        vehPanel.add(vehDeleteButton, "cell 2 1");
        envPanel.add(new JScrollPane(envList), "cell 0 2, span 3, grow");
        envPanel.add(envLabel, "cell 0 0, span 3, grow");
        envPanel.add(envNewButton, "cell 0 1");
        envPanel.add(envEditButton, "cell 1 1");
        envPanel.add(envDeleteButton, "cell 2 1");
    }

    /* Displays the GUI. Called initially, and whenever a JPanel needs to be
     * changed.
     * 0: dispPanel is blank.
     * 1: dispPanel shows a vehicle profile (uneditable).
     * 2: dispPanel shows a vehicle profile (editable).
     * 3: dispPanel shows an environment profile (uneditable).
     * 4: dispPanel shows an environment profile (editable).*/
    private void createAndShowGUI(int displayMode){
        // Clear the previous display
        mainFrame.getContentPane().removeAll();
                
        // Create the background
        backPanel = new JPanel(backLayout);
        backPanel.setBackground(Color.GRAY);
        
        // Create the extra display panel
        dispPanel = new JPanel();
        dispPanel.setBackground(Color.WHITE);

        // Vehicle Profile display
        if (displayMode == 1 || displayMode == 2){        
            boolean editable = false;
            if (displayMode == 2) editable = true;
            
            MigLayout layout = new MigLayout(
                "fill",
                "[140!][80!][100:120:150][80!][5!]", // Columns
                "[20!]"                              // Banner
                +"[30!][][][][][][][][][][][][][]"   // Stats
                +"[][40!]");                         // Save/Cancel buttons
            dispPanel.setLayout(layout);
            JLabel dispLabel = new JLabel( "Vehicle Profile", SwingConstants.CENTER);
            dispLabel.setOpaque(true);
            dispLabel.setBackground(Color.LIGHT_GRAY);
            JLabel nameLabel = new JLabel("Name: ");
            vNameTField = new JTextField(vehSelectedProfile.getName() );
            vNameTField.setEditable(editable);
            vNameTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vWidthTField.requestFocusInWindow();
                    vWidthTField.selectAll();
                }});
            Vehicle v = vehSelectedProfile.getVehicle();
            Color headingColor = new Color(230,230,230);
            
            // Vehicle stats
            JLabel vNameLabel = new JLabel("Vehicle Stats", SwingConstants.CENTER);
            vNameLabel.setBackground(headingColor);
            vNameLabel.setOpaque(true);
            JLabel vWidthLabel = new JLabel("Width (m):");
            vWidthTField = new JTextField(String.valueOf( v.getWidth() ));
            vWidthTField.setEditable(editable);
            vWidthTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vHeightTField.requestFocusInWindow();
                    vHeightTField.selectAll();
                }});
            JLabel vHeightLabel = new JLabel("Height (m):");
            vHeightTField = new JTextField(String.valueOf( v.getHeight() ));
            vHeightTField.setEditable(editable);
            vHeightTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vLinVelTField.requestFocusInWindow();
                    vLinVelTField.selectAll();
                }});
            JLabel vLinVelLabel = new JLabel("Linear Velocity (m/s):");
            vLinVelTField = new JTextField(String.valueOf( v.getLinearVelocity() ));
            vLinVelTField.setEditable(editable);
            vLinVelTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vRotVelTField.requestFocusInWindow();
                    vRotVelTField.selectAll();
                }});
            JLabel vRotVelLabel = new JLabel("Rotational Velocity (deg/s):");
            vRotVelTField = new JTextField(String.valueOf( v.getRotationalVelocity() ));
            vRotVelTField.setEditable(editable);
            vRotVelTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vLidRangeTField.requestFocusInWindow();
                    vLidRangeTField.selectAll();
                }});

            // Lidar stats
            JLabel vLidLabel = new JLabel("LIDAR Stats", SwingConstants.CENTER);
            vLidLabel.setBackground(headingColor);
            vLidLabel.setOpaque(true);
            JLabel vLidRangeLabel = new JLabel("Range (deg):");
            vLidRangeTField = new JTextField( String.valueOf (v.getLidarRange() ));
            vLidRangeTField.setEditable(editable);
            vLidRangeTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vLidIncTField.requestFocusInWindow();
                    vLidIncTField.selectAll();
                }});
            JLabel vLidIncLabel = new JLabel("Increment (deg):");
            vLidIncTField = new JTextField( String.valueOf (v.getLidarIncrement() ));
            vLidIncTField.setEditable(editable);
            vLidIncTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vLidDistTField.requestFocusInWindow();
                    vLidDistTField.selectAll();
                }});
            JLabel vLidDistLabel = new JLabel("Distance (m):");
            vLidDistTField = new JTextField( String.valueOf (v.getLidarDistance() ));
            vLidDistTField.setEditable(editable);
            vLidDistTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vLidPeriodTField.requestFocusInWindow();
                    vLidPeriodTField.selectAll();
                }});
            JLabel vLidPeriodLabel = new JLabel("Period (s):");
            vLidPeriodTField = new JTextField( String.valueOf (v.getLidarPeriod() ));
            vLidPeriodTField.setEditable(editable);
            vLidPeriodTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vCamRangeTField.requestFocusInWindow();
                    vCamRangeTField.selectAll();
                }});

            // Camera stats
            JLabel vCamLabel = new JLabel("Camera Stats", SwingConstants.CENTER);
            vCamLabel.setBackground(headingColor);
            vCamLabel.setOpaque(true);
            JLabel vCamRangeLabel = new JLabel("Range (deg):");
            vCamRangeTField = new JTextField( String.valueOf (v.getCameraRange() ));
            vCamRangeTField.setEditable(editable);
            vCamRangeTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vCamIncTField.requestFocusInWindow();
                    vCamIncTField.selectAll();
                }});
            JLabel vCamIncLabel = new JLabel("Increment (deg):");
            vCamIncTField = new JTextField( String.valueOf (v.getCameraIncrement() ));
            vCamIncTField.setEditable(editable);
            vCamIncTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vCamDistTField.requestFocusInWindow();
                    vCamDistTField.selectAll();
                }});
            JLabel vCamDistLabel = new JLabel("Distance (m):");
            vCamDistTField = new JTextField( String.valueOf (v.getCameraDistance() ));
            vCamDistTField.setEditable(editable);
            vCamDistTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vCamPeriodTField.requestFocusInWindow();
                    vCamPeriodTField.selectAll();
                }});
            JLabel vCamPeriodLabel = new JLabel("Period (s):");
            vCamPeriodTField = new JTextField( String.valueOf (v.getCameraPeriod() ));
            vCamPeriodTField.setEditable(editable);
            vCamPeriodTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vGpsErrorTField.requestFocusInWindow();
                    vGpsErrorTField.selectAll();
                }});

            // GPS stats
            JLabel vGpsLabel = new JLabel("GPS Stats", SwingConstants.CENTER);
            vGpsLabel.setBackground(headingColor);
            vGpsLabel.setOpaque(true);
            JLabel vGpsErrorLabel = new JLabel("Error (m, 3 stand dev):");
            vGpsErrorTField = new JTextField( String.valueOf (v.getGpsError() ));
            vGpsErrorTField.setEditable(editable);
            vGpsErrorTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vGpsPeriodTField.requestFocusInWindow();
                    vGpsPeriodTField.selectAll();
                }});
            JLabel vGpsPeriodLabel = new JLabel("Update Period (s):");
            vGpsPeriodTField = new JTextField( String.valueOf (v.getGpsUpdatePeriod() ));
            vGpsPeriodTField.setEditable(editable);
            vGpsPeriodTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vImuErrorTField.requestFocusInWindow();
                    vImuErrorTField.selectAll();
                }});

             // IMU stats
            JLabel vImuLabel = new JLabel("IMU Stats", SwingConstants.CENTER);
            vImuLabel.setBackground(headingColor);
            vImuLabel.setOpaque(true);
            JLabel vImuErrorLabel = new JLabel("Error (deg, 3 stand dev):");
            vImuErrorTField = new JTextField( String.valueOf (v.getImuError() ));
            vImuErrorTField.setEditable(editable);
            vImuErrorTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vImuPeriodTField.requestFocusInWindow();
                    vImuPeriodTField.selectAll();
                }});
            JLabel vImuPeriodLabel = new JLabel("Update Period (s):");
            vImuPeriodTField = new JTextField( String.valueOf (v.getImuUpdatePeriod() ));
            vImuPeriodTField.setEditable(editable);
            vImuPeriodTField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    vNavMethodCBox.requestFocusInWindow();
                }});

            // Navigation method
            JLabel vNavLabel = new JLabel("Navigation", SwingConstants.CENTER);
            vNavLabel.setBackground(headingColor);
            vNavLabel.setOpaque(true);
            JLabel vNavMethodLabel = new JLabel("Method:");
            vNavMethodCBox = new JComboBox<String>( Navigators.getNames() );
            vNavMethodCBox.setSelectedIndex( Navigators.indexOf( Navigators.getNavigator(v.getNavigatorName()) ));
            vNavMethodCBox.setEnabled(editable);
            
            // Save/Cancel buttons
            JButton vSaveButton = new JButton("Save");
            vSaveButton.addActionListener(this);
            vSaveButton.setActionCommand("vSaveButton");
            vSaveButton.setEnabled(editable);
            JButton vCancelButton = new JButton("Cancel");
            vCancelButton.addActionListener(this);
            vCancelButton.setActionCommand("vCancelButton");
            vCancelButton.setEnabled(editable);

            // Display
            dispPanel.add(dispLabel, "cell 0 0, span 5, grow");
            dispPanel.add(nameLabel, "cell 0 1");
            dispPanel.add(vNameTField, "cell 1 1, span 3, grow");
            dispPanel.add(vNameLabel, "cell 0 2, span 2, grow");
            dispPanel.add(vWidthLabel, "cell 0 3");
            dispPanel.add(vWidthTField, "cell 1 3, growx");
            dispPanel.add(vHeightLabel, "cell 0 4");
            dispPanel.add(vHeightTField, "cell 1 4, growx");
            dispPanel.add(vLinVelLabel, "cell 0 5");
            dispPanel.add(vLinVelTField, "cell 1 5, growx");
            dispPanel.add(vRotVelLabel, "cell 0 6");
            dispPanel.add(vRotVelTField, "cell 1 6, growx");
            
            dispPanel.add(vLidLabel, "cell 0 7, span 2, grow");
            dispPanel.add(vLidRangeLabel, "cell 0 8");
            dispPanel.add(vLidRangeTField, "cell 1 8, growx");
            dispPanel.add(vLidIncLabel, "cell 0 9");
            dispPanel.add(vLidIncTField, "cell 1 9, growx");
            dispPanel.add(vLidDistLabel, "cell 0 10");
            dispPanel.add(vLidDistTField, "cell 1 10, growx");
            dispPanel.add(vLidPeriodLabel, "cell 0 11");
            dispPanel.add(vLidPeriodTField, "cell 1 11, growx");

            dispPanel.add(vCamLabel, "cell 2 2, span 2, grow");
            dispPanel.add(vCamRangeLabel, "cell 2 3");
            dispPanel.add(vCamRangeTField, "cell 3 3, growx");
            dispPanel.add(vCamIncLabel, "cell 2 4");
            dispPanel.add(vCamIncTField, "cell 3 4, growx");
            dispPanel.add(vCamDistLabel, "cell 2 5");
            dispPanel.add(vCamDistTField, "cell 3 5, growx");
            dispPanel.add(vCamPeriodLabel, "cell 2 6");
            dispPanel.add(vCamPeriodTField, "cell 3 6, growx");

            dispPanel.add(vGpsLabel, "cell 2 7, span 2, grow");
            dispPanel.add(vGpsErrorLabel, "cell 2 8");
            dispPanel.add(vGpsErrorTField, "cell 3 8, growx");
            dispPanel.add(vGpsPeriodLabel, "cell 2 9");
            dispPanel.add(vGpsPeriodTField, "cell 3 9, growx");

            dispPanel.add(vImuLabel, "cell 2 10, span 2, grow");
            dispPanel.add(vImuErrorLabel, "cell 2 11");
            dispPanel.add(vImuErrorTField, "cell 3 11, growx");
            dispPanel.add(vImuPeriodLabel, "cell 2 12");
            dispPanel.add(vImuPeriodTField, "cell 3 12, growx");

            dispPanel.add(vNavLabel, "cell 2 13, span 2, grow");
            dispPanel.add(vNavMethodLabel, "cell 2 14");
            dispPanel.add(vNavMethodCBox, "cell 3 14, growx");
            
            dispPanel.add(vSaveButton, "cell 0 16, span 4, center");
            dispPanel.add(vCancelButton, "cell 0 16, span 4, center");
        }

        // Environment Profile display
        if (displayMode == 3 || displayMode == 4){ 
            boolean editable = false;
            if (displayMode == 4) editable = true;
            
            MigLayout layout = new MigLayout(
                "fill",
                "[][]",              // Columns
                "[20!]"              // Banner
                +"[30!][][30!]");    // Save/Cancel buttons
            dispPanel.setLayout(layout);
            JLabel dispLabel = new JLabel("Environment Profile", SwingConstants.CENTER);
            dispLabel.setOpaque(true);
            dispLabel.setBackground(Color.LIGHT_GRAY);
            JLabel nameLabel = new JLabel("Name: ");
            eNameTField = new JTextField(envSelectedProfile.getName() );
            eNameTField.setEditable(editable);
            Environment e = envSelectedProfile.getEnvironment();
            Color headingColor = new Color(230,230,230);

            // Environment maker
            eEnvMaker = new EnvironmentMaker(e);
            if (!editable) eEnvMaker.lock();
            
            // Save/Cancel buttons
            JButton vSaveButton = new JButton("Save");
            vSaveButton.addActionListener(this);
            vSaveButton.setActionCommand("eSaveButton");
            vSaveButton.setEnabled(editable);
            JButton vCancelButton = new JButton("Cancel");
            vCancelButton.addActionListener(this);
            vCancelButton.setActionCommand("eCancelButton");
            vCancelButton.setEnabled(editable);

            // Display
            dispPanel.add(dispLabel, "cell 0 0, span 5, grow");
            dispPanel.add(nameLabel, "cell 0 1");
            dispPanel.add(eNameTField, "cell 1 1, grow");
            dispPanel.add(eEnvMaker, "cell 0 2, span 2, grow");
            dispPanel.add(vSaveButton, "cell 0 4, span 4, center");
            dispPanel.add(vCancelButton, "cell 0 4, span 4, center");
        }
        
        // Pack and display the window
        backPanel.add(vehPanel, "cell 0 0");
        backPanel.add(envPanel, "cell 0 1");
        backPanel.add(dispPanel, "cell 1 0, span 1 2");
        backPanel.add(simButton, "cell 0 2, span 2, w 180!, center");
        mainFrame.add(backPanel);
        mainFrame.setVisible(true);
    }

    /* Displays a vehicle profile. Called when a new vehicle profile is
     * selected or a relevant button is clicked. */
    private void showVehicleProfile(String name, boolean toEdit){
        VehicleProfile vp = VehicleProfile.getVehicleProfile(name);
        vehSelectedProfile = vp;
        if ( vp==null ){
            createAndShowGUI(0);
            return;
        }
        if (toEdit){
            createAndShowGUI(2);
        } else {
            createAndShowGUI(1);
        }
    }

    /* Displays a environment profile. Called when a new environment profile is
     * selected or a relevant button is clicked. */
    private void showEnvironmentProfile(String name, boolean toEdit){
        EnvironmentProfile vp = EnvironmentProfile.getEnvironmentProfile(name);
        envSelectedProfile = vp;
        if ( vp==null ){
            createAndShowGUI(0);
            return;
        }
        if (toEdit){
            createAndShowGUI(4);
        } else {
            createAndShowGUI(3);
        }
    }
    
    /* When buttons are pressed, this is where it's handled. All events have a
     * command that matches the variable name of the source. */
    @Override
    public void actionPerformed( ActionEvent e){
        String event = e.getActionCommand();

        // New vehicle profile
        if ( event.equals("vehNewButton") ){
            // Try to create a uniquely named VehicleProfile
            VehicleProfile vp = null;
            int i = 0;
            while (vp == null){
                vp = VehicleProfile.createNewVehicleProfile("VehicleProfile"+i);
                i++;
            }

            // Add it to the vehicle list
            ( (DefaultListModel<String>)vehList.getModel() ).addElement( vp.getName() );
            vehList.setSelectedIndex(vehList.getModel().getSize()-1);

            // Set display
            showVehicleProfile(vp.getName(), true);
            vehNewButton.setEnabled(false);
            vehEditButton.setEnabled(false);
            vehDeleteButton.setEnabled(false);
            envNewButton.setEnabled(false);
            envEditButton.setEnabled(false);
            envDeleteButton.setEnabled(false);
            simButton.setEnabled(false);
        }

        // Edit vehicle profile
        if ( event.equals("vehEditButton") ){
            // Set display
            showVehicleProfile(vehSelectedProfile.getName(), true);
            vehNewButton.setEnabled(false);
            vehEditButton.setEnabled(false);
            vehDeleteButton.setEnabled(false);
            envNewButton.setEnabled(false);
            envEditButton.setEnabled(false);
            envDeleteButton.setEnabled(false);
            simButton.setEnabled(false);
        }

        // Delete vehicle profile
        if ( event.equals("vehDeleteButton") ){
            String name = vehSelectedProfile.getName();

            // Create confirmation dialog
            Object[] options = {"Delete", "Cancel"};
            int n = JOptionPane.showOptionDialog(mainFrame,
            "Are you sure you want to permanently delete \""+name
            +"\"?",
            "Delete Confirmation",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[1]);
            
            // Delete
            if (n == 0){
                ( (DefaultListModel<String>)vehList.getModel() ).removeElement(name);
                VehicleProfile.deleteVehicleProfile(name);
                
                // Save to file
                Logger.writeVehicleProfiles();
            }

            // Display
            if (   !( (DefaultListModel<String>)vehList.getModel() ).isEmpty()
                && !( (DefaultListModel<String>)envList.getModel() ).isEmpty()){
                simButton.setEnabled(true);
            } else {
                simButton.setEnabled(false);
            }
        }

        // Save a vehicle profile (temporarily attempt to change values, and if
        // all of them are valid, set them and finish editing.)
        if ( event.equals("vSaveButton") ){
            Color badColor = new Color(255, 30, 30);
            Vehicle vehicle = vehSelectedProfile.getVehicle();
            boolean valid = true;       // If any fields are invalid, this is false

            // Check and store all fields
            String newName = "";
            double newWidth = 0;
            double newHeight = 0;
            double newLinVel = 0;
            double newRotVel = 0;
            double newLidRange = 0;
            double newLidInc = 0;
            double newLidDist = 0;
            double newLidPeriod = 0;
            double newCamRange = 0;
            double newCamInc = 0;
            double newCamDist = 0;
            double newCamPeriod = 0;
            double newGpsError = 0;
            double newGpsPeriod = 0;
            double newImuError = 0;
            double newImuPeriod = 0;
            
            // Vehicle profile name
            newName = vNameTField.getText();
            String oldName = vehSelectedProfile.getName();
            if (vehSelectedProfile.setName(newName)){
                vehSelectedProfile.setName(oldName);
                vNameTField.setBackground(Color.WHITE);
                vNameTField.setForeground(Color.BLACK);
            } else {
                valid = false;
                vNameTField.setBackground(badColor);
                vNameTField.setForeground(Color.WHITE);
            } 
            // Vehicle Stats
            // width
            try {
                newWidth = Double.parseDouble( vWidthTField.getText() );
                double oldWidth = vehicle.getWidth();
                if ( vehicle.setWidth(newWidth) ){
                    vehicle.setWidth(oldWidth);
                    vWidthTField.setBackground(Color.WHITE);
                    vWidthTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vWidthTField.setBackground(badColor);
                    vWidthTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vWidthTField.setBackground(badColor);
                vWidthTField.setForeground(Color.WHITE);
            }
            // height
            try {
                newHeight = Double.parseDouble( vHeightTField.getText() );
                double oldHeight = vehicle.getHeight();
                if ( vehicle.setHeight(newHeight) ){
                    vehicle.setHeight(oldHeight);
                    vHeightTField.setBackground(Color.WHITE);
                    vHeightTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vHeightTField.setBackground(badColor);
                    vHeightTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vHeightTField.setBackground(badColor);
                vHeightTField.setForeground(Color.WHITE);
            }
            // linear velocity
            try {
                newLinVel = Double.parseDouble( vLinVelTField.getText() );
                double oldLinVel = vehicle.getLinearVelocity();
                if ( vehicle.setLinearVelocity(newLinVel) ){
                    vehicle.setLinearVelocity(oldLinVel);
                    vLinVelTField.setBackground(Color.WHITE);
                    vLinVelTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vLinVelTField.setBackground(badColor);
                    vLinVelTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vLinVelTField.setBackground(badColor);
                vLinVelTField.setForeground(Color.WHITE);
            }
            // rot velocity
            try {
                newRotVel = Double.parseDouble( vRotVelTField.getText() );
                double oldRotVel = vehicle.getRotationalVelocity();
                if ( vehicle.setRotationalVelocity(newRotVel) ){
                    vehicle.setRotationalVelocity(oldRotVel);
                    vRotVelTField.setBackground(Color.WHITE);
                    vRotVelTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vRotVelTField.setBackground(badColor);
                    vRotVelTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vRotVelTField.setBackground(badColor);
                vRotVelTField.setForeground(Color.WHITE);
            }
            // lidar range
            try {
                newLidRange = Double.parseDouble( vLidRangeTField.getText() );
                double oldLidRange = vehicle.getLidarRange();
                if ( vehicle.setLidarRange(newLidRange) ){
                    vehicle.setLidarRange(oldLidRange);
                    vLidRangeTField.setBackground(Color.WHITE);
                    vLidRangeTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vLidRangeTField.setBackground(badColor);
                    vLidRangeTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vLidRangeTField.setBackground(badColor);
                vLidRangeTField.setForeground(Color.WHITE);
            }
            // lidar increment
            try {
                newLidInc = Double.parseDouble( vLidIncTField.getText() );
                double oldLidInc = vehicle.getLidarIncrement();
                if ( vehicle.setLidarIncrement(newLidInc) ){
                    vehicle.setLidarIncrement(oldLidInc);
                    vLidIncTField.setBackground(Color.WHITE);
                    vLidIncTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vLidIncTField.setBackground(badColor);
                    vLidIncTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vLidIncTField.setBackground(badColor);
                vLidIncTField.setForeground(Color.WHITE);
            }
            // lidar distance
            try {
                newLidDist = Double.parseDouble( vLidDistTField.getText() );
                double oldLidDist = vehicle.getLidarDistance();
                if ( vehicle.setLidarDistance(newLidDist) ){
                    vehicle.setLidarDistance(oldLidDist);
                    vLidDistTField.setBackground(Color.WHITE);
                    vLidDistTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vLidDistTField.setBackground(badColor);
                    vLidDistTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vLidDistTField.setBackground(badColor);
                vLidDistTField.setForeground(Color.WHITE);
            }
            // lidar period
            try {
                newLidPeriod = Double.parseDouble( vLidPeriodTField.getText() );
                double oldLidPeriod = vehicle.getLidarPeriod();
                if ( vehicle.setLidarPeriod(newLidPeriod) ){
                    vehicle.setLidarPeriod(oldLidPeriod);
                    vLidPeriodTField.setBackground(Color.WHITE);
                    vLidPeriodTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vLidPeriodTField.setBackground(badColor);
                    vLidPeriodTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vLidPeriodTField.setBackground(badColor);
                vLidPeriodTField.setForeground(Color.WHITE);
            }
             // camera range
            try {
                newCamRange = Double.parseDouble( vCamRangeTField.getText() );
                double oldCamRange = vehicle.getCameraRange();
                if ( vehicle.setCameraRange(newCamRange) ){
                    vehicle.setCameraRange(oldCamRange);
                    vCamRangeTField.setBackground(Color.WHITE);
                    vCamRangeTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vCamRangeTField.setBackground(badColor);
                    vCamRangeTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vCamRangeTField.setBackground(badColor);
                vCamRangeTField.setForeground(Color.WHITE);
            }
            // camera increment
            try {
                newCamInc = Double.parseDouble( vCamIncTField.getText() );
                double oldCamInc = vehicle.getCameraIncrement();
                if ( vehicle.setCameraIncrement(newCamInc) ){
                    vehicle.setCameraIncrement(oldCamInc);
                    vCamIncTField.setBackground(Color.WHITE);
                    vCamIncTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vCamIncTField.setBackground(badColor);
                    vCamIncTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vCamIncTField.setBackground(badColor);
                vCamIncTField.setForeground(Color.WHITE);
            }
            // camera distance
            try {
                newCamDist = Double.parseDouble( vCamDistTField.getText() );
                double oldCamDist = vehicle.getCameraDistance();
                if ( vehicle.setCameraDistance(newCamDist) ){
                    vehicle.setCameraDistance(oldCamDist);
                    vCamDistTField.setBackground(Color.WHITE);
                    vCamDistTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vCamDistTField.setBackground(badColor);
                    vCamDistTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vCamDistTField.setBackground(badColor);
                vCamDistTField.setForeground(Color.WHITE);
            }
            // camera period
            try {
                newCamPeriod = Double.parseDouble( vCamPeriodTField.getText() );
                double oldCamPeriod = vehicle.getCameraPeriod();
                if ( vehicle.setCameraPeriod(newCamPeriod) ){
                    vehicle.setCameraPeriod(oldCamPeriod);
                    vCamPeriodTField.setBackground(Color.WHITE);
                    vCamPeriodTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vCamPeriodTField.setBackground(badColor);
                    vCamPeriodTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vCamPeriodTField.setBackground(badColor);
                vCamPeriodTField.setForeground(Color.WHITE);
            }
            // gps error
            try {
                newGpsError = Double.parseDouble( vGpsErrorTField.getText() );
                double oldGpsError = vehicle.getGpsError();
                if ( vehicle.setGpsError(newGpsError) ){
                    vehicle.setGpsError(oldGpsError);
                    vGpsErrorTField.setBackground(Color.WHITE);
                    vGpsErrorTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vGpsErrorTField.setBackground(badColor);
                    vGpsErrorTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vGpsErrorTField.setBackground(badColor);
                vGpsErrorTField.setForeground(Color.WHITE);
            }
            // gps update period
            try {
                newGpsPeriod = Double.parseDouble( vGpsPeriodTField.getText() );
                double oldGpsPeriod = vehicle.getGpsUpdatePeriod();
                if ( vehicle.setGpsUpdatePeriod(newGpsPeriod) ){
                    vehicle.setGpsUpdatePeriod(oldGpsPeriod);
                    vGpsPeriodTField.setBackground(Color.WHITE);
                    vGpsPeriodTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vGpsPeriodTField.setBackground(badColor);
                    vGpsPeriodTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vGpsPeriodTField.setBackground(badColor);
                vGpsPeriodTField.setForeground(Color.WHITE);
            }
            // imu error
            try {
                newImuError = Double.parseDouble( vImuErrorTField.getText() );
                double oldImuError = vehicle.getImuError();
                if ( vehicle.setImuError(newImuError) ){
                    vehicle.setImuError(oldImuError);
                    vImuErrorTField.setBackground(Color.WHITE);
                    vImuErrorTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vImuErrorTField.setBackground(badColor);
                    vImuErrorTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vImuErrorTField.setBackground(badColor);
                vImuErrorTField.setForeground(Color.WHITE);
            }
            // imu update period
            try {
                newImuPeriod = Double.parseDouble( vImuPeriodTField.getText() );
                double oldImuPeriod = vehicle.getImuUpdatePeriod();
                if ( vehicle.setImuUpdatePeriod(newImuPeriod) ){
                    vehicle.setImuUpdatePeriod(oldImuPeriod);
                    vImuPeriodTField.setBackground(Color.WHITE);
                    vImuPeriodTField.setForeground(Color.BLACK);
                } else {
                    valid = false;
                    vImuPeriodTField.setBackground(badColor);
                    vImuPeriodTField.setForeground(Color.WHITE);
                }
            } catch (NumberFormatException ex){
                valid = false;
                vImuPeriodTField.setBackground(badColor);
                vImuPeriodTField.setForeground(Color.WHITE);
            }

            // Save, if all fields are valid
            if (valid){
                // Update the profile
                vehSelectedProfile.setName(newName);
                int index = ( (DefaultListModel<String>)vehList.getModel() ).indexOf(oldName);
                ( (DefaultListModel<String>)vehList.getModel() ).set(index, newName);
                vehicle.setWidth(newWidth);
                vehicle.setHeight(newHeight);
                vehicle.setLinearVelocity(newLinVel);
                vehicle.setRotationalVelocity(newRotVel);
                vehicle.setLidarRange(newLidRange);
                vehicle.setLidarIncrement(newLidInc);
                vehicle.setLidarDistance(newLidDist);
                vehicle.setLidarPeriod(newLidPeriod);
                vehicle.setCameraRange(newCamRange);
                vehicle.setCameraIncrement(newCamInc);
                vehicle.setCameraDistance(newCamDist);
                vehicle.setCameraPeriod(newCamPeriod);
                vehicle.setGpsError(newGpsError);
                vehicle.setGpsUpdatePeriod(newGpsPeriod);
                vehicle.setImuError(newImuError);
                vehicle.setImuUpdatePeriod(newImuPeriod);
                vehicle.setNavigatorName( (String)vNavMethodCBox.getSelectedItem() );

                // Display
                showVehicleProfile(vehSelectedProfile.getName(), false);
                vehNewButton.setEnabled(true);
                if ( vehList.getSelectedIndex() != -1){
                    vehEditButton.setEnabled(true);
                    vehDeleteButton.setEnabled(true);
                }
                envNewButton.setEnabled(true);
                if ( envList.getSelectedIndex() != -1){
                    envEditButton.setEnabled(true);
                    envDeleteButton.setEnabled(true);
                }
                if (   !( (DefaultListModel<String>)vehList.getModel() ).isEmpty()
                    && !( (DefaultListModel<String>)envList.getModel() ).isEmpty()){
                    simButton.setEnabled(true);
                }
                
                // Save to file
                Logger.writeVehicleProfiles();
            }
        }

        // Restore a vehicle profile to its last saved state
        if ( event.equals("vCancelButton") ){
            // Save without updating
            Logger.writeVehicleProfiles();

            // Display
            showVehicleProfile(vehSelectedProfile.getName(), false);
            vehNewButton.setEnabled(true);
            if ( vehList.getSelectedIndex() != -1){
                vehEditButton.setEnabled(true);
                vehDeleteButton.setEnabled(true);
            }
            envNewButton.setEnabled(true);
            if ( envList.getSelectedIndex() != -1){
                envEditButton.setEnabled(true);
                envDeleteButton.setEnabled(true);
            }
            if (   !( (DefaultListModel<String>)vehList.getModel() ).isEmpty()
                && !( (DefaultListModel<String>)envList.getModel() ).isEmpty()){
                simButton.setEnabled(true);
            }
        }

        // New environment profile
        if ( event.equals("envNewButton") ){
            // Try to create a uniquely named EnvironmentProfile
            EnvironmentProfile ep = null;
            int i = 0;
            while (ep == null){
                ep = EnvironmentProfile.createNewEnvironmentProfile("EnvironmentProfile"+i);
                i++;
            }

            // Add it to the environment list
            ( (DefaultListModel<String>)envList.getModel() ).addElement( ep.getName() );
            envList.setSelectedIndex(envList.getModel().getSize()-1);

            // Set display
            showEnvironmentProfile(ep.getName(), true);
            vehNewButton.setEnabled(false);
            vehEditButton.setEnabled(false);
            vehDeleteButton.setEnabled(false);
            envNewButton.setEnabled(false);
            envEditButton.setEnabled(false);
            envDeleteButton.setEnabled(false);
            simButton.setEnabled(false);
        }

        // Edit environment profile
        if ( event.equals("envEditButton") ){
            // Set display
            showEnvironmentProfile(envSelectedProfile.getName(), true);
            vehNewButton.setEnabled(false);
            vehEditButton.setEnabled(false);
            vehDeleteButton.setEnabled(false);
            envNewButton.setEnabled(false);
            envEditButton.setEnabled(false);
            envDeleteButton.setEnabled(false);
            simButton.setEnabled(false);
        }

         // Delete vehicle profile
        if ( event.equals("envDeleteButton") ){
            String name = envSelectedProfile.getName();

            // Create confirmation dialog
            Object[] options = {"Delete", "Cancel"};
            int n = JOptionPane.showOptionDialog(mainFrame,
            "Are you sure you want to permanently delete \""+name
            +"\"?",
            "Delete Confirmation",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[1]);
            
            // Delete
            if (n == 0){
                ( (DefaultListModel<String>)envList.getModel() ).removeElement(name);
                EnvironmentProfile.deleteEnvironmentProfile(name);
                
                // Save to file
                Logger.writeEnvironmentProfiles();
            }

            // Display
            if (   !( (DefaultListModel<String>)vehList.getModel() ).isEmpty()
                && !( (DefaultListModel<String>)envList.getModel() ).isEmpty()){
                simButton.setEnabled(true);
            } else {
                simButton.setEnabled(false);
            }
        }

        // Save an environment profile (temporarily attempt to change values,
        // and if all of them are valid, set them and finish editing.)
        if ( event.equals("eSaveButton") ){
            Color badColor = new Color(255, 30, 30);
            Environment environment = envSelectedProfile.getEnvironment();
            boolean valid = true;       // If any fields are invalid, this is false

            // Check and store all fields
            String newName = "";

            // Environment profile name
            newName = eNameTField.getText();
            String oldName = envSelectedProfile.getName();
            if ( envSelectedProfile.setName(newName) ){
                //envSelectedProfile.setName(oldName);
                eNameTField.setBackground(Color.WHITE);
                eNameTField.setForeground(Color.BLACK);
            } else {
                valid = false;
                eNameTField.setBackground(badColor);
                eNameTField.setForeground(Color.WHITE);
            }
            
            // Save if all fields are valid
            if (valid){
                // Save to file
                eEnvMaker.save(environment);
                Logger.writeEnvironmentProfiles();

                // Update the list
                envSelectedProfile.setName(newName);
                int index = ( (DefaultListModel<String>)envList.getModel() ).indexOf(oldName);
                ( (DefaultListModel<String>)envList.getModel() ).set(index, newName);
                
                // Set display
                showEnvironmentProfile(envSelectedProfile.getName(), false);
                vehNewButton.setEnabled(true);
                if ( vehList.getSelectedIndex() != -1){
                    vehEditButton.setEnabled(true);
                    vehDeleteButton.setEnabled(true);
                }
                envNewButton.setEnabled(true);
                if ( envList.getSelectedIndex() != -1){
                    envEditButton.setEnabled(true);
                    envDeleteButton.setEnabled(true);
                }
                if (   !( (DefaultListModel<String>)vehList.getModel() ).isEmpty()
                    && !( (DefaultListModel<String>)envList.getModel() ).isEmpty()){
                    simButton.setEnabled(true);
                }
            }
        }
        
        // Restore an environment profile to its last saved state
        if ( event.equals("eCancelButton") ){
            // Save
            Logger.writeEnvironmentProfiles();

            // Set display
            showEnvironmentProfile(envSelectedProfile.getName(), false);
            vehNewButton.setEnabled(true);
            if ( vehList.getSelectedIndex() != -1){
                vehEditButton.setEnabled(true);
                vehDeleteButton.setEnabled(true);
            }
            envNewButton.setEnabled(true);
            if ( envList.getSelectedIndex() != -1){
                envEditButton.setEnabled(true);
                envDeleteButton.setEnabled(true);
            }
            if (   !( (DefaultListModel<String>)vehList.getModel() ).isEmpty()
                && !( (DefaultListModel<String>)envList.getModel() ).isEmpty()){
                simButton.setEnabled(true);
            }
        }

        // Prompt a user for simulation decisions
        if ( event.equals("simButton") ){
            // Get the string arrays
            Object[] vehObjects = ((DefaultListModel<String>)vehList.getModel()).toArray();
            String[] vehStrings = Arrays.copyOf(vehObjects, vehObjects.length, String[].class);
            Object[] envObjects = ((DefaultListModel<String>)envList.getModel()).toArray();
            String[] envStrings = Arrays.copyOf(envObjects, envObjects.length, String[].class);

            // Create message contents
            JComboBox<String> vehSelect = new JComboBox<String>(vehStrings);
            JComboBox<String> envSelect = new JComboBox<String>(envStrings);
            JCheckBox trailSelect = new JCheckBox("Trail", true);
            JCheckBox timerSelect = new JCheckBox("Timer");
            Object[] options = {"Simulate", "Cancel"};
            Object[] message = {
                "Select a vehicle profile:   ", vehSelect,
                "\n\nSelect an environment profile:   ", envSelect,
                "\n\nOptions:", trailSelect, timerSelect
            };

            // Display message
            int option = JOptionPane.showOptionDialog(
                    mainFrame,
                    message,
                    "Simulate",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[1]);
                    
            // Parse response
            if (option == 0){
                System.out.println("BEGIN SIMULATION");
                VehicleProfile vp = VehicleProfile.getVehicleProfile((String)vehSelect.getSelectedItem());
                EnvironmentProfile ep = EnvironmentProfile.getEnvironmentProfile((String)envSelect.getSelectedItem());
                boolean trails = trailSelect.isSelected();
                boolean timer = timerSelect.isSelected();
				
				// Create a non-EDT thread
				(new Thread(new Simulator(vp.getVehicle(), ep.getEnvironment(), trails, timer))).start();
				//mainFrame.setVisible(false);;
            }
        }

        // Create a copy of the currently selected vehicle profile
        if ( event.equals("vehCopy") ){
            System.out.println("vehCopy");
            // Set display
            String name = vehSelectedProfile.getName();

            // Find if name ends in a digit we can increment
            
        }
        if ( event.equals("envCopy") ){
            System.out.println("envCopy");
        }
    }

    /* When list selection is changed, this is where it's handled. All events
     * have a source with a name that matches the variable name of that source.*/
    @Override
    @SuppressWarnings("unchecked")
    public void valueChanged(ListSelectionEvent e){
        // Ignore incomplete changes
        if ( e.getValueIsAdjusting()){
            return;
        }
        JList<String> list = (JList<String>) e.getSource(); // Gives an 'unchecked' warning
        
        // Vehicle Profile List
        if (list.getName().equals("vehList") ){
            // Set display
            vehSelectedIndex = list.getSelectedIndex();
            vehNewButton.setEnabled(true);
            envNewButton.setEnabled(true);
            if (vehSelectedIndex == -1 ){
                showVehicleProfile("", false);
                vehEditButton.setEnabled(false);
                vehDeleteButton.setEnabled(false);
            } else {
                String name = list.getSelectedValue();
                envList.clearSelection();
                showVehicleProfile(name, false);
                vehEditButton.setEnabled(true);
                vehDeleteButton.setEnabled(true);
                if (   !( (DefaultListModel<String>)vehList.getModel() ).isEmpty()
                    && !( (DefaultListModel<String>)envList.getModel() ).isEmpty()){
                    simButton.setEnabled(true);
                } else {
                    simButton.setEnabled(false);
                }
            }
        }
        
        // Environment Profile List
        if (list.getName().equals("envList") ){
            // Set display
            envSelectedIndex = list.getSelectedIndex();
            vehNewButton.setEnabled(true);
            envNewButton.setEnabled(true);
            if (envSelectedIndex == -1 ){
                showEnvironmentProfile("", false);
                envEditButton.setEnabled(false);
                envDeleteButton.setEnabled(false);
            } else {
                String name = list.getSelectedValue();
                vehList.clearSelection();
                showEnvironmentProfile(name, false);
                envEditButton.setEnabled(true);
                envDeleteButton.setEnabled(true);
                if (   !( (DefaultListModel<String>)vehList.getModel() ).isEmpty()
                    && !( (DefaultListModel<String>)envList.getModel() ).isEmpty()){
                    simButton.setEnabled(true);
                } else {
                    simButton.setEnabled(false);
                }
            }
        }
    }
}
