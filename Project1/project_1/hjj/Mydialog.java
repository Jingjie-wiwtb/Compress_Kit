package hjj;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Font;
import javax.swing.JLabel;
import java.awt.Color;

@SuppressWarnings("serial")
public class Mydialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JLabel window_message;

	/**
	 * Launch the application.
	 */

	/**
	 * Create the dialog.
	 */
	/*
	public Mydialog() {
		getContentPane().setFont(new Font("Tekton Pro", Font.PLAIN, 30));
		
	}
*/	
	protected static void setVisible(String string) {
		// TODO Auto-generated method stub
		
	}

	public Mydialog(String message) {
		setBackground(new Color(245, 245, 245));
		setBounds(200, 200, 600, 400);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			window_message = new JLabel();
			window_message.setFont(new Font("Tekton Pro", Font.PLAIN, 30));
			window_message.setBounds(41, 33, 452, 162);
			contentPanel.add(window_message);
			window_message.setText(message);
		}
		{
			JButton okButton = new JButton("Back");
			okButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					dispose();
				}
			});
			okButton.setBounds(199, 239, 167, 37);
			contentPanel.add(okButton);
			okButton.setActionCommand("OK");
			okButton.setFont(new Font("Tekton Pro Ext", Font.ITALIC, 24));
			getRootPane().setDefaultButton(okButton);
		}
		
	}

}
