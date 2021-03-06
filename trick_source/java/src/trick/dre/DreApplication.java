
//========================================
//	Package
//========================================
package trick.dre;

//========================================
//	Imports
//========================================
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.tree.TreePath;
import javax.xml.parsers.ParserConfigurationException;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.View;
import org.jdesktop.swingx.JXLabel;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import trick.common.TrickApplication;
import trick.common.ui.UIUtils;
import trick.common.ui.components.NumberTextField;
import trick.common.ui.panels.ListPanel;
import trick.sie.utils.SearchPanel;
import trick.sie.utils.SieResourceDomParser;
import trick.sie.utils.SieTemplate;
import trick.sie.utils.SieTreeModel;
import trick.sie.utils.SieVariableTree;


/**
 * Dre - data recording editor application.
 *
 * @author Hong Chen	
 * @since Trick 10
 */
public class DreApplication extends TrickApplication {
	//========================================
	//	Public data
	//========================================
	
	
	//========================================
	//	Protected data
	//========================================
	protected static String sieResourcePath = null;
	
	//========================================
	//	Private Data
	//========================================
	private String single_prec_only;
	private String frequency;
	private String format;
	private String buffering;	
	private Vector<String> variables = new Vector<String>();
	
	/** The menu check box for Single Precision. */
	private JCheckBoxMenuItem singlePrecisionCheckBox;
	
	/** Popup window for clicking a tree node **/
    private JPopupMenu treePopup = null;
    
    /** S_sie.resource xml parser */
    //private SieResourceXMLParser sieXMLParser;
    
    /** The sim objects top level instances */
    private Collection<SieTemplate> rootTemplates;
    
    /** The variable (sim objects) tree */
    private SieVariableTree varTree;
    
    /** The search panel for the variable tree */
    private SearchPanel searchPanel;
        
    /** The selected variable list */
    private ListPanel selectedVarList;   
    
    /** The text field that contains the group name */
    private JTextField nameField;
    
    /** The text field that contains the cycle frequency for recording */
    private NumberTextField cycleField;
    
    private JRadioButtonMenuItem DRAscii_item;
    private JRadioButtonMenuItem DRBinary_item;
    private JRadioButtonMenuItem DRHDF5_item;
    
    private JRadioButtonMenuItem DRBuffer_item;
    private JRadioButtonMenuItem DRNoBuffer_item;
    private JRadioButtonMenuItem DRRingBuffer_item;
    //private JRadioButtonMenuItem DRThreadBuffer_item;
    
    private JRadioButtonMenuItem DRAlways_item;
    private JRadioButtonMenuItem DRChanges_item;
    private JRadioButtonMenuItem DRStepChanges_item;
    
    private boolean isSinglePrecision;
    
    /**
     * Vectors to contain the information on the variable
     * being added to the recording list.
     */
    Vector<VariableName> nameSegment = new Vector<VariableName>();
    Vector<String> fullName = new Vector<String>();
    
	//========================================
	//	Constructors
	//========================================
	
    //========================================
    //    Actions
    //========================================
    @Action
    public void openDR() {
    	File file = UIUtils.chooseOpenFile(null, null, "dr", getMainFrame());
    	if (file != null) {
    		openFile(file);
    	}
    }
    
    @Action
    public void saveDR() {
        if (nameField.getText().trim().compareTo("") == 0) {
     		JOptionPane.showMessageDialog(getMainFrame(), "A group name must be entered!",
    				"Error", JOptionPane.ERROR_MESSAGE);
     		return;
        } else if (nameField.getText().trim().contains(" ")) {
        	JOptionPane.showMessageDialog(getMainFrame(), "A group name can not have whitespace!",
    				"Error", JOptionPane.ERROR_MESSAGE);
        	return;
        }
        File file = UIUtils.chooseSaveFile(null, null, "dr", getMainFrame());
        if (file != null) {
        	saveFile(file);
        }
    }
    
    @Action
    public void selectDRBinary() {
    	format = "DRBinary";
    	DRAscii_item.setSelected(false);
    	DRBinary_item.setSelected(true);
    	DRHDF5_item.setSelected(false);
    }
    
    @Action
    public void selectDRAscii() {
    	format = "DRAscii";
    	DRAscii_item.setSelected(true);
    	DRBinary_item.setSelected(false);
    	DRHDF5_item.setSelected(false);
    }
    
    @Action
    public void selectDRHDF5() {
    	format = "DRHDF5";
    	DRAscii_item.setSelected(false);
    	DRBinary_item.setSelected(false);
    	DRHDF5_item.setSelected(true);
    } 
    
    @Action
    public void selectDRAlways() {
    	frequency = "DR_Always";
    	DRAlways_item.setSelected(true);
    	DRChanges_item.setSelected(false);
    	DRStepChanges_item.setSelected(false);
    }
    
    @Action
    public void selectDRChanges() {
    	frequency = "DR_Changes";
    	DRAlways_item.setSelected(false);
    	DRChanges_item.setSelected(true);
    	DRStepChanges_item.setSelected(false);
    }
 
    @Action
    public void selectDRStepChanges() {
    	frequency = "DR_Step_Changes";
    	DRAlways_item.setSelected(false);
    	DRChanges_item.setSelected(false);
    	DRStepChanges_item.setSelected(true);
    }    
    
    @Action
    public void toggleSinglePrecision() {
    	isSinglePrecision = singlePrecisionCheckBox.getState();
    	if (isSinglePrecision) {
    		single_prec_only = "True";
    	} else {
    		single_prec_only = "False";
    	}
    }
    
    @Action
    public void selectDRBuffer() {
    	buffering = "DR_Buffer";
    	DRBuffer_item.setSelected(true);
    	DRNoBuffer_item.setSelected(false);
    	DRRingBuffer_item.setSelected(false);
    	//DRThreadBuffer_item.setSelected(false);
    }
    
    @Action
    public void selectDRNoBuffer() {
    	buffering = "DR_No_Buffer";
    	DRBuffer_item.setSelected(false);
    	DRNoBuffer_item.setSelected(true);
    	DRRingBuffer_item.setSelected(false);
    	//DRThreadBuffer_item.setSelected(false);
    }
    
    @Action
    public void selectDRRingBuffer() {
    	buffering = "DR_Ring_Buffer";
    	DRBuffer_item.setSelected(false);
    	DRNoBuffer_item.setSelected(false);
    	DRRingBuffer_item.setSelected(true);
    	//DRThreadBuffer_item.setSelected(false);
    }
 
    @Action
    public void selectDRThreadBuffer() {
    	buffering = "DR_Thread_Buffer";
    	DRBuffer_item.setSelected(false);
    	DRNoBuffer_item.setSelected(false);
    	DRRingBuffer_item.setSelected(false);
    	//DRThreadBuffer_item.setSelected(true);
    }
        
    @Action
    public void removeSelected() {
    	Object[] values = selectedVarList.getSelectedData();
    	for (int i = 0; i < values.length; i++) {
    		variables.remove(values[i]);
    	}
    	selectedVarList.removeSelectedData();
    }
	
    @Action
    public void removeAll() {
    	selectedVarList.removeAllData();
    	variables.clear();
    }
    
    @Action
    public void addVariables() {
        for (SieTemplate thisValue : searchPanel.getSelectedValues()) {
            addVariable(thisValue.toString());
        }
    }
    
	//========================================
	//	Set/Get methods
	//========================================
	
    
    
	//========================================
	//	Methods
	//========================================	
	/**
     * Main method for this application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Application.launch(DreApplication.class, args);
    }
    
    /**
     * Makes initialization as needed. This is called before startup().
     * 
     * @see #startup
     */    
    @Override
    protected void initialize(String[] args) {
        super.initialize(args);
        File resourceFile = null;
        if (sieResourcePath == null) {
        	resourceFile = new File(resourceMap.getString("sie.resource.file"));
        } else {
        	resourceFile = new File(sieResourcePath);
        }
        //sieXMLParser = null;
        if (resourceFile != null && !resourceFile.exists()) {
            System.out.println(resourceFile.getName() + " file does not exist. Exit!!!");
            System.exit(0);
        }
    
        try {
			rootTemplates = SieResourceDomParser.parse(new InputSource(new FileInputStream(resourceFile)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        /*try {        	
            sieXMLParser = new SieResourceXMLParser(resourceFile.getCanonicalPath());           
            sieXMLParser.runParser();
        } catch (Exception e) {
            System.out.println("Failed to parse " + resourceFile.getName() + "!");
            System.exit(0);
        }*/
        
        treePopup = new JPopupMenu();
               
        varTree = new SieVariableTree();
        varTree.setRootInstances(rootTemplates);
        
        varTree.setShowsRootHandles(true);           
    }
    
    /**
     * Starts building GUI. This is called after initialize. 
     * Once startup() is done, ready() is called.
     * 
     * @see #initialize
     * @see #ready
     */
    @Override
    protected void startup() {
        super.startup();
        
        View view = getMainView();
        view.setComponent(createMainPanel());
        view.setMenuBar(createMenuBar());
        view.setToolBar(createToolBar());
        
        show(view);
    }

    /**
     * Creates the main panel. This is required by TrickApplication.
     * 
     * @return a {@link JComponent} as the main panel.
     */
    @Override
	protected JComponent createMainPanel() {
    	JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    	JSplitPane treeSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    	    	
    	searchPanel = new SearchPanel();
        
        searchPanel.setAction(actionMap.get("addVariables"));
        
        searchPanel.setRootTemplates(rootTemplates);
        
        treeSplitPane.add(UIUtils.createSearchableTitledPanel("Variables", varTree, null));
        treeSplitPane.add(searchPanel);
    	    	
        splitPane.add(treeSplitPane);
    	splitPane.add(UIUtils.createSearchableTitledPanel("Selected Variables", createSelectedVariablesPanel(), null));
    
    	 // create a mouse listener and add to tree
        TreeMouseListener mouser = new TreeMouseListener();
        varTree.addMouseListener(mouser); 
        
        return splitPane;
    }
    
    /**
     * Helper method for creating the variables tab.
     */
    private JComponent createSelectedVariablesPanel() {
    	JPanel panel = new JPanel();
    	panel.setLayout(new BorderLayout());
    	panel.add(new JSeparator(), BorderLayout.NORTH);
    	selectedVarList = new ListPanel();
    	String[] popupMenuActions = { "removeSelected", "removeAll"};
        selectedVarList.setPopup(createPopupMenu(popupMenuActions), 0);
    	panel.add(selectedVarList, BorderLayout.CENTER);
    	return panel;
    }
    
    /**
     * Create the JMenuBar for this application. 
     */
    @Override
	protected JMenuBar createMenuBar() {
    	JMenuBar menuBar = super.createMenuBar();
    	JMenu menu = menuBar.getMenu(0);
        menu.add(new JSeparator(), 0);
        menu.add(new JMenuItem(getAction("saveDR")), 0);
        menu.add(new JMenuItem(getAction("openDR")), 0);
        
        menuBar.add(createOptionsMenu(), 1);
        
        return menuBar;
    }

    /**
     * Helper method for creating Options menu.
     */
    private JMenu createOptionsMenu() {
    	JMenu optionsMenu = new JMenu();
    	optionsMenu.setName("optionsMenu");
    	
    	optionsMenu.add(new JLabel("Format		"));
 
     	DRBinary_item = new JRadioButtonMenuItem();
        optionsMenu.add(DRBinary_item);
        DRBinary_item.setAction(getAction("selectDRBinary"));
        
    	DRAscii_item = new JRadioButtonMenuItem();
        optionsMenu.add(DRAscii_item);
        DRAscii_item.setAction(getAction("selectDRAscii"));
        
    	DRHDF5_item = new JRadioButtonMenuItem();
        optionsMenu.add(DRHDF5_item);
        DRHDF5_item.setAction(getAction("selectDRHDF5"));
        
        selectDRBinary(); // by default, DR_Binary      
        optionsMenu.addSeparator();
        
        optionsMenu.add(new JLabel("Freq"));
        
     	DRAlways_item = new JRadioButtonMenuItem();
        optionsMenu.add(DRAlways_item);
        DRAlways_item.setAction(getAction("selectDRAlways"));
        
    	DRChanges_item = new JRadioButtonMenuItem();
        optionsMenu.add(DRChanges_item);
        DRChanges_item.setAction(getAction("selectDRChanges"));
        
    	DRStepChanges_item = new JRadioButtonMenuItem();
        optionsMenu.add(DRStepChanges_item);
        DRStepChanges_item.setAction(getAction("selectDRStepChanges"));
        
        selectDRAlways(); // by default, DR_Always
        
        optionsMenu.addSeparator();
        
        singlePrecisionCheckBox = new JCheckBoxMenuItem(getAction("toggleSinglePrecision"));
        toggleSinglePrecision();
        
        optionsMenu.add(singlePrecisionCheckBox);
        optionsMenu.addSeparator();
        
        optionsMenu.add(new JXLabel("Buffering"));

     	DRBuffer_item = new JRadioButtonMenuItem();
        optionsMenu.add(DRBuffer_item);
        DRBuffer_item.setAction(getAction("selectDRBuffer"));
        
     	DRNoBuffer_item = new JRadioButtonMenuItem();
        optionsMenu.add(DRNoBuffer_item);
        DRNoBuffer_item.setAction(getAction("selectDRNoBuffer"));
        
     	DRRingBuffer_item = new JRadioButtonMenuItem();
        optionsMenu.add(DRRingBuffer_item);
        DRRingBuffer_item.setAction(getAction("selectDRRingBuffer"));        
        
     	//DRThreadBuffer_item = new JRadioButtonMenuItem("selectDRThreadBuffer");
        //optionsMenu.add(DRThreadBuffer_item);
        //DRThreadBuffer_item.setAction(getAction("selectDRThreadBuffer"));
        
        selectDRBuffer(); // by default, DR_Buffer
        
    	return optionsMenu;
    }
    
    /**
     * Creates the tool bar for the application.
     * 
     * @return a {@link JToolBar} for the application.
     */
    @Override
    protected JToolBar createToolBar() {
        String[] toolbarActionNames = {"openDR", "saveDR"};
        JToolBar toolBar = new JToolBar(); 
        for (String actionName : toolbarActionNames) {
            if (actionName.equals("---")) {
                toolBar.addSeparator();
            } else {
                toolBar.add(createButton(actionName, false));
            }
        }      
        toolBar.addSeparator();       
        toolBar.add(new JLabel("DR Name (NO SPACE):  "));  
        nameField = new JTextField(15);
        nameField.setMinimumSize(nameField.getPreferredSize());
        nameField.setPreferredSize(nameField.getPreferredSize());
        nameField.setMaximumSize(nameField.getPreferredSize());        
        toolBar.add(nameField);
        
        toolBar.add(Box.createHorizontalStrut(10));
        
        toolBar.add(new JLabel("DR Cycle:  "));   
        cycleField= new NumberTextField("0.1", 5);
        cycleField.setMinimumSize(cycleField.getPreferredSize());       
        toolBar.add(cycleField);
        
        return toolBar;
    }
    
    /**
     * routine to read the Frequency from the opened file.
     * 
     * @param string String the string read in from the opened file.
     */
    private void readFrequency(String string) {
    	if (string.indexOf("trick.DR_Always") != -1) {
    		selectDRAlways();
    	} else if (string.indexOf("trick.DR_Changes") != -1) {
    		selectDRChanges();
    	} else if (string.indexOf("trick.DR_Step_Changes") != -1) {
    		selectDRStepChanges();
    	} else {
    		System.out.println("Frequency Type is not recognized, defaulting to DR_Always");
    		selectDRAlways();
    	}
    }
    
    /**
     * routine to read the Buffering from the opened file.
     * 
     * @param string String the string read in from the opened file.
     */
    private void readBuffering(String string) {
    	if (string.indexOf("trick.DR_Buffer") != -1) {
    		selectDRBuffer();
    	} else if (string.indexOf("trick.DR_No_Buffer") != -1) {
    		selectDRNoBuffer();
    	} else if (string.indexOf("trick.DR_Ring_Buffer") != -1) {
    		selectDRRingBuffer();
    	} else if (string.indexOf("trick.DR_Thread_Buffer") != -1) {
    		selectDRThreadBuffer();
    	} else {
    		System.out.println("Buffering Type is not recognized, defaulting to DR_Buffer");
    		selectDRBuffer();
    	}
    }
    
    /**
     * routine to read the Format from the opened file.
     * 
     * @param string String the string read in from the opened file.
     */
    private void readFormat(String string) {
    	if (string.indexOf("DRBinary") != -1) {
    		selectDRBinary();
    	} else if (string.indexOf("DRAscii") != -1) {
    		selectDRAscii();
    	} else if (string.indexOf("DRHDF5") != -1) {
    		selectDRHDF5();
    	} else {
    		System.out.println("Format Type is not recognized, defaulting to DR_Binary");
    		selectDRBinary();
    	}
    }
    
    /**
     * routine to read the contents of the opened file
     * 
     * @param file File the name of the file to open.
     */
    private void openFile(File file) {
    	try {
    		variables.clear();
    		selectedVarList.removeAllData();
    		BufferedReader reader = new BufferedReader(new FileReader(file));
    		try {
    			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
    				if (line.indexOf("append") != -1) {
    					String[] segment = line.split("\"");
    					readFormat(line);
    					nameField.setText(segment[1]);    			
    				} else if (line.indexOf("add_data_record_group") != -1) {
    				    readBuffering(line);
    				} else if (line.indexOf("drg[DR_GROUP_ID]") != -1) {
    					int indx = line.indexOf("(");
    					int len = line.length();
    					if (line.indexOf("set_freq") != -1) {
    						readFrequency(line);
    					} else if (line.indexOf("enable") != -1) {
    						;
    					} else if (line.indexOf("set_cycle") != -1) {
    						cycleField.setText(line.substring(indx+1,len-1));
    					} else if (line.indexOf("add_variable") != -1) {
    						selectedVarList.addData(line.substring(indx+2,len-2));
    						variables.add(line.substring(indx+2,len-2));
    					} else if (line.indexOf("set_single_prec_only") != -1) {
    						if (line.substring(indx+1,len-1).equals("True")) {
    							singlePrecisionCheckBox.setState(true);
    						} else {
    							singlePrecisionCheckBox.setState(false);
    						}
    						
    					}
    				} 
    			}
    		}
    		finally {
    			if (reader != null) {
    				reader.close();
    			}
    		}
    	}
    	catch (Exception e) {
    		JOptionPane.showMessageDialog(getMainFrame(), e.toString(), "Error Reading File", JOptionPane.ERROR_MESSAGE);
    	}
    }
    
    /**
     * routine to write the saved options for data recording to a file.
     * 
     * @param file File the name of the file to save
     */
    private void saveFile(File file) {
    	try {
    		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    		try {
    			writer.write("global DR_GROUP_ID\n");
    			writer.write("global drg\n");
    			writer.write("try:\n");
    			writer.write("    if DR_GROUP_ID >= 0:\n");
    			writer.write("        DR_GROUP_ID += 1\n");
    			writer.write("except NameError:\n");
    			writer.write("    DR_GROUP_ID = 0\n" +
    					     "    drg = []\n\n"); 
    			writer.write("drg.append(trick." + format + "(\"" + nameField.getText().trim() + "\"))\n");
    			writer.write("drg[DR_GROUP_ID].set_freq(trick." + frequency + ")\n");
    			writer.write("drg[DR_GROUP_ID].set_cycle(" + cycleField.getText() + ")\n");
    			writer.write("drg[DR_GROUP_ID].set_single_prec_only(" + single_prec_only + ")\n");

    			for (int i = 0; i < variables.size(); i++) {
    				writer.write("drg[DR_GROUP_ID].add_variable(\"" + variables.get(i) + "\")\n");
    			}
    			writer.write("trick.add_data_record_group(drg[DR_GROUP_ID], trick." + buffering + ")\n");
    			writer.write("drg[DR_GROUP_ID].enable()\n");
    		}
    		finally {
    			if (writer != null) {
    				writer.close();
    			}
    		}
    	}
    	catch (Exception e) {
    		JOptionPane.showMessageDialog(getMainFrame(), e.toString(),
    				"Error Saving File", JOptionPane.ERROR_MESSAGE);
    	}
    }    
    
    /**
     * routine to add the subscripts to the variable name being created
     * 
     * @param index int the size of the segments array.
     */
    private void addSubscript(int index) {
        if (nameSegment.get(index).dimensions.size() != 0) {
            for (int j=0; j<nameSegment.get(index).dimensions.size(); j++) {
                int total = fullName.size();
                for (int ii=0; ii<total; ii++) {
                    String temp = fullName.remove(0);
                    for (int jj=0; jj<nameSegment.get(index).dimensions.get(j); jj++) {
                        fullName.add(temp + "[" + jj + "]");
                    }
                    if (nameSegment.get(index).dimensions.get(j) == 0) {
                        fullName.add(temp + "[0]");
                    }
                }
            }
        }
    }
    
    /**
     * routine to add the segment name to the variable name being created.
     * 
     * @param name String the segment name of the variable to be data recorded.
     */
    private void addName(String name) {
        int total = fullName.size();
        for (int ii=0; ii<total; ii++) {
            String temp = fullName.remove(0);
            fullName.add(temp + "." + name);
        }
    }
    
    /**
     * routine to find the segments in the variable name and the subscripts
     * 
     * @param name String the variable's name to be data recorded.
     */
    private void addVariable(String name) {
        String[] segments = name.split("\\.");

        for (int i = 0; i < segments.length ; i++) {
            VariableName tempName = new VariableName();
            Matcher matcher = Pattern.compile("\\[.\\]").matcher(segments[i]);
            tempName.name = segments[i].replaceFirst("\\[.*\\]",""); 

            while (matcher.find()) {
                String str_idx = matcher.group().substring(1, matcher.group().length()-1);
                tempName.dimensions.add(Integer.parseInt(str_idx));
            }
            nameSegment.add(tempName);
        }
        
        fullName.add(nameSegment.get(0).name);
        addSubscript(0);
        for (int i = 1; i < nameSegment.size(); i++) {
            addName(nameSegment.get(i).name);
            addSubscript(i);
        }
        for (int i=0; i<fullName.size(); i++) {
            variables.add(fullName.get(i));
            selectedVarList.addData(fullName.get(i));
        }
        fullName.clear();
        nameSegment.clear();
    }
    
    //========================================
    //    Inner classes
    //========================================
    /**
     * private class to contain the name and the  
     * dimensions for each segment of variable added.
     */
    private class VariableName {
        String name;
        Vector<Integer> dimensions = new Vector<Integer>();
    }
    
    /**
     * Using an inner class to define MouseListener to help organize code better.
     * The goal of this class is to handle mouse calls and forward them
     * to the interested parties.
     */
    private class TreeMouseListener extends MouseAdapter {
        
        
        //========================================
        //    MouseListener methods
        //========================================
        /**
         * Invoked when the mouse button has been clicked (pressed
         * and released) on a component.
         *
         * @param e MouseEvent sent from system.
         */
        @Override
		public void mouseClicked(MouseEvent e) 
        {
        	SieTemplate clickedNode = null;
        	if (UIUtils.isDoubleClick(e) || UIUtils.isRightMouseClick(e)) {
        		TreePath clickedPath = varTree.getClosestPathForLocation(e.getX(), e.getY());               
                
                    clickedNode = (SieTemplate)clickedPath.getLastPathComponent();
                
                if (UIUtils.isRightMouseClick(e)) {
            		if (clickedNode != null && clickedNode.isTrickManaged()) {                    
                        if (treePopup.getComponentCount() > 0) {
                            treePopup.removeAll();
                        }
                        
                        JMenuItem firstItem = new JMenuItem(SieTreeModel.getPathName(clickedPath) + clickedNode);
                        if (clickedNode != null && varTree.getModel().isLeaf(clickedNode) && clickedNode.isTrickManaged()) {  
	                        firstItem.addActionListener(new ActionListener() {
	                        	public void actionPerformed(ActionEvent e) {         		 
	                        			addVariable(e.getActionCommand());	                        		
	                        	}
	                        });
	                        treePopup.add(firstItem);
            			} else {
            				treePopup.add(new JLabel("    " + SieTreeModel.getPathName(clickedPath) + clickedNode));
            			}
                                               
                            treePopup.addSeparator();
                            
                            if (clickedNode.enumeration != null) {
                                	JMenu subMenu = new JMenu("Type: " + clickedNode.typeName);
                                	for (final Object eachLabel : clickedNode.enumeration.pairs.keySet()) {
                                        subMenu.add((String)eachLabel);
                                    }                               
                                treePopup.add(subMenu);
                            } else {
                                treePopup.add("Type: " + clickedNode.typeName);
                            }
                            treePopup.add("Units: " + clickedNode.units);
                        
                        if (!treePopup.isVisible()) {    
                            treePopup.show(e.getComponent(), e.getX(), e.getY());
                        }                        
            		}            		
            	} else if (clickedNode != null && varTree.getModel().isLeaf(clickedNode) && clickedNode.isTrickManaged()) {
            		addVariable(SieTreeModel.getPathName(clickedPath) + clickedNode);
                }
        	}
        	
        	if (!UIUtils.isRightMouseClick(e)) {
        		if (treePopup.isVisible()) {
                    treePopup.setVisible(false);
                }
        	}                       	
        }   
    }
    
 }
