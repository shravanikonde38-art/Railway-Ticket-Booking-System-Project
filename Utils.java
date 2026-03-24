import java.util.UUID;

public class Utils {
    // Generate a short PNR-like id
    public static String generatePNR() {
        return "PNR" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
