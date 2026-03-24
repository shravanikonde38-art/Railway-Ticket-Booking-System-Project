import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ViewBookingsPage extends Frame implements ActionListener {
    TextField tfUser, tfPass;
    Button btnLogin;
    Label lblMsg;
    int userId;
    String username;
    TextArea taBookings;
    Button btnClose;

    // Constructor opens login window
    public ViewBookingsPage() {
        super("View My Bookings - Login");
        setSize(350, 220);
        setLayout(null);

        Label l1 = new Label("Username:");
        l1.setBounds(30, 40, 80, 25);
        add(l1);

        tfUser = new TextField();
        tfUser.setBounds(120, 40, 180, 25);
        add(tfUser);

        Label l2 = new Label("Password:");
        l2.setBounds(30, 80, 80, 25);
        add(l2);

        tfPass = new TextField();
        tfPass.setEchoChar('*');
        tfPass.setBounds(120, 80, 180, 25);
        add(tfPass);

        btnLogin = new Button("View Bookings");
        btnLogin.setBounds(120, 130, 120, 30);
        btnLogin.addActionListener(this);
        add(btnLogin);

        lblMsg = new Label("");
        lblMsg.setBounds(30, 170, 300, 20);
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
        username = tfUser.getText().trim();
        String pass = tfPass.getText().trim();

        if (username.isEmpty() || pass.isEmpty()) {
            lblMsg.setText("Enter username & password.");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                     "SELECT id, username FROM users WHERE username=? AND password=?")) {

            pst.setString(1, username);
            pst.setString(2, pass);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                userId = rs.getInt("id");
                dispose(); // close login window
                openBookingsWindow();
            } else {
                lblMsg.setText("Invalid credentials.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            lblMsg.setText("DB error: " + ex.getMessage());
        }
    }

    private void openBookingsWindow() {
        Frame bookingFrame = new Frame("My Bookings - " + username);
        bookingFrame.setSize(600, 400);
        bookingFrame.setLayout(null);

        Label lblTitle = new Label("Bookings for " + username);
        lblTitle.setBounds(20, 30, 400, 20);
        bookingFrame.add(lblTitle);

        taBookings = new TextArea();
        taBookings.setBounds(20, 60, 550, 250);
        taBookings.setEditable(false);
        bookingFrame.add(taBookings);

        btnClose = new Button("Close");
        btnClose.setBounds(250, 320, 80, 30);
        bookingFrame.add(btnClose);

        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                bookingFrame.dispose();
            }
        });

        bookingFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                bookingFrame.dispose();
            }
        });

        loadBookings();
        bookingFrame.setVisible(true);
    }

    private void loadBookings() {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                     "SELECT b.pnr, b.seats_booked, b.journey_date, b.booking_date, " +
                             "t.train_name, t.source, t.destination " +
                             "FROM bookings b JOIN trains t ON b.train_id = t.id " +
                             "WHERE b.user_id = ?")) {

            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-12s %-20s %-15s %-15s %-10s %-12s\n",
                    "PNR", "Train", "Source", "Destination", "Seats", "Journey Date"));
            sb.append("---------------------------------------------------------------------\n");

            boolean hasBookings = false;
            while (rs.next()) {
                hasBookings = true;
                sb.append(String.format("%-12s %-20s %-15s %-15s %-10d %-12s\n",
                        rs.getString("pnr"),
                        rs.getString("train_name"),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getInt("seats_booked"),
                        rs.getString("journey_date")));
            }

            if (!hasBookings) {
                sb.append("No bookings found.");
            }

            taBookings.setText(sb.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            taBookings.setText("Error loading bookings: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new ViewBookingsPage();
    }
}
