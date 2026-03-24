import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminPage extends Frame implements ActionListener {
    Button btnAddTrain, btnUpdateTrain, btnDeleteTrain, btnViewBookings;

    public AdminPage() {
        super("Admin Panel - Railway Booking System");
        setSize(400, 300);
        setLayout(new GridLayout(4, 1, 10, 10));

        btnAddTrain = new Button("Add Train");
        btnUpdateTrain = new Button("Update Train");
        btnDeleteTrain = new Button("Delete Train");
        btnViewBookings = new Button("View All Bookings");

        btnAddTrain.addActionListener(this);
        btnUpdateTrain.addActionListener(this);
        btnDeleteTrain.addActionListener(this);
        btnViewBookings.addActionListener(this);

        add(btnAddTrain);
        add(btnUpdateTrain);
        add(btnDeleteTrain);
        add(btnViewBookings);

        // Close window properly
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose(); // Close this window
                System.exit(0); // Exit app
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAddTrain) {
            new AddTrainDialog(this);
        } else if (e.getSource() == btnUpdateTrain) {
            new UpdateTrainDialog(this);
        } else if (e.getSource() == btnDeleteTrain) {
            new DeleteTrainDialog(this);
        } else if (e.getSource() == btnViewBookings) {
            new ViewBookingsDialog(this);
        }
    }

    public static void main(String[] args) {
        new AdminPage();
    }
}

// --- Dialogs for Add/Update/Delete/View ---

class AddTrainDialog extends Dialog implements ActionListener {
    TextField tfName, tfSource, tfDestination, tfSeats;
    Button btnSave;
    Label lblMsg;

    public AddTrainDialog(Frame parent) {
        super(parent, "Add Train", true);
        setSize(350, 300);
        setLayout(null);

        add(new Label("Train Name:")).setBounds(30, 40, 100, 20);
        tfName = new TextField();
        tfName.setBounds(140, 40, 160, 20);
        add(tfName);

        add(new Label("Source:")).setBounds(30, 80, 100, 20);
        tfSource = new TextField();
        tfSource.setBounds(140, 80, 160, 20);
        add(tfSource);

        add(new Label("Destination:")).setBounds(30, 120, 100, 20);
        tfDestination = new TextField();
        tfDestination.setBounds(140, 120, 160, 20);
        add(tfDestination);

        add(new Label("Seats:")).setBounds(30, 160, 100, 20);
        tfSeats = new TextField();
        tfSeats.setBounds(140, 160, 160, 20);
        add(tfSeats);

        btnSave = new Button("Save");
        btnSave.setBounds(120, 200, 100, 30);
        btnSave.addActionListener(this);
        add(btnSave);

        lblMsg = new Label("");
        lblMsg.setBounds(30, 240, 300, 20);
        add(lblMsg);

        // Close dialog properly
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = tfName.getText().trim();
        String src = tfSource.getText().trim();
        String dest = tfDestination.getText().trim();
        String seatsStr = tfSeats.getText().trim();

        if (name.isEmpty() || src.isEmpty() || dest.isEmpty() || seatsStr.isEmpty()) {
            lblMsg.setText("All fields are required.");
            return;
        }

        try {
            int seats = Integer.parseInt(seatsStr);
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(
                         "INSERT INTO trains (train_name, source, destination, seats) VALUES (?, ?, ?, ?)")) {
                pst.setString(1, name);
                pst.setString(2, src);
                pst.setString(3, dest);
                pst.setInt(4, seats);
                pst.executeUpdate();
                lblMsg.setText("Train added successfully!");
            }
        } catch (NumberFormatException ex) {
            lblMsg.setText("Seats must be a number.");
        } catch (Exception ex) {
            ex.printStackTrace();
            lblMsg.setText("DB Error: " + ex.getMessage());
        }
    }
}

// Update Train Dialog
class UpdateTrainDialog extends Dialog implements ActionListener {
    TextField tfId, tfName, tfSource, tfDestination, tfSeats;
    Button btnUpdate;
    Label lblMsg;

    public UpdateTrainDialog(Frame parent) {
        super(parent, "Update Train", true);
        setSize(400, 350);
        setLayout(null);

        add(new Label("Train ID:")).setBounds(30, 40, 100, 20);
        tfId = new TextField();
        tfId.setBounds(140, 40, 160, 20);
        add(tfId);

        add(new Label("Train Name:")).setBounds(30, 80, 100, 20);
        tfName = new TextField();
        tfName.setBounds(140, 80, 160, 20);
        add(tfName);

        add(new Label("Source:")).setBounds(30, 120, 100, 20);
        tfSource = new TextField();
        tfSource.setBounds(140, 120, 160, 20);
        add(tfSource);

        add(new Label("Destination:")).setBounds(30, 160, 100, 20);
        tfDestination = new TextField();
        tfDestination.setBounds(140, 160, 160, 20);
        add(tfDestination);

        add(new Label("Seats:")).setBounds(30, 200, 100, 20);
        tfSeats = new TextField();
        tfSeats.setBounds(140, 200, 160, 20);
        add(tfSeats);

        btnUpdate = new Button("Update");
        btnUpdate.setBounds(140, 240, 100, 30);
        btnUpdate.addActionListener(this);
        add(btnUpdate);

        lblMsg = new Label("");
        lblMsg.setBounds(30, 280, 300, 20);
        add(lblMsg);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            int id = Integer.parseInt(tfId.getText().trim());
            String name = tfName.getText().trim();
            String src = tfSource.getText().trim();
            String dest = tfDestination.getText().trim();
            int seats = Integer.parseInt(tfSeats.getText().trim());

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(
                         "UPDATE trains SET train_name=?, source=?, destination=?, seats=? WHERE id=?")) {
                pst.setString(1, name);
                pst.setString(2, src);
                pst.setString(3, dest);
                pst.setInt(4, seats);
                pst.setInt(5, id);
                int updated = pst.executeUpdate();
                if (updated > 0) lblMsg.setText("Train updated successfully!");
                else lblMsg.setText("Train ID not found.");
            }

        } catch (NumberFormatException ex) {
            lblMsg.setText("ID and Seats must be numbers.");
        } catch (Exception ex) {
            ex.printStackTrace();
            lblMsg.setText("DB Error: " + ex.getMessage());
        }
    }
}

// Delete Train Dialog
class DeleteTrainDialog extends Dialog implements ActionListener {
    TextField tfId;
    Button btnDelete;
    Label lblMsg;

    public DeleteTrainDialog(Frame parent) {
        super(parent, "Delete Train", true);
        setSize(300, 200);
        setLayout(null);

        add(new Label("Train ID:")).setBounds(30, 40, 80, 20);
        tfId = new TextField();
        tfId.setBounds(120, 40, 120, 20);
        add(tfId);

        btnDelete = new Button("Delete");
        btnDelete.setBounds(90, 80, 100, 30);
        btnDelete.addActionListener(this);
        add(btnDelete);

        lblMsg = new Label("");
        lblMsg.setBounds(30, 120, 250, 20);
        add(lblMsg);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            int id = Integer.parseInt(tfId.getText().trim());
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement("DELETE FROM trains WHERE id=?")) {
                pst.setInt(1, id);
                int deleted = pst.executeUpdate();
                lblMsg.setText(deleted > 0 ? "Train deleted successfully." : "Train ID not found.");
            }
        } catch (NumberFormatException ex) {
            lblMsg.setText("ID must be a number.");
        } catch (Exception ex) {
            ex.printStackTrace();
            lblMsg.setText("DB Error: " + ex.getMessage());
        }
    }
}

// View Bookings Dialog
class ViewBookingsDialog extends Dialog {
    public ViewBookingsDialog(Frame parent) {
        super(parent, "All Bookings", true);
        setSize(500, 400);
        setLayout(new BorderLayout());

        TextArea ta = new TextArea();
        ta.setEditable(false);
        add(ta, BorderLayout.CENTER);

        Button btnClose = new Button("Close");
        btnClose.addActionListener(e -> dispose());
        Panel p = new Panel();
        p.add(btnClose);
        add(p, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
            }
        });

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                     "SELECT b.id, u.username, t.train_name, b.seats_booked, b.pnr, b.payment_status " +
                             "FROM bookings b JOIN users u ON b.user_id = u.id " +
                             "JOIN trains t ON b.train_id = t.id")) {

            ResultSet rs = pst.executeQuery();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-5s %-15s %-20s %-10s %-15s %-10s\n",
                    "ID", "User", "Train", "Seats", "PNR", "Payment"));
            sb.append("---------------------------------------------------------------\n");

            while (rs.next()) {
                sb.append(String.format("%-5d %-15s %-20s %-10d %-15s %-10s\n",
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("train_name"),
                        rs.getInt("seats_booked"),
                        rs.getString("pnr"),
                        rs.getString("payment_status")));
            }
            ta.setText(sb.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            ta.setText("Error fetching bookings: " + ex.getMessage());
        }

        setVisible(true);
    }
}
