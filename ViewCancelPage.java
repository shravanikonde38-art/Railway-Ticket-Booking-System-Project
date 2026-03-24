import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ViewCancelPage extends Frame implements ActionListener {
    int userId;
    String username;
    List bookingList;
    Button btnRefresh, btnCancel;

    // ✅ Normal constructor (used when coming from Login/Booking)
    public ViewCancelPage(int userId, String username) {
        super("My Bookings - " + username);
        this.userId = userId;
        this.username = username;
        initUI();
    }

    // ✅ No-arg constructor (standalone test mode)
    public ViewCancelPage() {
        super("My Bookings - TestUser");
        this.userId = 1;             // dummy test userId
        this.username = "TestUser";  // dummy username
        initUI();
    }

    private void initUI() {
        setSize(600, 420);
        setLayout(null);

        bookingList = new List();
        bookingList.setBounds(20, 40, 540, 260);
        add(bookingList);

        btnRefresh = new Button("Refresh");
        btnRefresh.setBounds(150, 320, 100, 30);
        btnRefresh.addActionListener(this);
        add(btnRefresh);

        btnCancel = new Button("Cancel Selected");
        btnCancel.setBounds(300, 320, 130, 30);
        btnCancel.addActionListener(this);
        add(btnCancel);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });

        loadBookings();
        setVisible(true);
    }

    private void loadBookings() {
        bookingList.removeAll();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                     "SELECT b.id, b.pnr, t.train_name, t.source, t.destination, b.journey_date, b.seats_booked " +
                             "FROM bookings b JOIN trains t ON b.train_id = t.id WHERE b.user_id = ?")) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String pnr = rs.getString("pnr");
                String train = rs.getString("train_name");
                String src = rs.getString("source");
                String dest = rs.getString("destination");
                String date = rs.getString("journey_date");
                int seats = rs.getInt("seats_booked");
                String line = id + " | " + pnr + " | " + train + " | " + src + "->" + dest + " | " + date + " | Seats:" + seats;
                bookingList.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRefresh) {
            loadBookings();
            return;
        }
        if (e.getSource() == btnCancel) {
            String sel = bookingList.getSelectedItem();
            if (sel == null) {
                return;
            }
            int bookingId = Integer.parseInt(sel.split("\\|")[0].trim());
            try (Connection con = DBConnection.getConnection()) {
                con.setAutoCommit(false);
                try (PreparedStatement pstGet = con.prepareStatement("SELECT train_id, seats_booked FROM bookings WHERE id=? FOR UPDATE")) {
                    pstGet.setInt(1, bookingId);
                    ResultSet rs = pstGet.executeQuery();
                    if (!rs.next()) {
                        con.rollback();
                        return;
                    }
                    int trainId = rs.getInt("train_id");
                    int seats = rs.getInt("seats_booked");

                    try (PreparedStatement pstDel = con.prepareStatement("DELETE FROM bookings WHERE id=?")) {
                        pstDel.setInt(1, bookingId);
                        pstDel.executeUpdate();
                    }

                    try (PreparedStatement pstUpd = con.prepareStatement("UPDATE trains SET seats = seats + ? WHERE id=?")) {
                        pstUpd.setInt(1, seats);
                        pstUpd.setInt(2, trainId);
                        pstUpd.executeUpdate();
                    }

                    con.commit();
                    loadBookings();
                } catch (Exception ex) {
                    con.rollback();
                    throw ex;
                } finally {
                    con.setAutoCommit(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // ✅ Main method to run standalone
    public static void main(String[] args) {
        new ViewCancelPage();  // launches with dummy TestUser
    }
}
