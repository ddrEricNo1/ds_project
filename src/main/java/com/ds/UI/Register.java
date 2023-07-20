package com.ds.UI;
import com.ds.Main.Client;
import com.ds.role.remoteClient.RemoteClient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
public class Register extends JFrame {
    private ImageIcon background;
    private JLabel backgroundLabel;
    private JLabel headerLabel;
    private JLabel emailLabel;
    private JTextField emailField;
    private JLabel psdLabel;
    private JPasswordField passwordField;
    private JCheckBox checkBox;
    private JButton loginButton;private JLabel errorMessage;
    public boolean flag = false;
    public Client client;
    private final RemoteClient rc;
    public Register(RemoteClient rc, Client client) {
        super("Register page");
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.getContentPane().setLayout(null);
        this.emailLabel = new JLabel("Account: ");
        this.emailField = new JTextField();
        this.psdLabel = new JLabel("Password: ");
        this.passwordField = new JPasswordField();
        this.checkBox = new JCheckBox("Show");
        this.headerLabel = new JLabel("Register the account");
        this.loginButton = new JButton("Register");
        this.errorMessage = new JLabel("invalid email!");
        this.rc = rc;
        JPanel imPanel = (JPanel) this.getContentPane();
// set the panel to be transparent
        imPanel.setOpaque(false);
// read the background image
        this.background = new ImageIcon("src/main/img/background.jpg");
        this.backgroundLabel = new JLabel(this.background);
        this.backgroundLabel.setBounds(0, 0, this.getWidth(), this.getHeight());
        background.setImage(background.getImage().getScaledInstance(backgroundLabel.getWidth(), backgroundLabel.getHeight(), Image.SCALE_DEFAULT));
// add the label into the panel
        this.getLayeredPane().add(backgroundLabel, Integer.valueOf(Integer.MIN_VALUE));
        Font f3 = new Font("Arial Black", Font.BOLD, 34);
        Font f1 = new Font("Arial Black", Font.BOLD, 26);
        Font f2 = new Font("Arial Black", Font.PLAIN, 20);
// set the header of the login page
        this.headerLabel.setFont(f3);
        this.headerLabel.setForeground(Color.white);
        this.headerLabel.setBounds(this.getWidth() / 4, this.getHeight() / 10, this.getWidth() / 2
                + 50, 60);
        this.headerLabel.setHorizontalAlignment(JLabel.CENTER);
        this.emailLabel.setFont(f1);
        this.emailLabel.setForeground(Color.white);
        this.emailLabel.setBounds(this.getWidth() / 20,this.getHeight()/3,175,50);
        this.emailLabel.setHorizontalAlignment(JLabel.RIGHT);
        this.emailField.setHorizontalAlignment(JTextField.LEFT);
        this.emailField.setBounds(this.getWidth() / 2 - 150, this.getHeight() / 3, 380, 45);
// this.emailField.setOpaque(false);
        this.emailField.setFont(f2);
        this.emailField.setForeground(Color.BLACK);
        this.psdLabel.setFont(f1);
        this.psdLabel.setForeground(Color.white);
        this.psdLabel.setBounds(this.getWidth()/ 20, this.getHeight() / 3 + 100, 175, 50);
        this.psdLabel.setHorizontalAlignment(JLabel.RIGHT);
        this.checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {if (e.getStateChange() == ItemEvent.SELECTED) {
                passwordField.setEchoChar((char)0);
            } else {
                passwordField.setEchoChar('*');
            }
            }
        });
        this.checkBox.setBounds(this.getWidth() / 2 + 150, this.getHeight() / 3 + 115, 70, 30);
        this.checkBox.setOpaque(false);
        this.checkBox.setForeground(Color.BLACK);
        this.passwordField.setHorizontalAlignment(JTextField.LEFT);
        this.passwordField.setFont(f2);
        this.passwordField.setBounds(this.getWidth() / 2 - 150, this.getHeight() / 3 + 110, 380,
                40);
// this.passwordField.setOpaque(false);
        this.passwordField.setForeground(Color.BLACK);
        this.loginButton.setBounds(355, 450, 90,30);
        this.loginButton.setOpaque(false);
        this.loginButton.setForeground(new Color(255, 150, 40));
        this.loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    boolean result = rc.registerUser(emailField.getText(),
                            String.valueOf(passwordField.getPassword()));
                    if (result == true) {
                        System.out.println("successfully registered");
                        client.changeFrame(new Login(rc, client));
                        dispose();
                    } else {
                        System.out.println("register failed");errorMessage.setVisible(true);
                    }
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                } catch (NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        this.errorMessage.setFont(new Font("Arial Black", Font.ITALIC, 12));
        this.errorMessage.setForeground(Color.red);
        this.errorMessage.setBounds(500, 350, 160, 60);
        this.errorMessage.setVisible(false);
// add components to the panel
        this.getContentPane().add(this.emailLabel);
        this.getContentPane().add(this.headerLabel);
        this.getContentPane().add(this.psdLabel);
        this.getContentPane().add(this.emailField);
        this.getContentPane().add(this.passwordField);
        this.getContentPane().add(this.checkBox, 1);
        this.getContentPane().add(this.loginButton);
        this.getContentPane().add(this.errorMessage);
        this.setVisible(true);
    }
}