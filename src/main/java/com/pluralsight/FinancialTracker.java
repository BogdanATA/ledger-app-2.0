package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;


public class FinancialTracker {

    /* ------------------------------------------------------------------
       Shared data and formatters
       ------------------------------------------------------------------ */
    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    private static final String FILE_NAME = "transactions.csv";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    /* ------------------------------------------------------------------
       Main menu
       ------------------------------------------------------------------ */
    public static void main(String[] args) {
        loadTransactions(FILE_NAME);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n💰 Welcome to TransactionApp 💰");
            System.out.println("=================================");
            System.out.println("Choose an option:");
            System.out.println("💵  (D) Add Deposit");
            System.out.println("💳  (P) Make Payment (Debit)");
            System.out.println("📒  (L) Ledger");
            System.out.println("E) Edit Transaction");
            System.out.println("🚪  (X) Exit");
            System.out.println("=================================");


            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addDeposit(scanner);
                case "P" -> addPayment(scanner);
                case "L" -> ledgerMenu(scanner);
                case "E" -> editDeleteMenu(scanner);
                case "X" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
        scanner.close();
    }

    /* ------------------------------------------------------------------
       File I/O
       ------------------------------------------------------------------ */
    /**
     * Loads transactions from file and adds them to array list.
     *
     * @param fileName FILE_NAME is passed down into this parameter
     */
    public static void loadTransactions(String fileName) {
        try {
            File file = new File(fileName); // stores file inside new File object

            // create file if it doesnt exist
            if (!file.exists()){
                file.createNewFile();
            }
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;

            while ((line = br.readLine()) != null) {    // while line isnt null keep reading
                String[] tokens = line.split("\\|"); // split line each time it reads '|'

                if (tokens.length != 7) continue; // if line inside file doesnt have exactly 7 tokens skip it

                try {
                    int id = Integer.parseInt(tokens[0]);
                    LocalDate date = LocalDate.parse(tokens[1], DATE_FMT);
                    LocalTime time = LocalTime.parse(tokens[2], TIME_FMT);
                    String description = tokens[3];
                    String vendor = tokens[4];
                    double amount = Double.parseDouble(tokens[5]);
                    CategoryType category = CategoryType.valueOf(tokens[6]);

                    // creates transaction object using parsed data from file, reusing its persisted id
                    Transaction transaction = new Transaction(id, date, time, description, vendor, amount, category);

                    transactions.add(transaction); // adds new transaction object into the transactions array list
                } catch (Exception e) {
                    System.err.println("Bad line:" + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file" + fileName);
        }
    }

    /* ------------------------------------------------------------------
       Add new transactions
       ------------------------------------------------------------------ */
    /**
     * Adds deposit and saves it to the file.
     *
     * @param scanner used to read user input
     */
    private static void addDeposit(Scanner scanner) {
        // date + time user input and parse
        try {
            System.out.print("📅 Enter date and time (yyyy-MM-dd HH:mm:ss): ");
            String dateTimeInput = scanner.nextLine().trim();

            LocalDateTime dateTime = LocalDateTime.parse(dateTimeInput, DATETIME_FMT);
            LocalDate date = dateTime.toLocalDate();
            LocalTime time = dateTime.toLocalTime();

            // description
            System.out.print("📝 Enter description: ");
            String description = scanner.nextLine().trim();

            // vendor
            System.out.print("🏪 Enter vendor: ");
            String vendor = scanner.nextLine().trim();

            // amount
            System.out.print("💵 Enter amount: $");
            double amount = Double.parseDouble(scanner.nextLine().trim());

            // make sure amount is positive
            if (amount <= 0) {
                System.out.println("⚠️ Deposit amount must be positive.");
                return;
            }

            // creates a new category for the helper method
            CategoryType category = selectCategory(scanner);

            // creates transaction object using parsed data from file
            Transaction transaction = new Transaction(date, time, description, vendor, amount, category);

            transactions.add(transaction); // adds new transaction object into the transactions array list

            saveTransaction(transaction); // saves changes to the file

            System.out.println("✅ Deposit added successfully!");
        } catch (Exception e) {
            System.out.println("❌ Invalid input. Deposit not added.");
        }
    }

    /**
     * Adds payment and saves it to the file.
     *
     * @param scanner used to read user input
     */
    private static void addPayment(Scanner scanner) {
        try {
            System.out.print("📅 Enter date and time (yyyy-MM-dd HH:mm:ss): ");
            String dateTimeInput = scanner.nextLine().trim();

            LocalDateTime dateTime = LocalDateTime.parse(dateTimeInput, DATETIME_FMT);
            LocalDate date = dateTime.toLocalDate();
            LocalTime time = dateTime.toLocalTime();

            // description
            System.out.print("📝 Enter description: ");
            String description = scanner.nextLine().trim();

            // vendor
            System.out.print("🏪 Enter vendor: ");
            String vendor = scanner.nextLine().trim();

            // amount
            System.out.print("💵 Enter amount: $");
            double amount = Double.parseDouble(scanner.nextLine().trim());

            // make sure amount is positive
            if (amount <= 0) {
                System.out.println("⚠️ Payment amount must be positive.");
                return;
            }
            //negate the payment entered
            double negatedAmount = amount * -1;

            // creates a new category for the helper method
            CategoryType category = selectCategory(scanner);

            // creates transaction object using parsed data from file
            Transaction transaction = new Transaction(date, time, description, vendor, negatedAmount, category);

            transactions.add(transaction); // adds new transaction object into the transactions array list

            saveTransaction(transaction); // saves changes to the file

            System.out.println("✅ Payment added successfully!");
        } catch (Exception e) {
            System.out.println("❌ Invalid input. Payment not added.");
        }
    }

    /**
     * Appends transaction to transaction file.
     *
     * @param transaction takes transaction object and writes it to file
     * */
    private static void saveTransaction(Transaction transaction) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            // appends new transaction line to the end of the file
            writeTransactionLine(bw, transaction);
        } catch (IOException e) {
            System.out.println("⚠️ Error saving transaction");
        }
    }

    /**
     * Rewrites the whole file from the in-memory transactions list.
     * Needed after an edit or delete, since those change/remove a line
     * that isn't necessarily the last one in the file.
     * */
    private static void rewriteTransactionsFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, false))) {
            for (Transaction transaction : transactions) {
                writeTransactionLine(bw, transaction);
            }
        } catch (IOException e) {
            System.out.println("Error saving transactions");
        }
    }

    /**
     * Writes a single transaction as a line in the CSV format.
     *
     * @param bw the writer to write to
     * @param transaction the transaction to serialize
     * */
    private static void writeTransactionLine(BufferedWriter bw, Transaction transaction) throws IOException {
        bw.write(transaction.getId() +
                "|" + transaction.getDate().format(DATE_FMT) +
                "|" + transaction.getTime().format(TIME_FMT) +
                "|" + transaction.getDescription() +
                "|" + transaction.getVendor() +
                "|" + String.format("%.2f", transaction.getAmount()) +
                "|" + transaction.getCategory());
        bw.newLine();
    }

    /* ------------------------------------------------------------------
       Ledger menu
       ------------------------------------------------------------------ */
    /**
     * Displays the ledger menu and lets the user navigate it.
     *
     * @param scanner Used to read user navigation commands
     * */
    private static void ledgerMenu(Scanner scanner) {
        // sort transactions so they print newest at top
        transactions.sort(Comparator.comparing(Transaction::getDate)
                .thenComparing(Transaction::getTime).reversed());

        boolean running = true;
        while (running) {
            System.out.println("\n📒 Ledger");
            System.out.println("=================================");
            System.out.println("Choose an option:");
            System.out.println("📋  (A) All");
            System.out.println("💵  (D) Deposits");
            System.out.println("💳  (P) Payments");
            System.out.println("📊  (R) Reports");
            System.out.println("🏠  (H) Home");
            System.out.println("=================================");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "A" -> displayLedger();
                case "D" -> displayDeposits();
                case "P" -> displayPayments();
                case "R" -> reportsMenu(scanner);
                case "H" -> running = false;
                default -> System.out.println("⚠️ Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Edit / Delete menu
       ------------------------------------------------------------------ */
    /**
     * Displays the edit/delete menu and lets the user navigate it.
     *
     * @param scanner Used to read user navigation commands
     * */
    private static void editDeleteMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n✏️ Edit/Delete Transaction");
            System.out.println("=================================");
            System.out.println("Choose an option:");
            System.out.println("📝  (E) Edit");
            System.out.println("🗑️  (D) Delete");
            System.out.println("🏠  (H) Home");
            System.out.println("=================================");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "E" -> editTransaction(scanner);
                case "D" -> deleteTransaction(scanner);
                case "H" -> running = false;
                default -> System.out.println("⚠️ Invalid option");
            }
        }
    }

    /**
     * Shows the ledger, asks for a transaction id, then walks through each
     * field letting the user keep the current value (Enter) or replace it.
     *
     * @param scanner Used to read user input
     * */
    private static void editTransaction(Scanner scanner) {
        displayLedger();

        System.out.print("\n🔎 Enter ID to edit: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        Transaction transaction = findTransactionById(id);
        if (transaction == null) {
            System.out.println("❌ No transaction found with ID " + id);
            return;
        }

        System.out.println("✏️ Editing transaction " + id + ". Press Enter to keep the current value.");

        System.out.print("📅 Date and time (yyyy-MM-dd HH:mm:ss) [" +
                transaction.getDate().format(DATE_FMT) + " " + transaction.getTime().format(TIME_FMT) + "]: ");
        String dateTimeInput = scanner.nextLine().trim();
        if (!dateTimeInput.isBlank()) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(dateTimeInput, DATETIME_FMT);
                transaction.setDate(dateTime.toLocalDate());
                transaction.setTime(dateTime.toLocalTime());
            } catch (Exception e) {
                System.out.println("⚠️ Invalid date/time, keeping current value.");
            }
        }

        System.out.print("📝 Description [" + transaction.getDescription() + "]: ");
        String description = scanner.nextLine().trim();
        if (!description.isBlank()) transaction.setDescription(description);

        System.out.print("🏪 Vendor [" + transaction.getVendor() + "]: ");
        String vendor = scanner.nextLine().trim();
        if (!vendor.isBlank()) transaction.setVendor(vendor);

        System.out.print("💵 Amount [" + String.format("%.2f", transaction.getAmount()) + "]: ");
        String amountInput = scanner.nextLine().trim();
        if (!amountInput.isBlank()) {
            try {
                transaction.setAmount(Double.parseDouble(amountInput));
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid amount, keeping current value.");
            }
        }

        transaction.setCategory(editCategory(scanner, transaction.getCategory()));

        rewriteTransactionsFile();
        System.out.println("✅ Transaction updated successfully.");
    }

    /**
     * Shows the ledger, asks for a transaction id, confirms with the user,
     * then removes the matching transaction.
     *
     * @param scanner Used to read user input
     * */
    private static void deleteTransaction(Scanner scanner) {
        displayLedger();

        System.out.print("\n🔎 Enter ID to delete: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Invalid ID.");
            return;
        }

        Transaction transaction = findTransactionById(id);
        if (transaction == null) {
            System.out.println("❌ No transaction found with ID " + id);
            return;
        }

        printLedgerHeader();
        printTransaction(transaction);

        System.out.print("🗑️ Are you sure you want to delete this transaction? (Y/N): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("Y")) {
            transactions.remove(transaction);
            rewriteTransactionsFile();
            System.out.println("✅ Transaction deleted successfully.");
        } else {
            System.out.println("🚫 Delete cancelled.");
        }
    }

    /**
     * Finds a transaction by its id.
     *
     * @param id the id to search for
     * @return the matching Transaction, or null if none found
     * */
    private static Transaction findTransactionById(int id) {
        for (Transaction transaction : transactions) {
            if (transaction.getId() == id) return transaction;
        }
        return null;
    }

    /**
     * prompts for a new category, or keeps the current one if left blank
     *
     * @param scanner Used to read the user given category type
     * @param current the transaction's current category, returned if the user presses Enter
     * @return the CategoryType the user selected, or current if left blank
     * */
    private static CategoryType editCategory(Scanner scanner, CategoryType current) {
        System.out.println("🍔 (1) Food");
        System.out.println("⛽ (2) Gas");
        System.out.println("🎬 (3) Entertainment");
        System.out.println("📦 (4) Other");
        System.out.print("👉 Choose new category [" + current + "], or press Enter to keep current: ");

        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isBlank()) return current;

            switch (input) {
                case "1": return CategoryType.FOOD;
                case "2": return CategoryType.GAS;
                case "3": return CategoryType.ENTERTAINMENT;
                case "4": return CategoryType.OTHER;
                default: System.out.print("⚠️ Invalid option. Choose 1-4 or press Enter to keep current: ");
            }
        }
    }

    /* ------------------------------------------------------------------
       Display helpers: show data in neat columns
       ------------------------------------------------------------------ */
    /**
     * Display all transactions from the array list.
     * */
    private static void displayLedger() {
        printLedgerHeader();
        for (Transaction transaction : transactions) {
            printTransaction(transaction);
        }
    }

    /**
     * Display only the transactions with positive amounts.
     * */
    private static void displayDeposits() {
        printLedgerHeader();
        for (Transaction deposit : transactions) {
            if (deposit.getAmount() > 0){
                printTransaction(deposit);
            }
        }
    }

    /**
     * Display only the transactions with negative amounts.
     * */
    private static void displayPayments() {
        printLedgerHeader();
        for (Transaction payment : transactions) {
            if (payment.getAmount() < 0){
                printTransaction(payment);
            }
        }
    }

    /**
     * Prints formatted header for the ledger categories.
     * */
    private static void printLedgerHeader () {
        System.out.printf("%-6s %-12s %-10s %-35s %-20s %-12s %s%n", "ID", "Date", "Time", "Description", "Vendor", "Category", "Amount in $");
        System.out.println("-".repeat(101)); // creates line of dashes
    }

    /**
     * Prints 1 transaction in a formatted column.
     *
     * @param transaction takes the transaction that needs to be printed
     * */
    private static void printTransaction(Transaction transaction) {
        System.out.printf("%-6s %-12s %-10s %-35s %-20s %-12s %.2f%n", // assigns and holds x amount of spaces starting from the left
                transaction.getId(),
                transaction.getDate().format(DATE_FMT),
                transaction.getTime().format(TIME_FMT),
                transaction.getDescription(),
                transaction.getVendor(),
                transaction.getCategory(),
                transaction.getAmount());
        }
    /* ------------------------------------------------------------------
       Reports menu
       ------------------------------------------------------------------ */
    /**
     * Displays the reports menu and lets the user navigate it.
     *
     * @param scanner Used to read user navigation commands
     * */
    private static void reportsMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n📊 Reports");
            System.out.println("=================================");
            System.out.println("Choose an option:");
            System.out.println("1) 📅 Month To Date");
            System.out.println("2) 📆 Previous Month");
            System.out.println("3) 🗓️  Year To Date");
            System.out.println("4) 📉 Previous Year");
            System.out.println("5) 🏪 Search by Vendor");
            System.out.println("6) 🔎 Custom Search");
            System.out.println("0) 🔙 Back");
            System.out.println("=================================");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {monthToDate();}
                case "2" -> {previousMonth(); }
                case "3" -> {yearToDate(); }
                case "4" -> {previousYear(); }
                case "5" -> {searchByVendor(scanner); }
                case "6" -> customSearch(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /**
     * Displays transactions from start of current month up to the current date.
     * */
    private static void monthToDate() {
        // get start and end date info
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1); // takes today and creates new date with same year and month but the day is set to 1

        // print header
        System.out.println("\nMONTH TO DATE");
        printLedgerHeader();

        filterTransactionsByDate(startOfMonth, today); // method takes the start and end date and handles the filtering logic
    }

    /**
     * Displays transactions from the previous month.
     * */
    private static void previousMonth() {
        // get start and end date info
        LocalDate today = LocalDate.now();
        LocalDate firstOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastOfLastMonth = firstOfLastMonth.withDayOfMonth(firstOfLastMonth.lengthOfMonth()); // withDateOfMonth takes the last day of the month

        // print header
        System.out.println("\nPREVIOUS MONTH");
        printLedgerHeader();

        filterTransactionsByDate(firstOfLastMonth, lastOfLastMonth); // method takes the start and end date and handles the filtering logic
    }

    /**
     * Displays transactions from the start of current year up to the current date.
     * */
    private static void yearToDate() {
        // get start and end date info
        LocalDate today = LocalDate.now();
        LocalDate startOfYear = today.withDayOfYear(1);

        // print header
        System.out.println("\nYEAR TO DATE");
        printLedgerHeader();

        filterTransactionsByDate(startOfYear, today); // method takes the start and end date and handles the filtering logic
    }

    /**
     * Displays transactions from the previous year.
     * */
    private static void previousYear() {
        // get start and end date info
        LocalDate today = LocalDate.now();
        LocalDate firstOfLastYear = today.minusYears(1).withDayOfYear(1);
        LocalDate lastOfLastYear = firstOfLastYear.withDayOfYear(firstOfLastYear.lengthOfYear());

        // print header
        System.out.println("\nPREVIOUS YEAR");
        printLedgerHeader();

        filterTransactionsByDate(firstOfLastYear, lastOfLastYear); // method takes the start and end date and handles the filtering logic
    }

    /**
     * Gets vendor name from user and prints ledger header
     *
     * @param scanner Used to read name of vendor the user wants to search for
     * */
    private static void searchByVendor(Scanner scanner) {
        System.out.print("Search vendor name: ");
        String vendorName = scanner.nextLine().trim(); // saves user input as a string

        // print header
        System.out.println("VENDOR SEARCH");
        printLedgerHeader();

        filterTransactionsByVendor(vendorName); // method takes the string and handles filtering logic
    }
    /* ------------------------------------------------------------------
       Reporting helpers
       ------------------------------------------------------------------ */
    /**
     * Prints transactions between the given dates
     *
     * @param start the start date
     * @param end the end date
     * */
    private static void filterTransactionsByDate(LocalDate start, LocalDate end) {
        boolean found = false;
        for (Transaction transaction : transactions) {
            if (transaction.getDate().isBefore(start)) continue;
            if (transaction.getDate().isAfter(end)) continue;
            printTransaction(transaction);
            found = true;
        }
        if (!found) System.out.println("No transactions found for that period.");
    }

    /**
     * Prints transactions that match user given vendor
     *
     * @param vendorName User given vendor name that gets compared to list of vendors
     * */
    private static void filterTransactionsByVendor(String vendorName) {
        boolean found = false;
        for (Transaction transaction : transactions) {
            if (!transaction.getVendor().toLowerCase().contains(vendorName.toLowerCase())) continue;
            printTransaction(transaction);
            found = true;
        }
        if (!found) System.out.println("No transactions found for " + vendorName);
    }

    /**
     * Prints filtered transactions
     *
     * @param scanner Used to read user input
     * */
    private static void customSearch(Scanner scanner) {
        System.out.println("🔍 Custom Search — press 'ENTER' to leave blank");

        LocalDate start = parseDate("📅 Start date (yyyy-MM-dd): ", scanner);

        LocalDate end = parseDate("📅 End date (yyyy-MM-dd): ", scanner);

        System.out.print("📝 Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("🏪 Vendor name: ");
        String vendorName = scanner.nextLine().trim();


        Double amount = parseDouble("Amount: ", scanner);

        printLedgerHeader();
        boolean found = false;
        for (Transaction transaction : transactions) {
            if (start != null && transaction.getDate().isBefore(start)) continue;
            if (end != null && transaction.getDate().isAfter(end)) continue;
            if (!description.isBlank() && !transaction.getDescription().equalsIgnoreCase(description)) continue;
            if (!vendorName.isBlank() && !transaction.getVendor().equalsIgnoreCase(vendorName)) continue;
            if (amount != null && transaction.getAmount() != amount) continue;
            printTransaction(transaction);
            found = true;
        }
        if (!found) System.out.println("No transactions found matching your search");
    }

    /* ------------------------------------------------------------------
       Utility parsers (you can reuse in many places)
       ------------------------------------------------------------------ */
    /**
     * Prompts for a date
     *
     * @param prompt The message displayed to the user (asking for date input)
     * @param scanner Used to read the user given date
     * @return parsed LocalDate or null if user leaves input blank
     * */
   private static LocalDate parseDate(String prompt, Scanner scanner) {

        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isBlank()) return null;

            try {
                return LocalDate.parse(input, DATE_FMT);
            } catch (Exception e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd or press Enter to skip.");
            }
        }
    }

    /**
     * prompts for an amount
     *
     * @param prompt The message displayed to the user (asking for amount input)
     * @param scanner Used to read the user given amount
     * @return parsed Double or null if user leaves input blank
     * */
    private static Double parseDouble(String prompt, Scanner scanner) {

       while (true) {
           System.out.print(prompt);
           String input = scanner.nextLine().trim();

           if (input.isBlank()) return null;
           try {
               return Double.parseDouble(input);
           } catch (Exception e) {
               System.out.println("Invalid amount. Please enter a number or press Enter to skip.");
           }
       }
    }

    /**
     * prompts for a category type
     *
     * @param scanner Used to read the user given category type
     * @return the CategoryType the user selected; keeps prompting until valid input is given
     * */
    // category type menu helper method
    private static CategoryType selectCategory(Scanner scanner) {

        CategoryType category = null;

        boolean running = true;
        while (running) {
            System.out.println("🍔 (1) Food");
            System.out.println("⛽ (2) Gas");
            System.out.println("🎬 (3) Entertainment");
            System.out.println("📦 (4) Other");
            System.out.print("👉 Choose category: ");
            int categoryChoice = scanner.nextInt();scanner.nextLine();

            switch (categoryChoice) {

                case 1 -> {
                    category = CategoryType.FOOD;
                    running = false;
                }
                case 2 -> {
                    category = CategoryType.GAS;
                    running = false;
                }
                case 3 -> {
                    category = CategoryType.ENTERTAINMENT;
                    running = false;
                }
                case 4 -> {
                    category = CategoryType.OTHER;
                    running = false;
                }
                default -> System.out.print("invalid option");
            }
        }
        return category;
    }
}

