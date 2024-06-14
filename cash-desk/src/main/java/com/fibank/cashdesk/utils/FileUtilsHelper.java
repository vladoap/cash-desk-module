package com.fibank.cashdesk.utils;

import com.fibank.cashdesk.dto.CashBalanceDTO;
import com.fibank.cashdesk.dto.CashOperationDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.*;
import java.time.LocalDateTime;
import java.util.Stack;

public class FileUtilsHelper  {

    private static final String CASH_BALANCES_FILE = "src/main/resources/cash_balances.txt";
    private static final String CASH_OPERATIONS_FILE = "src/main/resources/cash_operations.txt";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    public static void writeCashOperation(CashOperationDTO cashOperationDTO) throws IOException {
        cashOperationDTO.setLocalDateTime(LocalDateTime.now());
        try (FileWriter writer = new FileWriter(CASH_OPERATIONS_FILE, true)) {
            gson.toJson(cashOperationDTO, writer);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            throw new IOException("Error writing to file: " + CASH_OPERATIONS_FILE, e);
        }
    }

    public static void writeCashBalance(CashBalanceDTO cashBalanceDTO) throws IOException {
        cashBalanceDTO.setLocalDateTime(LocalDateTime.now());
        try (FileWriter writer = new FileWriter(CASH_BALANCES_FILE, true)) {
            gson.toJson(cashBalanceDTO, writer);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            throw new IOException("Error writing to file: " + CASH_BALANCES_FILE, e);
        }
    }

    public static CashBalanceDTO readLastBalance() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(CASH_BALANCES_FILE, "r")) {
            long fileLength = file.length();
            if (fileLength == 0) {
                throw new IOException("File is empty: " + CASH_BALANCES_FILE);
            }

            long pointer = fileLength - 1;
            Stack<Character> stack = new Stack<>();
            boolean insideJson = false;
            StringBuilder sb = new StringBuilder();

            while (pointer >= 0) {
                file.seek(pointer);
                char c = (char) file.readByte();

                if (c == '}') {
                    stack.push(c);
                    insideJson = true;
                }

                if (insideJson) {
                    sb.append(c);
                }

                if (c == '{' && !stack.isEmpty()) {
                    stack.pop();
                    if (stack.isEmpty()) {
                        break;
                    }
                }

                pointer--;
            }

            String json = sb.reverse().toString().trim();
            return gson.fromJson(json, CashBalanceDTO.class);
        } catch (JsonSyntaxException e) {
            throw new IOException("Error reading from file: " + CASH_BALANCES_FILE, e);
        }
    }

    public static boolean isCashBalanceFileEmpty() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(CASH_BALANCES_FILE, "r")) {
            return file.length() == 0;
        } catch (IOException e) {
            throw new IOException("Error checking the file: " + CASH_BALANCES_FILE, e);
        }
    }


}