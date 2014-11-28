package cornerfinders.recognizers;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.tamu.hammond.sketch.input.DrawPanel;

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
	 * Draw panel to use for test
	 */
	protected DrawPanel m_drawPanel;

	/**
	 * Default constructor
	 */
	public PaleoDemo() {
		super("Paleo Test");
		setSize(800,600);
		m_drawPanel = new DrawPanel();
		m_drawPanel.enableRightClickMenu(false);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// create bottom button panel
		JPanel bottomPanel = new JPanel();
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				m_drawPanel.clear();
			}
		});
		JButton recognize = new JButton("Recognize");
		recognize.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				m_drawPanel.recognizeAll();
			}
		});
		bottomPanel.add(clear);
		bottomPanel.add(recognize);

		// add components
		getContentPane().add(m_drawPanel, BorderLayout.CENTER);
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
