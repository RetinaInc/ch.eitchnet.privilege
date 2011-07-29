/*
 * Copyright (c) 2010
 * 
 * Apixxo AG 
 * Hauptgasse 25
 * 4600 Olten
 * 
 * All rights reserved.
 * 
 */

/**
 * 
 */
package ch.eitchnet.privilege.helper;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Simple Swing UI to create passwords
 * 
 * @author rvonburg
 */
public class PasswordCreaterUI {

	/**
	 * Launches the UI
	 * 
	 * @param args
	 *            not used
	 */
	public static void main(String[] args) {

		JFrame.setDefaultLookAndFeelDecorated(true);

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Password creator");
		frame.setLayout(new GridLayout(4, 2));

		JLabel digest = new JLabel("Digest:", SwingConstants.RIGHT);
		JLabel password = new JLabel("Password:", SwingConstants.RIGHT);
		JLabel hash = new JLabel("Hash:", SwingConstants.RIGHT);

		String[] digests = new String[] { "MD2", "MD5", "SHA-1", "SHA-256", "SHA-384", "SHA-512" };
		final JComboBox digestCombo = new JComboBox(digests);
		digestCombo.setSelectedIndex(3);
		final JPasswordField passwordField = new JPasswordField();
		final JTextField hashField = new JTextField(150);

		JButton digestBtn = new JButton("Digest");

		passwordField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				//
			}

			@Override
			public void keyReleased(KeyEvent e) {
				//
			}

			@Override
			public void keyPressed(KeyEvent e) {
				hashField.setText("");
			}
		});
		digestBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					String digest = (String) digestCombo.getSelectedItem();
					char[] passwordChar = passwordField.getPassword();
					String password = new String(passwordChar);
					String hash = HashHelper.stringToHash(digest, password);
					hashField.setText(hash);
				} catch (Exception e1) {
					e1.printStackTrace();
					hashField.setText("Failed: " + e1.getLocalizedMessage());
				}
			}
		});

		frame.add(digest);
		frame.add(digestCombo);
		frame.add(password);
		frame.add(passwordField);
		frame.add(hash);
		frame.add(hashField);
		frame.add(new JLabel());
		frame.add(digestBtn);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 500;
		int height = 160;
		frame.setSize(width, height);
		frame.setLocation(screenSize.width / 2 - width, screenSize.height / 2 - height);

		frame.setVisible(true);
	}
}
