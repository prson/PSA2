package com.caeps.gui;

import javax.swing.JFrame;

public class PSAnalysisFrame  {
	
	public static void main(String args[]){
		PSAnalysisPanel psAnalysisPanel = new PSAnalysisPanel();
		JFrame frame = new JFrame("Computer Applications in Power Systems - Machine Learning");
		frame.setContentPane(psAnalysisPanel);
	      frame.setSize(850,650);
	      frame.setVisible(true);
	      frame.setResizable(false);
	}

}
