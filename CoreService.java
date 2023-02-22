import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;


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

class Bakery {

    final List<String> salesProducts = new ArrayList<>();
    final HashMap<Integer, LocalDate> expDate = new HashMap<>();
    final HashMap<BatchDate, List> expStock = new HashMap<>();

    final List<String> getSalesProducts = new ArrayList<>();

    final List<String> soldProducts = new ArrayList<>();

    final PriorityQueue priorityProducts = new PriorityQueue(new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            final LocalDate o1Exp = expDate.get(getBatchNumber((String) o1));
            final LocalDate o2Exp = expDate.get(getBatchNumber((String) o2));
            return o2Exp.compareTo(o1Exp);
        }

    });


    static int getBatchNumber(String name) {
        if (name == null) return -1;
        if (name.split(" ").length != 3) return -1;
        if (!name.split(" ")[1].equalsIgnoreCase("batch")) return -1;
        return Integer.parseInt(name.split(" ")[2]);
    }

    public HashMap<BatchDate, List> getExpStock() {
        return expStock;
    }

    /**
     * This method will list batches on standard IO
     */
    void listBatch() {
        System.out.println(CoreService.ANSI_YELLOW);
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
        System.out.println(CoreService.ANSI_RESET);
    }


    void listInventory() {
        System.out.println(CoreService.ANSI_YELLOW);
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
        System.out.println(CoreService.ANSI_RESET);
    }

    void listInventory(List<String> list, LocalDate expDate) {
        System.out.println(CoreService.ANSI_YELLOW);
        final String firstField = "| %-40s |";
        final String field = " %-40s |";
        final String heading1 = String.format(firstField, "INVENTORY").replace(' ', '_');
        final String heading2 = String.format(field, "EXPIRY DATES").replace(' ', '_');
        System.out.println(heading1 + heading2);
        list.forEach(prod -> {
            final String product = String.format(firstField, prod);
            final String expiryDate = String.format(field, expDate);
            System.out.println(product + expiryDate);
        });
        System.out.println(CoreService.ANSI_RESET);
    }

    public void removeExpiredBatches() {
        final List<Integer>  batchToDelete = new ArrayList<>(); //To avoid concurrency exception
        this.expDate.forEach((batchNo, date) -> {
            if (date.isBefore(LocalDate.now())){
               final BatchDate batchDate = new BatchDate(batchNo, date);
               final List<String> expiredStock = salesProducts
                       .stream()
                       .filter(salesProduct -> getBatchNumber(salesProduct) == batchNo)
                       .collect(Collectors.toList());
               expStock.put(batchDate, expiredStock);
               salesProducts.removeIf(salesProduct -> getBatchNumber(salesProduct) == batchNo);
               batchToDelete.add(batchNo);
            }
        });
        batchToDelete.forEach(batchNo -> expDate.remove(batchNo));
    }

    public void sellIt() {
        final List<Integer>  batchToDelete = new ArrayList<>(); //To avoid concurrency exception
        this.expDate.forEach((batchNo, data ) -> {
            if (data.isAfter(LocalDate.now())) {
                final List<String> Sold = salesProducts;
                soldProducts.addAll( salesProducts
                        .stream().filter(soldProduct -> getBatchNumber(soldProduct) == batchNo) //is stream framework allowed ?
                        .map(prod -> prod.substring(0, prod.lastIndexOf(' ')))
                        .collect(Collectors.toList()));
                salesProducts.removeIf(salesProduct -> getBatchNumber(salesProduct) == batchNo);
                batchToDelete.add(batchNo);
            }
        });
        //sold batch removing from expDate
        batchToDelete.forEach(batchNo -> expDate.remove(batchNo));
    }

    public void loadSheddingAdjust() {
        expDate.forEach(((batchNo, localDate) -> expDate.put(batchNo, localDate.minusDays(1))));
    }

    public void sellUnits(Integer units) {
        priorityProducts.clear();
        this.salesProducts.forEach(p -> priorityProducts.offer(p));
        listPriorityBatch(priorityProducts);

        int initialSize = priorityProducts.size();

        while (initialSize - priorityProducts.size() != units && priorityProducts.size() > 0) {

            String prod = (String) priorityProducts.poll();

            this.soldProducts.add(prod.substring(0, prod.lastIndexOf(' ')));
            this.salesProducts.remove(prod);
            if (this.salesProducts.stream().filter(x -> getBatchNumber(x) == getBatchNumber(prod)).collect(Collectors.toList()).size() == 0) {
                this.expDate.remove(getBatchNumber(prod));
            }
        }
        System.out.println("Remaining in priority products after sales");
        listPriorityBatch(priorityProducts);
    }

    private void listPriorityBatch(PriorityQueue priorityQueue) {
        System.out.println(CoreService.ANSI_YELLOW);
        System.out.println("______________PRIORITY_INVENTORY_________________");
        final String firstField = "| %-40s |";
        final String field = " %-40s |";
        final String heading1 = String.format(firstField, "INVENTORY").replace(' ', '_');
        final String heading2 = String.format(field, "EXPIRY DATES").replace(' ', '_');
        System.out.println(heading1 + heading2);
        priorityQueue.forEach(prod -> {
            final String product = String.format(firstField, prod);
            final String expiryDate = String.format(field, expDate.get(getBatchNumber((String) prod)));
            System.out.println(product + expiryDate);
        });
        System.out.println(CoreService.ANSI_RESET);
    }
}


public class CoreService {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public static void main(String[] args) throws NoSuchMethodException {
        Bakery bakery = new Bakery();
        bakery.salesProducts.add("Croissant batch 220");
        bakery.salesProducts.add("Croissant batch 280");
        bakery.salesProducts.add("Croissant batch 230");
        bakery.salesProducts.add("Croissant batch 230");
        bakery.salesProducts.add("Croissant batch 220");
        bakery.salesProducts.add("Croissant batch 280");

        bakery.expDate.put(280, LocalDate.of(2022, 12, 30));
        bakery.expDate.put(230, LocalDate.of(2023, 12, 30));
        bakery.expDate.put(220, LocalDate.of(2020, 12, 30));

        bakery.listInventory();

        reduceSelection(prompt(), bakery);
    }


    private static void reduceSelection(char selected, Bakery bakery) throws NoSuchMethodException {
        String input = null;
        switch (selected) {
            case 'A':
                while (Bakery.getBatchNumber(input) == -1) {
                    System.out.print(ANSI_GREEN + "Add croissant name (Coissant batch XXX) \n:");
                    Scanner sc = new Scanner(System.in);
                    input = sc.nextLine();
                    if (Bakery.getBatchNumber(input) == -1) {
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
                        System.out.println(ANSI_RED + e.getMessage() + " [date is invalid]" + ANSI_RESET);
                    } catch (Exception e) {
                        valid = false;
                        System.out.println(ANSI_RED + "Please use ISO date format [YYYY-MM-DD]" + ANSI_RESET);
                    } finally {
                        if (!valid) {
                            input = null;
                        }
                    }
                }
                bakery.expDate.put(Bakery.getBatchNumber(refProduct), expDate);
                break;
            case 'L':
                bakery.listInventory();
                break;
            case 'E':
                bakery.listBatch();
                break;
            case 'D':
                //Remove expired batches from stock load them to expired data
                while (input == null) {
                    System.out.print(ANSI_RED + "System will start clearing of expired inventory, do you with to continue [ Y / N ]?\n:" + ANSI_RESET);
                    final Scanner scanner = new Scanner(System.in);
                    input = scanner.nextLine();
                    if (input.length() == 1) {
                        switch (input.toUpperCase().charAt(0)) {
                            case 'Y':
                                bakery.removeExpiredBatches();
                                bakery.expStock.forEach((batchDate, list)  -> {
                                    System.out.println(ANSI_YELLOW + "\nBatch Group:"  + batchDate.getBatchNumber() + "\nExpired:" + batchDate.getExpDate());
                                    bakery.listInventory(list, batchDate.getExpDate());
                                });
                                break;
                            case 'N': break;
                            default:
                                System.out.println(ANSI_RED + "Unknown operation selected" + ANSI_RESET);
                                input = null;
                        }
                    } else {
                        System.out.println(ANSI_RED + "Unknown operation selected" + ANSI_RESET);
                        input = null;
                    }
                }
                break;
            case 'S':
                while (input == null) {
                    final String message = "Sell all unexpired inventory to continue enter Y to confirm [ Y / N ]?";
                    try {
                        input = generiDialogPrompt(message, bakery ,bakery.getClass().getDeclaredMethod("sellIt"));
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            case 'U':
               while (input == null) {
                   final String message = "Adjust expiry dates for inventory items [ Y / N ]?";
                   try {
                       input = generiDialogPrompt(message, bakery ,bakery.getClass().getDeclaredMethod("loadSheddingAdjust"));
                   } catch (InvocationTargetException e) {
                       throw new RuntimeException(e);
                   } catch (IllegalAccessException e) {
                       throw new RuntimeException(e);
                   }
               }
               break;
            case 'G':
                while (input == null) {
                    final String message = "Sell by freshness?";
                    try {
                        input = generiDialogPrompt(message, null ,null);

                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (input.equalsIgnoreCase("Y")) {
                    String message = ANSI_RED + "How many units do you wish to sell?\n:" + ANSI_RESET;
                    Integer units = null;
                    while (units == null) {
                        try {
                            System.out.print(message);
                            Scanner scanner = new Scanner(System.in);
                            units = scanner.nextInt();
                        } catch (Exception e) {
                            units = null;
                            System.out.println(ANSI_RED + "Input is invalid" + ANSI_RESET);
                        }
                    }
                    bakery.sellUnits(units);
                }

                //expDate, expStock, soldProducts, priorityProducts and salesProducts.
                break;
            case 'M':
                System.out.println(ANSI_GREEN + "_____________Number of Records Summary_______________");
                System.out.println("expDate         :" + bakery.expDate.size());
                System.out.println("expStock        :" + bakery.expStock.size());
                System.out.println("soldProducts    :" + bakery.soldProducts.size());
                System.out.println("priorityProducts:" + bakery.priorityProducts.size());
                System.out.println("salesProducts   :" + bakery.salesProducts.size()+ ANSI_RESET);
                break;
            case 'X':
                System.exit(0);
                break;
        }
        reduceSelection(prompt(), bakery);
    }

    static String generiDialogPrompt(String message, Object object, Method method) throws InvocationTargetException, IllegalAccessException {
            System.out.print(ANSI_RED + message + "\n:" + ANSI_RESET);
            final Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if (input.length() == 1) {
                switch (input.toUpperCase().charAt(0)) {
                    case 'y':
                    case 'Y':
                        if (object != null) {
                            method.invoke(object);
                        }
                        return input;
                    case 'N':
                        return input;
                    default:
                        System.out.println(ANSI_RED + "Unknown operation selected" + ANSI_RESET);
                        return input;
                }
            } else {
                System.out.println(ANSI_RED + "Unknown operation selected" + ANSI_RESET);
                return input;
            }
    }
    /**
     * This method will print list available inventory on the standard io
     */


    private static char prompt() {
        System.out.println(ANSI_RESET);
        System.out.println("Type A to add croissant");
        System.out.println("Type D to remove croissant");
        System.out.println("Type U update expiry dates of inventory(Load Shedding)");
        System.out.println("Type L list product and expiry dates");
        System.out.println("Type E list batch and expiry dates");
        System.out.println("Type S sell all unexpired products");
        System.out.println("Type G to sell by freshest to least fresh");
        System.out.println("Type M to list number of elements in each data set");
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
}
