/*
 * 
 */
package com.caeps.gui;

import javax.swing.JApplet;

/**
 * The Class PSAnalysisApplet.
 */
public class PSAnalysisApplet extends JApplet {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see java.applet.Applet#init()
	 */
	public void init() {
		PSAnalysisPanel psAnalysisPanel = new PSAnalysisPanel();
		setSize(1200, 800);
		add(psAnalysisPanel);
	}

}
