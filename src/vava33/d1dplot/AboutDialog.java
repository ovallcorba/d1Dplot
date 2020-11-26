package com.vava33.d1dplot;

/**
 * D1Dplot
 * 
 * About dialog
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.io.File;
import java.io.IOException;
import net.miginfocom.swing.MigLayout;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

public class AboutDialog {
	
	private JDialog aboutDialog;
    private JButton btnUsersGuide;
    private JPanel contentPanel;
    private JLabel lblTalplogo;
    private JEditorPane textPane;
    private JScrollPane scrollPane;
    private JLabel lblLogoalba;

    private static final String className = "About";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    /**
     * Create the dialog.
     */
    public AboutDialog(JFrame parent) {
    	this.aboutDialog = new JDialog(parent,"About d1Dplot",true);
    	this.contentPanel = new JPanel();
    	aboutDialog.setIconImage(D1Dplot_global.getIcon());
    	aboutDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        aboutDialog.setSize(620,700);
        D1Dplot_global.showOnScreen(D1Dplot_global.getDisplayMonitor(), aboutDialog, true);
        
        aboutDialog.getContentPane().setLayout(new BorderLayout());
        this.contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        aboutDialog.getContentPane().add(this.contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[grow]", "[64px][grow][]"));
        {
            lblTalplogo = new JLabel("** LOGO **");
            contentPanel.add(lblTalplogo, "flowx,cell 0 0,alignx center,aligny center");
        }
        {
            JPanel buttonPane = new JPanel();
            aboutDialog.getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                {
                    this.btnUsersGuide = new JButton("User's Guide");
                    this.btnUsersGuide.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            do_btnUsersGuide_actionPerformed(arg0);
                        }
                    });
                    buttonPane.setLayout(new MigLayout("", "[][grow]", "[]"));
                    buttonPane.add(this.btnUsersGuide, "cell 0 0,alignx left,aligny top");
                }
            }
            JButton okButton = new JButton("Close");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    do_okButton_actionPerformed(arg0);
                }
            });
            okButton.setActionCommand("OK");
            buttonPane.add(okButton, "cell 1 0,alignx right,aligny top");
            aboutDialog.getRootPane().setDefaultButton(okButton);
        }

        // posem el logo escalat
        Image img = D1Dplot_global.getIcon();
        Image newimg = img.getScaledInstance(-100, 64, java.awt.Image.SCALE_SMOOTH);
        ImageIcon logo = new ImageIcon(newimg);
        lblTalplogo.setText("");
        lblTalplogo.setIcon(logo);
        {
            scrollPane = new JScrollPane();
            scrollPane.setViewportBorder(null);
            scrollPane.setOpaque(false);
            contentPanel.add(scrollPane, "cell 0 1,grow");
            {
                textPane = new JEditorPane();
                textPane.setOpaque(false);
                scrollPane.setViewportView(textPane);
            }
        }

        // this.setIconImage(new
        // ImageIcon(getClass().getResource("/img/icona.png")).getImage());
        
        //llegim el fitxer html per poblar la label:
        
        textPane.setContentType("text/html");
        textPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        textPane.setEditable(false);
        
        textPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if(Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (Exception e1) {
                            log.warning("Error opening default browser");
                        }
                    }
                }
            }
        });
        {
            lblLogoalba = new JLabel("");
            lblLogoalba.setIcon(new ImageIcon(AboutDialog.class.getResource("/com/vava33/d1dplot/img/ALBALogo.png")));
            contentPanel.add(lblLogoalba, "cell 0 2,alignx center");
        }
        java.net.URL aboutURL = AboutDialog.class.getResource("/com/vava33/d1dplot/img/about.html");
        if (aboutURL != null) {
            try {
                textPane.setPage(aboutURL);
            } catch (IOException e) {
                log.warning("Error reading ABOUT information");
            }
        } else {
            log.warning("Couldn't find file: " + aboutURL);
        }
        
        scrollPane.getViewport().setOpaque(false);
       
    }

    protected void do_btnUsersGuide_actionPerformed(ActionEvent arg0) {
        openManual();
    }

    private void openManual(){
        try{
            if(Desktop.isDesktopSupported()){ // s'obre amb el programa per defecte
                Desktop.getDesktop().open(new File(D1Dplot_global.usersGuidePath));
                return;
            }else{
                if(FileUtils.confirmDialog(FileUtils.getOS(),"win")){
                    new ProcessBuilder("cmd","/c",D1Dplot_global.usersGuidePath).start();  
                }else{
                    throw new Exception(); 
                }
                return;
            }
        } catch (Exception e) {
            //error
        }
        FileUtils.InfoDialog(aboutDialog, "Sorry, unable to open user's guide with default pdf viewer. \n"
                + "Please open it manually from the program folder", "D1Dplot User's Guide");
    }
    
    private void do_okButton_actionPerformed(ActionEvent arg0) {
        this.tanca();
    }
    
    public void tanca() {
    	aboutDialog.dispose();
    }

	public void visible(boolean vis) {
		aboutDialog.setVisible(vis);
	}
}
