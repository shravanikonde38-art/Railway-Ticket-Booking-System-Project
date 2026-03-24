import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class ConfirmationPage extends Frame implements ActionListener {
    // Login components
    TextField tfUser, tfPass;
    Button btnLogin;
    Label lblMsg;

    // Booking selection
    Choice bookingChoice;
    Button btnConfirm;

    int userId;
    String username;
    ArrayList<Integer> trainIds = new ArrayList<>();
    ArrayList<Integer> seatsBookedList = new ArrayList<>();
    ArrayList<String> bookingLabels = new ArrayList<>();

    public ConfirmationPage() {
        super("Ticket Confirmation - Login");
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

        btnLogin = new Button("Login");
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

    private void showConfirmationWindow() {
        setSize(400, 250);
        setLayout(null);

        remove(tfUser);
        remove(tfPass);
        remove(btnLogin);
        remove(lblMsg);

        Label lblSelect = new Label("Select Booking:");
        lblSelect.setBounds(30, 30, 200, 20);
        add(lblSelect);

        bookingChoice = new Choice();
        bookingChoice.setBounds(30, 60, 300, 25);
        add(bookingChoice);

        btnConfirm = new Button("Show Confirmation");
        btnConfirm.setBounds(120, 100, 150, 30);
        btnConfirm.addActionListener(this);
        add(btnConfirm);

        loadBookings();
        repaint();
    }

    private void loadBookings() {
        trainIds.clear();
        seatsBookedList.clear();
        bookingLabels.clear();
        bookingChoice.removeAll();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                     "SELECT b.train_id, b.seats_booked, t.train_name, t.source, t.destination " +
                             "FROM bookings b JOIN trains t ON b.train_id = t.id " +
                             "WHERE b.user_id=?")) {

            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            boolean hasBookings = false;
            while (rs.next()) {
                int trainId = rs.getInt("train_id");
                int seats = rs.getInt("seats_booked");
                String label = rs.getString("train_name") + " (" + rs.getString("source") + "→" + rs.getString("destination") + ") - Seats: " + seats;

                trainIds.add(trainId);
                seatsBookedList.add(seats);
                bookingLabels.add(label);
                bookingChoice.add(label);

                hasBookings = true;
            }

            if (!hasBookings) {
                bookingChoice.add("No bookings found");
                btnConfirm.setEnabled(false);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
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
                    userId = rs.getInt("id");
                    username = rs.getString("username");
                    remove(lblMsg);
                    showConfirmationWindow();
                } else {
                    lblMsg.setText("Invalid credentials.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                lblMsg.setText("DB error: " + ex.getMessage());
            }
        } else if (e.getSource() == btnConfirm) {
            int idx = bookingChoice.getSelectedIndex();
            if (idx < 0 || idx >= trainIds.size()) {
                showPopupScrollable("No valid booking selected.");
                return;
            }

            int trainId = trainIds.get(idx);
            int seatsBooked = seatsBookedList.get(idx);

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT * FROM trains WHERE id=?")) {
                ps.setInt(1, trainId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String message = String.format(
                            "✅ Booking Confirmed!\n\n" +
                                    "Passenger: %s\n" +
                                    "Train: %-20s\n" +
                                    "Route: %-10s → %-10s\n" +
                                    "Seats Booked: %d",
                            username,
                            rs.getString("train_name"),
                            rs.getString("source"),
                            rs.getString("destination"),
                            seatsBooked
                    );

                    showPopupScrollable(message);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                showPopupScrollable("Error fetching train info.");
            }
        }
    }

    // Scrollable popup dialog
    private void showPopupScrollable(String message) {
        Dialog d = new Dialog(this, "Booking Details", true);
        d.setSize(400, 250);
        d.setLayout(new BorderLayout());

        TextArea ta = new TextArea(message, 10, 40, TextArea.SCROLLBARS_VERTICAL_ONLY);
        ta.setEditable(false);
        d.add(ta, BorderLayout.CENTER);

        Panel p = new Panel();
        Button btnClose = new Button("OK");
        btnClose.addActionListener(ev -> d.dispose());
        p.add(btnClose);
        d.add(p, BorderLayout.SOUTH);

        d.setVisible(true);
    }

    public static void main(String[] args) {
        new ConfirmationPage();
    }
}
