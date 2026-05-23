package com.system.studentmanagementsystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public class DBConnection {
    public static Connection connect() {
        Map<String, String> env = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    env.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not read local .env file. Falling back to default settings.");
        }

        try {
            String url = env.getOrDefault("DB_URL", "jdbc:postgresql://db.YOUR_FALLBACK_ID.supabase.co:5432/postgres");
            String user = env.getOrDefault("DB_USER", "postgres");
            String password = env.getOrDefault("DB_PASSWORD", "YOUR_FALLBACK_PASSWORD");

            Class.forName("org.postgresql.Driver");

            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            System.err.println("Database connection failed. Please check your credentials.");
            e.printStackTrace();
            return null;
        }
    }
}