package utility;

import model.exception.InputInterruptedRuntimeException;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class ScannerUtility {

    private static final Scanner scanner = new Scanner(System.in);

    public static String getFirstNChar(int n) {
        String nextLine;
        try {
            nextLine = scanner.nextLine();
        } catch (NoSuchElementException e) {
            throw new InputInterruptedRuntimeException();
        }
        return nextLine.substring(0, Math.min(nextLine.length(), n));
    }

    public static String getFirstChar() {
        return getFirstNChar(1);
    }

    public static String askFirstChar(String ask) {
        String resultString;
        do {
            System.out.printf("%s -> ", ask);
            resultString = getFirstChar();
        } while (resultString.length() < 1);
        return resultString;
    }

    public static String getString() {
        try {
            String returnString = scanner.nextLine();
            return returnString.split("[ \\t]")[0];
        } catch (NoSuchElementException e) {
            throw new InputInterruptedRuntimeException();
        }
    }

    public static String getText() {
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            throw new InputInterruptedRuntimeException();
        }
    }

    public static String askString(String ask, int limit) {
        String resultString;
        do {
            System.out.printf("%s -> ", ask);
            resultString = getString();
            if (resultString.length() > limit)
                System.out.printf("Stringa inserita troppo lunga, lunghezza massima %d.\n", limit);
        } while (resultString.length() < 1 || resultString.length() > limit);
        return resultString;
    }

    public static String askText(String ask, int limit) {
        String resultString;
        do {
            System.out.printf("%s -> ", ask);
            resultString = getText();
            if (resultString.length() > limit)
                System.out.printf("Stringa inserita troppo lunga, lunghezza massima %d.\n", limit);
        } while (resultString.length() < 1 || resultString.length() > limit);
        return resultString;
    }

}
