package com.ds.UI;
import com.ds.Main.Client;
import com.ds.role.remoteClient.RemoteClient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Enumeration;
public class Login extends JFrame {
    // background image for login page
    private ImageIcon background;
    private JLabel backgroundLabel;
    private JLabel headerLabel;
    private JLabel emailLabel;
    private JTextField emailField;
    private JLabel psdLabel;
    private JPasswordField passwordField;
    private JCheckBox checkBox;
    private JButton loginButton;
    private JLabel errorMessage;
    public boolean flag = false;
    public JButton registerButton;
    public Client client;
    private final RemoteClient rc;

    public Login(RemoteClient rc, Client client) {
        super("login page");
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.getContentPane().setLayout(null);
        this.emailLabel = new JLabel("Account: ");
        this.emailField = new JTextField();
        this.psdLabel = new JLabel("Password: ");
        this.passwordField = new JPasswordField();
        this.checkBox = new JCheckBox("Show");
        this.headerLabel = new JLabel("Login to account");
        this.loginButton = new JButton("Login");
        this.registerButton = new JButton("Register");
        this.errorMessage = new JLabel("wrong password!");
        this.rc = rc;
        this.client = client;
        JPanel imPanel = (JPanel) this.getContentPane();
        // set the panel to be transparent
        imPanel.setOpaque(false);
        // read the background image
        this.background = new ImageIcon("src/main/img/background.jpg");
        this.backgroundLabel = new JLabel(this.background);
        this.backgroundLabel.setBounds(0, 0, this.getWidth(), this.getHeight());
        background.setImage(background.getImage().getScaledInstance(backgroundLabel.getWidth(
        ), backgroundLabel.getHeight(), Image.SCALE_DEFAULT));
        // add the label into the panel
        this.getLayeredPane().add(backgroundLabel, Integer.valueOf(Integer.MIN_VALUE));
        Font f3 = new Font("Arial Black", Font.BOLD, 34);
        Font f1 = new Font("Arial Black", Font.BOLD, 26);
        Font f2 = new Font("Arial Black", Font.PLAIN, 20);
        // set the header of the login page
        this.headerLabel.setFont(f3);
        this.headerLabel.setForeground(Color.white);
        this.headerLabel.setBounds(this.getWidth() / 4, this.getHeight() / 10, this.getWidth() / 2,
                60);
        this.headerLabel.setHorizontalAlignment(JLabel.CENTER);
        this.emailLabel.setFont(f1);
        this.emailLabel.setForeground(Color.white);
        this.emailLabel.setBounds(this.getWidth() / 20, this.getHeight() / 3, 175, 50);
        this.emailLabel.setHorizontalAlignment(JLabel.RIGHT);
        this.emailField.setHorizontalAlignment(JTextField.LEFT);
        this.emailField.setBounds(this.getWidth() / 2 - 150, this.getHeight() / 3, 380, 45);
        // this.emailField.setOpaque(false);
        this.emailField.setFont(f2);
        this.emailField.setForeground(Color.BLACK);
        this.psdLabel.setFont(f1);
        this.psdLabel.setForeground(Color.white);
        this.psdLabel.setBounds(this.getWidth() / 20, this.getHeight() / 3 + 100, 175, 50);
        this.psdLabel.setHorizontalAlignment(JLabel.RIGHT);
        this.checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    passwordField.setEchoChar((char) 0);
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
        this.loginButton.setBounds(355, 400, 90, 30);
        this.registerButton.setBounds(355, 480, 90, 30);
        this.loginButton.setOpaque(false);
        this.registerButton.setOpaque(false);
        this.loginButton.setForeground(new Color(255, 150, 40));
        this.registerButton.setForeground(new Color(255, 150, 40));
        this.loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    System.out.println("Login actionPerformed");
                    boolean result = false;
                    result = rc.authenticateUser(emailField.getText(),
                            String.valueOf(passwordField.getPassword()));
                    if (result == true) {
                        System.out.println("successful login");
                        System.out.println("binding");
                        Registry registry = LocateRegistry.createRegistry(1099);
                        String localhost = InetAddress.getLocalHost().getHostAddress();
                        Enumeration<NetworkInterface> interfaces =
                                NetworkInterface.getNetworkInterfaces();
                        while (interfaces.hasMoreElements()) {
                            NetworkInterface networkInterface = interfaces.nextElement();
                            Enumeration<InetAddress> addresses =
                                    networkInterface.getInetAddresses();
                            while (addresses.hasMoreElements()) {
                                InetAddress address = addresses.nextElement();
                                if ((!address.isLoopbackAddress()) && (address instanceof
                                        Inet4Address)) {
                                    localhost = address.getHostAddress();
                                }
                            }
                        }
                        String data = "rmi://" + localhost + "/" + emailField.getText();
                        registry.bind(data, client);
                        System.out.println("binding success");
                        client.changeFrame(new BidderUI(rc, data, client));
                        rc.addClient(data);
                        dispose();
                    } else {
                        System.out.println("login failed");
                        errorMessage.setVisible(true);
                    }
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                } catch (NotBoundException ex) {
                    throw new RuntimeException(ex);
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                } catch (NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                } catch (AlreadyBoundException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        this.registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == registerButton) {
                    dispose();
                    client.changeFrame(new Register(rc, client));
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
        this.getContentPane().add(this.registerButton);
        this.getContentPane().add(this.errorMessage);
        this.setVisible(true);
    }
}
