package util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleInputHandler {

    public static String getStringFromUser(String promptMessage) {
        String input = readFromBufferedReader(promptMessage);
        return input;
    }

    // todo: need to handle empty string/char
    public static char getCharFromUser(String promptMessage) {
        String input = readFromBufferedReader(promptMessage);
        return input.charAt(0);
    }

    public static int getIntFromUser(String promptMessage) {
        String input = readFromBufferedReader(promptMessage);
        return Integer.parseInt(input);
    }

    private static String readFromBufferedReader(String promptMessage) {
        prompt(promptMessage);
        String line = "";
        try {
            BufferedReader commandReader = new BufferedReader(new InputStreamReader(System.in));
            line = commandReader.readLine();
        } catch (Exception io) {
            io.printStackTrace();
        }
        return line;
    }

    private static void prompt(String promptMessage) {
        System.out.print(promptMessage + ">> ");
    }


}