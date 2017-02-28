package vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Plot 2D panel
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class Plot2D_dialog extends JFrame {

    private JPanel contentPane;

    /**
     * Launch the application.
     */
    public PlotPanel2D plotPanel2D;
    /**
     * Create the frame.
     */
    public Plot2D_dialog() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 696, 515);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        
        plotPanel2D = new PlotPanel2D();
        contentPane.add(plotPanel2D, BorderLayout.CENTER);
    }
    
    
    
}
