package com.caeps.gui;

import javax.swing.JApplet;

public class PSAnalysisApplet extends JApplet {
	private static final long serialVersionUID = 1L;

	public void init() {
		PSAnalysisPanel psAnalysisPanel = new PSAnalysisPanel();
		setSize(850, 600);
		add(psAnalysisPanel);
	}

}
