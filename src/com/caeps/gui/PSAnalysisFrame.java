package com.caeps.gui;

import javax.swing.JFrame;

/**
 * The Class PSAnalysisFrame.
 */
public class PSAnalysisFrame  {
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String args[]){
		PSAnalysisPanel psAnalysisPanel = new PSAnalysisPanel();
		JFrame frame = new JFrame("Computer Applications in Power Systems - Machine Learning");
		frame.setContentPane(psAnalysisPanel);
	      frame.setSize(1200,800);
	      frame.setVisible(true);
	      frame.setResizable(false);
	}

}
