package cornerfinders.recognizers;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
/**
 * Test draw panel for PaleoSketch
 *
 * @author bpaulson
 */
public class PaleoDemo extends JFrame {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -7184108573941373162L;


	/**
	 * Default constructor
	 */
	public PaleoDemo() {
		super("Paleo Test");
		setSize(800,600);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// create bottom button panel
		JPanel bottomPanel = new JPanel();
		JButton clear = new JButton("Clear");
        bottomPanel.add(clear);
    	getContentPane().add(bottomPanel, BorderLayout.SOUTH);
	}

	/**
	 * @param args
	 *            not needed
	 */
	public static void main(String[] args) {
		PaleoDemo p = new PaleoDemo();
		p.setVisible(true);
	}

}
