package com.fibank.cashdesk.service;

import com.fibank.cashdesk.dto.CashBalanceDTO;
import com.fibank.cashdesk.dto.CashOperationDTO;
import com.fibank.cashdesk.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.*;
import java.time.LocalDateTime;
import java.util.Stack;


@Component
public class FileUtilsService {

    private final String cashBalancesFile;
    private final String cashOperationsFile;
    private final Gson gson;

    public FileUtilsService(
            @Value("${cashdesk.cash.balances.file}") String cashBalancesFile,
            @Value("${cashdesk.cash.operations.file}") String cashOperationsFile) {
        this.cashBalancesFile = cashBalancesFile;
        this.cashOperationsFile = cashOperationsFile;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
    }

    public void writeCashOperation(CashOperationDTO cashOperationDTO) throws IOException {
        cashOperationDTO.setLocalDateTime(LocalDateTime.now());
        try (FileWriter writer = new FileWriter(cashOperationsFile, true)) {
            gson.toJson(cashOperationDTO, writer);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            throw new IOException("Error writing to file: " + cashOperationsFile, e);
        }
    }

    public void writeCashBalance(CashBalanceDTO cashBalanceDTO) throws IOException {
        cashBalanceDTO.setLocalDateTime(LocalDateTime.now());
        try (FileWriter writer = new FileWriter(cashBalancesFile, true)) {
            gson.toJson(cashBalanceDTO, writer);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            throw new IOException("Error writing to file: " + cashBalancesFile, e);
        }
    }

    public CashBalanceDTO readLastBalance() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(cashBalancesFile, "r")) {
            long fileLength = file.length();
            if (fileLength == 0) {
                throw new IOException("File is empty: " + cashBalancesFile);
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
            throw new IOException("Error reading from file: " + cashBalancesFile, e);
        }
    }

    public boolean isCashBalanceFileEmpty() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(cashBalancesFile, "r")) {
            return file.length() == 0;
        } catch (IOException e) {
            throw new IOException("Error checking the file: " + cashBalancesFile, e);
        }
    }
}