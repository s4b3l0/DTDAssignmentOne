import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;


final class BatchDate {
    private int batchNumber;
    private LocalDate expDate;

    public BatchDate(Integer batchNumber, LocalDate expDate) {
        this.batchNumber = batchNumber;
        this.expDate = expDate;
    }

    public Integer getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(Integer batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getExpDate() {
        return expDate;
    }

    public void setExpDate(LocalDate expDate) {
        this.expDate = expDate;
    }


}

public class Bakery {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    final List<String> salesProducts = new ArrayList<>();
    final HashMap<Integer, LocalDate> expDate = new HashMap<>();
    final HashMap<BatchDate, PriorityQueue> expStock = new HashMap<>();
    final List<String> soldProducts = new ArrayList<>();

    private static int getBatchNumber(String name) {
        if (name == null) return -1;
        if (name.split(" ").length != 3) return -1;
        if (!name.split(" ")[1].equalsIgnoreCase("batch")) return -1;
        return Integer.parseInt(name.split(" ")[2]);
    }

    public static void main(String[] args) {
        Bakery bakery = new Bakery();
        bakery.salesProducts.add("Croissant batch 220");
        bakery.salesProducts.add("Croissant batch 220");
        bakery.salesProducts.add("Croissant batch 230");
        bakery.salesProducts.add("Croissant batch 230");
        bakery.salesProducts.add("Croissant batch 280");
        bakery.salesProducts.add("Croissant batch 280");


        bakery.expDate.put(220, LocalDate.of(2020, 12, 30));
        bakery.expDate.put(230, LocalDate.of(2023, 12, 30));
        bakery.expDate.put(280, LocalDate.of(2025, 12, 30));

        bakery.listInventory();

        reduceSelection(prompt(), bakery);
    }

    private static char prompt() {
        System.out.println(ANSI_RESET);
        System.out.println("Type A to add croissant");
        System.out.println("Type D to remove croissant");
        System.out.println("Type U update expiry dates to croissant");
        System.out.println("Type L list product and expiry dates");
        System.out.println("Type B list batch and expiry dates");
        System.out.println("Type S list and sell by freshest");
        System.out.println("Type X to exit");
        System.out.print(ANSI_GREEN + ":");

        String s = null;
        while (s == null || s.length() != 1) {
            Scanner in = new Scanner(System.in);
            s = in.nextLine();

            if (s.length() > 1) {
                System.out.println(ANSI_RED + "Unknown operation entered!");
            }
            System.out.println(ANSI_RESET);
        }
        return s.toUpperCase().charAt(0);
    }

    private static void reduceSelection(char selected, Bakery bakery) {
        String input = null;
        switch (selected) {
            case 'A':
                while (getBatchNumber(input) == -1) {
                    System.out.print(ANSI_GREEN + "Add croissant name (Coissant batch XXX) \n:");
                    Scanner sc = new Scanner(System.in);
                    input = sc.nextLine();
                    if (getBatchNumber(input) == -1) {
                        System.out.println(ANSI_RED + "\n Invalid Input" + ANSI_RESET);
                    }
                }
                bakery.salesProducts.add(input);
                String refProduct = input;
                input = null;
                LocalDate expDate = null;
                while (input == null) {
                    System.out.print(ANSI_GREEN + "Please enter expiry date for product " + refProduct + " in ISO format [YYYY-MM-DD] \n:");
                    Scanner sc = new Scanner(System.in);
                    input = sc.nextLine();
                    String[] slicedInput = input.split("-");
                    boolean valid = true;

                    try {
                        if (slicedInput.length != 3) throw new Exception();
                        for (String s : slicedInput) {
                            Integer.parseInt(s);
                        }
                        expDate = LocalDate.parse(input, DateTimeFormatter.ISO_DATE);
                    } catch (NumberFormatException | DateTimeParseException e) {
                        valid = false;
                        System.out.print(ANSI_RED + e.getMessage() + " [date is invalid]" + ANSI_RESET);
                    } catch (Exception e) {
                        valid = false;
                        System.out.println(ANSI_RED + "Please use ISO date format [YYYY-MM-DD]" + ANSI_RESET);
                    } finally {
                        if (!valid) {
                            input = null;
                        }
                    }
                }
                bakery.expDate.put(getBatchNumber(refProduct), expDate);
                break;
            case 'L':
                bakery.listInventory();
                break;
            case 'E':
                bakery.listBatch();
                break;
            case 'S':
                break;
            case 'X':
                System.exit(0);
                break;
        }
        reduceSelection(prompt(), bakery);
    }

    /**
     * This method will print list available inventory on the standard io
     */
    private void listInventory() {
        System.out.println(ANSI_YELLOW);
        final String firstField = "| %-40s |";
        final String field = " %-40s |";
        final String heading1 = String.format(firstField, "INVENTORY").replace(' ', '_');
        final String heading2 = String.format(field, "EXPIRY DATES").replace(' ', '_');
        System.out.println(heading1 + heading2);
        salesProducts.forEach(prod -> {
            final String product = String.format(firstField, prod);
            final String expiryDate = String.format(field, expDate.get(getBatchNumber(prod)));
            System.out.println(product + expiryDate);
        });
        System.out.println(ANSI_RESET);
    }

    /**
     * This method will list batches on standard IO
     */
    private void listBatch() {
        System.out.println(ANSI_YELLOW);
        final String firstField = "| %-40s |";
        final String field = " %-40s |";
        final String heading1 = String.format(firstField, "BATCHES").replace(' ', '_');
        final String heading2 = String.format(field, "EXPIRY DATES").replace(' ', '_');
        System.out.println(heading1 + heading2);
        expDate.forEach((batchNo, date) -> {
            final String product = String.format(firstField, batchNo);
            final String expiryDate = String.format(field, date);
            System.out.println(product + expiryDate);
        });
        System.out.println(ANSI_RESET);
    }
}
