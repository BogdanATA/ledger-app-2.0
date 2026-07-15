package com.pluralsight;

import java.time.LocalDate;
import java.time.LocalTime;

public class Transaction {
    private static int nextId = 1000;

    private final int id;
    private LocalDate date;
    private LocalTime time;
    private String description;
    private String vendor;
    private double amount;
    private CategoryType category;

    public Transaction(LocalDate date, LocalTime time, String description, String vendor, double amount, CategoryType category) {
        this.id = nextId++;
        this.date = date;
        this.time = time;
        this.description = description;
        this.vendor = vendor;
        this.amount = amount;
        this.category = category;
    }

    /**
     * Used when loading transactions that already have an id (e.g. from file),
     * so a persisted id is reused instead of generating a new one.
     */
    public Transaction(int id, LocalDate date, LocalTime time, String description, String vendor, double amount, CategoryType category) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.description = description;
        this.vendor = vendor;
        this.amount = amount;
        this.category = category;
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public String getVendor() {
        return vendor;
    }

    public double getAmount() {
        return amount;
    }

    public CategoryType getCategory() {
        return category;
    }
    public void setCategory(CategoryType category) {
        this.category = category;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
