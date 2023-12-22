import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.KeySpec;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Scanner;

public class Utility {
    private static final String ENCRYPTED_FILE_NAME = "bankDatabase.ser";
    private static final Scanner input = new Scanner(System.in);

    public static DynamicList<String> readFileLines(String fileName) {
        DynamicList<String> lines = new DynamicList<>("'" + fileName + "' Lines");

        try {
            File file = new File(fileName);
            Scanner fileReader = new Scanner(file);

            while (fileReader.hasNextLine()) {
                lines.add(fileReader.nextLine());
            }

            fileReader.close();
        } catch (FileNotFoundException e) {
            printError("File could not be found: " + e.getMessage());
        }

        return lines;
    }

    // FORMATTERS
    public static String formatMoney(double amount) {
        DecimalFormat decimalFormat = new DecimalFormat("£#,##0.00");
        return decimalFormat.format(amount);
    }

    public static String formatPossession(String item) {
        if (item.endsWith("s")) return item + "'";
        return item + "'s";
    }

    public static String formatPlural(String item, int amount) {
        if (amount == 1) return item;
        return item + "s";
    }

    public static String formatPercent(double percent) {
        return (percent * 100) + "%";
    }

    public static String formatId(String id) {
        return "UUID-" + id;
    }

    public static String formatDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("HH:mm d/MM/yyyy"));
    }

    public static String formatDuration(Duration duration) {
        long minutes = duration.toMinutes();
        long seconds = duration.toSeconds() % 60;

        return (minutes != 0 ? minutes + " minutes, " : "") + (seconds + " seconds");
    }

    // INPUTS
    public static double getDoubleInput(String output) {
        double returnDouble;

        Utility.print(output + " - ", false);
        String choice = input.nextLine();

        if (choice.matches("^(?!.*[.].*[.])(?!.*[^[0-9].]).*[0-9].*$")) {
            returnDouble = Double.parseDouble(choice);
            if (returnDouble >= 0) {
                return returnDouble;
            }
        }
        return getDoubleInput(output);
    }

    public static int getIntInput(String output, DynamicList<?> options) {
        int returnInt;

        options.display(1, true);
        Utility.print(output + " [" + 1 + "-" + options.size() + "] - ", false);
        String choice = input.nextLine();

        if (choice.matches("[0-9]+")) {
            returnInt = Integer.parseInt(choice);
            if (returnInt >= 1 && returnInt <= options.size()) {
                return returnInt - 1;
            }
        }
        return getIntInput(output, options);
    }

    public static String getInput(String output) {
        Utility.print(output + " - ", false);
        return input.nextLine();
    }

    public static char[] getPassword(String output) {
        try {
            // Will only work when running securely in TERMINAL
            return System.console().readPassword(output + " - ");
        } catch (Exception e) {
            // For IntelliJ
            return getInput(output).toCharArray();
        }
    }

    // RANDOM
    public static int randomIndex(DynamicList<?> list) {
        return new Random().nextInt(list.size());
    }

    public static double randomRange(int min, int max) {
        return new Random().nextDouble(min, max);
    }

    public static int randomOfLength(int length) {
        int minBound = (int) Math.pow(10, length - 1);
        int maxBound = (int) Math.pow(10, length) - 1;
        return new Random().nextInt(maxBound - minBound + 1) + minBound;
    }

    // PRINTERS
    public static void print(Object obj) {
        print(obj, true);
    }

    public static void print(Object obj, Boolean newLine) {
        if (newLine) System.out.println(obj);
        else System.out.print(obj);
    }

    public static void printLog(String template, Object... args) {
        int placeholderCount = countPlaceholders(template);
        int argCount = args.length;

        if (placeholderCount != argCount) {
            throw new IllegalArgumentException("Number of placeholders does not match the number of arguments.");
        }

        for (Object arg : args) {
            template = template.replaceFirst("%d", arg.toString());
        }

        print("\u001B[34;1m" + template + "\033[0m");
    }

    private static int countPlaceholders(String template) {
        int count = 0;
        int index = template.indexOf("%d");

        while (index != -1) {
            count++;
            index = template.indexOf("%d", index + 2);
        }

        return count;
    }

    public static void printError(Object obj) {
        print("\033[31m" + obj + "\033[0m");
    }

    public static void printSuccess(Object obj) {
        print("\n=== \033[1;32m" + obj + "\033[0m ===\n");
    }

    public static void printComment(Object obj) {
        print("\033[90m # " + obj + "\033[0m");
    }

    public static void printTitle(Object obj) {
        print("\n=== \033[97m" + obj + "\033[0m ===");
    }

    public static void printChapter(Object obj) {
        print("\033[97m" + obj + "\033[0m");
    }

    // TIME
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static Duration duration(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return Duration.between(startDateTime, endDateTime);
    }

    // CRYPTOGRAPHY
    private static SecretKey generateKey(String password) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), "salt".getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (Exception e) {
            printError(e.getMessage());
        }
        return null;
    }

    // Encrypt serialized object using AES and write to "bankDatabase.ser"
    public static void encryptAndWriteObject(Serializable object, String password) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(object);
            oos.flush();

            SecretKey secretKey = generateKey(password);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] serializedBytes = baos.toByteArray();
            byte[] encryptedBytes = cipher.doFinal(serializedBytes);

            Files.write(Paths.get(ENCRYPTED_FILE_NAME), encryptedBytes);
        } catch (Exception e) {
            printError(e.getMessage());
        }
    }

    // Read  from "bankDatabase.ser" and decrypt serialized object
    public static BankDatabase readAndDecryptObject(String password) {
        try {
            SecretKey secretKey = generateKey(password);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] encryptedBytes = Files.readAllBytes(Paths.get(ENCRYPTED_FILE_NAME));
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            try (ByteArrayInputStream bais = new ByteArrayInputStream(decryptedBytes);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {

                return (BankDatabase) ois.readObject();
            }
        } catch (Exception e) {
            printError(e.getMessage());
        }
        return null;
    }
}
