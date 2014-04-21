package layout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class LoginView extends JFrame{
	private JPanel _panel;
	private JLabel _userLabel;
	private JTextField _userText;
	private JLabel _passwordLabel;
	private JPasswordField _passwordText;
	private JButton _loginButton;
	private JButton _registerButton;
	
	public LoginView(){
	    try {
	    	init();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void init() throws Exception{
		this.setSize(300, 150);
		this.setTitle("log in");
		
		_panel = new JPanel();
		this.add(_panel);
		_panel.setLayout(null);

		_userLabel = new JLabel("User");
		_userLabel.setBounds(10, 10, 80, 25);
		_panel.add(_userLabel);

		_userText = new JTextField(20);
		_userText.setBounds(100, 10, 160, 25);
		_panel.add(_userText);

		_passwordLabel = new JLabel("Password");
		_passwordLabel.setBounds(10, 40, 80, 25);
		_panel.add(_passwordLabel);

		_passwordText = new JPasswordField(20);
		_passwordText.setBounds(100, 40, 160, 25);
		_panel.add(_passwordText);

		_loginButton = new JButton("login");
		_loginButton.setBounds(10, 80, 80, 25);
		_panel.add(_loginButton);
		
        _loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	// TO DO
            }
        });
		
		_registerButton = new JButton("register");
		_registerButton.setBounds(180, 80, 80, 25);
		_panel.add(_registerButton);
		
        _registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	// TO DO
            }
        });
	}
}
