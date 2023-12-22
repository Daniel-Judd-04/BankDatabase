import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;


public class Password implements Serializable {
    private final byte[] salt;
    private final byte[] hashedPassword;
    private int attempts;
    private final int maxAttempts;

    public Password(char[] password, int maxAttempts) {
        this.salt = generateSalt();
        this.hashedPassword = hashPassword(password, salt);
        this.attempts = 0;
        this.maxAttempts = maxAttempts;

        Arrays.fill(password, '\0');
    }

    // Generate a random salt
    private byte[] generateSalt() {
        // in bytes
        int SALT_LENGTH = 32;
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;
    }

    // Hash a password with salt and key stretching
    private static byte[] hashPassword(char[] password, byte[] salt) {
        Utility.print("Hashing Password...", false);
        int iterations = 1000000; // Around 0.3s
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Add salt
            md.update(salt);

            // Add password bytes
            for (char c : password) md.update((byte) c);

            // Perform key stretching
            for (int i = 0; i < iterations; i++) md.update(md.digest());

            // Clear sensitive data
            Arrays.fill(password, '\0');

            Utility.print("[DONE]");
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            Utility.printError(e.getMessage());
            return null;
        }
    }

    // Verify a password against its hashed version
    public boolean verifyPassword(char[] password) {
        if (checkAttempts()) {
            boolean valid = MessageDigest.isEqual(hashPassword(password, salt), hashedPassword);
            if (valid) attempts = 0;
            else attempts++;
            Arrays.fill(password, '\0');
            return valid;
        }
        return false;
    }

    public boolean checkAttempts() {
        return attempts < maxAttempts;
    }

    public void resetAttempts() {
        attempts = 0;
    }

//    // Helper method to convert bytes to hexadecimal string
//    private static String byteToHex(byte[] hashedPassword) {
//        StringBuilder hexString = new StringBuilder();
//        for (byte b : hashedPassword) {
//            String hex = Integer.toHexString(0xff & b);
//            if (hex.length() == 1) hexString.append('0');
//            hexString.append(hex);
//        }
//        return hexString.toString();
//    }
//
//    @Override
//    public String toString() {
//        return byteToHex(hashedPassword);
//    }
}
