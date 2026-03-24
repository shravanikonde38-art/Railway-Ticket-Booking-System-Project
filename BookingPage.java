import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.UUID;

public class BookingPage extends Frame implements ActionListener {
    int userId;
    String username;
    Label lblWelcome, lblSeats, lblDate, lblMsg;
    TextField tfSeats, tfDate;
    Button btnBook;
    Choice trainChoice;

    // Constructor used when opening from LoginPage (real flow)
    public BookingPage(String username, int userId) {
        super("Railway Booking - Ticket Booking");
        this.username = username;
        this.userId = userId;
        initUI();
    }

    // No-arg constructor for running independently (testing/demo)
    public BookingPage() {
        super("Railway Booking - Ticket Booking (Test Mode)");
        this.username = "GuestUser";   // default dummy username
        this.userId = 1;               // default dummy id
        initUI();
    }

    private void initUI() {
        setSize(500, 350);
        setLayout(null);

        // Welcome message
        lblWelcome = new Label("Welcome, " + username);
        lblWelcome.setBounds(30, 30, 300, 20);
        add(lblWelcome);

        // Train selection
        Label lblTrain = new Label("Select Train:");
        lblTrain.setBounds(30, 70, 80, 20);
        add(lblTrain);

        trainChoice = new Choice();
        trainChoice.setBounds(120, 70, 300, 20);
        add(trainChoice);

        // Load trains from DB
        loadTrains();

        // Seats booked
        lblSeats = new Label("Seats:");
        lblSeats.setBounds(30, 110, 80, 20);
        add(lblSeats);

        tfSeats = new TextField();
        tfSeats.setBounds(120, 110, 80, 20);
        add(tfSeats);

        // Journey date
        lblDate = new Label("Journey Date (YYYY-MM-DD):");
        lblDate.setBounds(30, 150, 180, 20);
        add(lblDate);

        tfDate = new TextField();
        tfDate.setBounds(220, 150, 120, 20);
        add(tfDate);

        // Book button
        btnBook = new Button("Book Ticket");
        btnBook.setBounds(120, 200, 100, 30);
        btnBook.addActionListener(this);
        add(btnBook);

        // Message label
        lblMsg = new Label("");
        lblMsg.setBounds(30, 250, 450, 20);
        add(lblMsg);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private void loadTrains() {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, train_name, source, destination FROM trains")) {

            boolean hasTrains = false;

            while (rs.next()) {
                int id = rs.getInt("id");
                String trainName = rs.getString("train_name");
                String src = rs.getString("source");
                String dest = rs.getString("destination");
                trainChoice.add(id + " - " + trainName + " (" + src + " → " + dest + ")");
                hasTrains = true;
            }

            if (!hasTrains) {
                trainChoice.add("No trains available");
            }

        } catch (Exception e) {
            e.printStackTrace();
            trainChoice.add("Error loading trains");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnBook) {
            String selected = trainChoice.getSelectedItem();
            if (selected == null || selected.equals("No trains available") || selected.equals("Error loading trains")) {
                lblMsg.setText("No valid train selected.");
                return;
            }

            int trainId = Integer.parseInt(selected.split(" - ")[0]);
            String seatsStr = tfSeats.getText().trim();
            String journeyDate = tfDate.getText().trim();

            if (seatsStr.isEmpty() || journeyDate.isEmpty()) {
                lblMsg.setText("Please fill all fields.");
                return;
            }

            try {
                int seatsBooked = Integer.parseInt(seatsStr);
                String pnr = "PNR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                try (Connection con = DBConnection.getConnection();
                     PreparedStatement pst = con.prepareStatement(
                             "INSERT INTO bookings(user_id, train_id, seats_booked, journey_date, pnr, booking_date) " +
                                     "VALUES (?, ?, ?, ?, ?, NOW())")) {

                    pst.setInt(1, userId);
                    pst.setInt(2, trainId);
                    pst.setInt(3, seatsBooked);
                    pst.setString(4, journeyDate);
                    pst.setString(5, pnr);

                    int rows = pst.executeUpdate();
                    if (rows > 0) {
                        lblMsg.setText("Booking successful! Redirecting to View Bookings...");
                        dispose();
                        new ViewBookingsPage();
                    } else {
                        lblMsg.setText("Booking failed.");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                lblMsg.setText("DB Error: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new BookingPage();  // runs in standalone mode with dummy user
    }
}
