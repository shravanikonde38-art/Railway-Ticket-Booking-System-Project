import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPage extends Frame implements ActionListener {
    TextField tfUser, tfPass;
    Button btnLogin, btnRegister;
    Label lblMsg;

    public LoginPage() {
        super("Railway Booking - Login");
        setSize(380, 220);
        setLayout(null);

        Label l1 = new Label("Username:");
        l1.setBounds(40, 40, 80, 25);
        add(l1);

        tfUser = new TextField();
        tfUser.setBounds(130, 40, 180, 25);
        add(tfUser);

        Label l2 = new Label("Password:");
        l2.setBounds(40, 80, 80, 25);
        add(l2);

        tfPass = new TextField();
        tfPass.setEchoChar('*');
        tfPass.setBounds(130, 80, 180, 25);
        add(tfPass);

        btnLogin = new Button("Login");
        btnLogin.setBounds(40, 130, 90, 30);
        btnLogin.addActionListener(this);
        add(btnLogin);

        btnRegister = new Button("Register");
        btnRegister.setBounds(140, 130, 90, 30);
        btnRegister.addActionListener(this);
        add(btnRegister);

        lblMsg = new Label("");
        lblMsg.setBounds(40, 170, 300, 20);
        add(lblMsg);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
                System.exit(0);
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRegister) {
            new RegistrationPage();
            return;
        }

        if (e.getSource() == btnLogin) {
            String user = tfUser.getText().trim();
            String pass = tfPass.getText().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                lblMsg.setText("Enter username & password.");
                return;
            }

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(
                         "SELECT id, username FROM users WHERE username=? AND password=?")) {

                pst.setString(1, user);
                pst.setString(2, pass);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    int userId = rs.getInt("id");
                    dispose();

                    // Check if admin
                    if (user.equalsIgnoreCase("admin")) {
                        new AdminPage(); // Open admin page
                    } else {
                        new BookingPage(user, userId); // Open normal booking page
                    }

                } else {
                    lblMsg.setText("Invalid credentials.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                lblMsg.setText("DB error: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new LoginPage();
    }
}
