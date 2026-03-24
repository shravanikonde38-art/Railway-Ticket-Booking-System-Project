import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegistrationPage extends Frame implements ActionListener {
    TextField tfName, tfUser, tfPass, tfEmail, tfPhone;
    Button btnSubmit, btnBack;
    Label lblMsg;

    public RegistrationPage() {
        super("Register - Railway Booking");
        setSize(420, 360);
        setLayout(null);

        addLabel("Full Name:", 30, 50);
        tfName = addTextField(120, 50);

        addLabel("Username:", 30, 90);
        tfUser = addTextField(120, 90);

        addLabel("Password:", 30, 130);
        tfPass = addTextField(120, 130);
        tfPass.setEchoChar('*');

        addLabel("Email:", 30, 170);
        tfEmail = addTextField(120, 170);

        addLabel("Phone:", 30, 210);
        tfPhone = addTextField(120, 210);

        btnSubmit = new Button("Register");
        btnSubmit.setBounds(80, 260, 120, 30);
        btnSubmit.addActionListener(this);
        add(btnSubmit);

        btnBack = new Button("Back to Login");
        btnBack.setBounds(220, 260, 120, 30);
        btnBack.addActionListener(this);
        add(btnBack);

        lblMsg = new Label("");
        lblMsg.setBounds(30, 300, 350, 25);
        add(lblMsg);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

    private void addLabel(String text, int x, int y) {
        Label l = new Label(text);
        l.setBounds(x, y, 80, 25);
        add(l);
    }

    private TextField addTextField(int x, int y) {
        TextField tf = new TextField();
        tf.setBounds(x, y, 240, 25);
        add(tf);
        return tf;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnBack) {
            dispose();
            return;
        }
        // Register
        String name = tfName.getText().trim();
        String username = tfUser.getText().trim();
        String password = tfPass.getText().trim();
        String email = tfEmail.getText().trim();
        String phone = tfPhone.getText().trim();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            lblMsg.setText("Name, username and password are required.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            // Check existing username
            PreparedStatement check = con.prepareStatement("SELECT id FROM users WHERE username=?");
            check.setString(1, username);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                lblMsg.setText("Username already exists.");
                return;
            }

            PreparedStatement insert = con.prepareStatement(
                    "INSERT INTO users(name, username, password, email, phone) VALUES(?,?,?,?,?)");
            insert.setString(1, name);
            insert.setString(2, username);
            insert.setString(3, password);
            insert.setString(4, email);
            insert.setString(5, phone);
            insert.executeUpdate();

            lblMsg.setText("Registration successful. You can login now.");
        } catch (Exception ex) {
            ex.printStackTrace();
            lblMsg.setText("Error: " + ex.getMessage());
        }
    }
}
