import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

abstract class Property {
    protected String location;
    protected double price;
    protected String propertyType;
    protected int id;
    protected boolean sold;
    protected String sellerName;

    public Property(int id, String location, double price, String propertyType, String sellerName) {
        this.id = id;
        this.location = location;
        this.price = price;
        this.propertyType = propertyType;
        this.sold = false;
        this.sellerName = sellerName;
    }

    public abstract void displayPropertyDetails();

    public String getLocation() { return location; }
    public double getPrice() { return price; }
    public String getPropertyType() { return propertyType; }
    public int getId() { return id; }
    public boolean isSold() { return sold; }
    public void markAsSold() { this.sold = true; }
    public String getSellerName() { return sellerName; }
}

class Apartment extends Property {
    private int floorNumber;
    private int bhk;

    public Apartment(int id, String location, int bhk, int floorNumber, String sellerName) {
        super(id, location, calculatePrice(location, bhk, floorNumber), "Apartment", sellerName);
        this.floorNumber = floorNumber;
        this.bhk = bhk;
    }

    private static double calculatePrice(String location, int bhk, int floorNumber) {
        double basePrice = getBasePrice(location);
        double price = basePrice + (bhk * 5000) - (floorNumber > 10 ? 0 : (floorNumber * 1000));
        return Math.max(price, basePrice);
    }

    private static double getBasePrice(String location) {
        switch (location.toLowerCase()) {
            case "ahmedabad": return 40000;
            case "mumbai": return 50000;
            case "delhi": return 60000;
            case "chennai": return 70000;
            case "kolkata": return 80000;
            default: return 10000;
        }
    }

    public void displayPropertyDetails() {
        System.out.printf("%-5d %-15s %-10.2f %-15s %-10s\n", id, location, price, propertyType, bhk + " BHK, Floor " + floorNumber);
    }
}

class Bungalow extends Property {
    private double plotSize;

    public Bungalow(int id, String location, double plotSize, String sellerName) {
        super(id, location, calculatePrice(location, plotSize), "Bungalow", sellerName);
        this.plotSize = plotSize;
    }

    private static double calculatePrice(String location, double plotSize) {
        double basePrice = getBasePrice(location);
        return basePrice + (plotSize * 10000);
    }

    private static double getBasePrice(String location) {
        switch (location.toLowerCase()) {
            case "ahmedabad": return 40000;
            case "mumbai": return 50000;
            case "delhi": return 60000;
            case "chennai": return 70000;
            case "kolkata": return 80000;
            default: return 10000;
        }
    }

    public void displayPropertyDetails() {
        System.out.printf("%-5d %-15s %-10.2f %-15s %-10s\n", id, location, price, propertyType, plotSize + " acres");
    }
}

class Buyer {
    private String name;
    private double budget;
    private Property purchasedProperty;
    private String transactionType;
    private boolean transactionSuccess;

    public Buyer(String name, double budget) {
        this.name = name;
        this.budget = budget;
        this.transactionSuccess = false;
    }

    public void suggestPropertiesByType(List<Property> properties, String propertyType) {
        System.out.println("\nOptions for " + name + " (" + propertyType + "s):");
        boolean hasOptions = false;

        for (Property property : properties) {
            if (!property.isSold() && property.getPropertyType().equalsIgnoreCase(propertyType) && this.canAfford(property)) {
                System.out.printf("ID: %d, Location: %s, Price: Rs %.2f\n",
                    property.getId(), property.getLocation(), property.getPrice());
                hasOptions = true;
            }
        }

        if (!hasOptions) {
            System.out.println("\nNo properties available in your budget for this type.");
        }
    }

    public boolean canAfford(Property property) {
        return this.budget >= property.getPrice();
    }

    public void buyProperty(Property property) throws Exception {
        if (property.isSold()) {
            throw new Exception("Property ID " + property.getId() + " is already sold.");
        }

        if (this.canAfford(property)) {
            this.purchasedProperty = property;
            this.budget -= property.getPrice();
            this.transactionType = "Purchased";
            property.markAsSold();
            this.transactionSuccess = true;
            System.out.println(name + " bought property ID " + property.getId() + " at " + property.getLocation());
        } else {
            this.transactionSuccess = false;
            throw new Exception(name + " cannot afford property ID " + property.getId() + " priced at Rs " + property.getPrice());
        }
    }

    public String getName() { return name; }
    public double getBudget() { return budget; }
    public Property getPurchasedProperty() { return purchasedProperty; }
    public String getTransactionType() { return transactionSuccess ? transactionType : "N/A"; }
    public boolean isTransactionSuccessful() { return transactionSuccess; }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Buyer> buyers = new ArrayList<>();
        List<Property> properties = new ArrayList<>();

        try {
            BufferedReader buyerReader = new BufferedReader(new FileReader("buyers.txt"));
            String line;
            while ((line = buyerReader.readLine()) != null) {
                String[] details = line.split(",");
                String name = details[0].trim();
                double budget = Double.parseDouble(details[1].trim());
                buyers.add(new Buyer(name, budget));
            }
            buyerReader.close();

            BufferedReader propertyReader = new BufferedReader(new FileReader("properties.txt"));
            while ((line = propertyReader.readLine()) != null) {
                String[] details = line.split(",");
                int id = Integer.parseInt(details[0].trim());
                String location = details[1].trim();
                String type = details[2].trim();
                String sellerName = details[3].trim();

                Property property;
                if (type.equalsIgnoreCase("Apartment")) {
                    int bhk = Integer.parseInt(details[4].trim());
                    int floorNumber = Integer.parseInt(details[5].trim());
                    property = new Apartment(id, location, bhk, floorNumber, sellerName);
                } else if (type.equalsIgnoreCase("Bungalow")) {
                    double plotSize = Double.parseDouble(details[4].trim());
                    property = new Bungalow(id, location, plotSize, sellerName);
                } else {
                    continue;
                }
                properties.add(property);
            }
            propertyReader.close();

            displayBuyerDetails(buyers);

            displayPropertyDetails(properties);

            buyers.sort(Comparator.comparingDouble(Buyer::getBudget).reversed());

            for (Buyer buyer : buyers) {
                System.out.println("\n" + buyer.getName() + "'s turn to buy a property (Budget: Rs " + buyer.getBudget() + ")");

                System.out.print("Enter property type (Apartment/Bungalow) you wish to buy: ");
                String propertyType = scanner.next();

                buyer.suggestPropertiesByType(properties, propertyType);

                while (true) {
                    System.out.print("Enter property ID to purchase or 0 to skip: ");
                    int propertyId = scanner.nextInt();

                    if (propertyId == 0) break;

                    Property selectedProperty = properties.stream()
                            .filter(property -> property.getId() == propertyId && property.getPropertyType().equalsIgnoreCase(propertyType))
                            .findFirst()
                            .orElse(null);

                    if (selectedProperty != null) {
                        try {
                            buyer.buyProperty(selectedProperty);
                            break;
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    } else {
                        System.out.println("Property not found or doesn't match the selected type.");
                    }
                }
            }

            printTransactionSummary(buyers);

        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }

        scanner.close();
    }

    private static void displayBuyerDetails(List<Buyer> buyers) {
        System.out.println("\nBuyer Details:");
        System.out.printf("%-20s %-10s\n", "Name", "Budget");
        System.out.println("------------------------------------");
        for (Buyer buyer : buyers) {
            System.out.printf("%-20s Rs %-10.2f\n", buyer.getName(), buyer.getBudget());
        }
        System.out.println("------------------------------------");
    }

    private static void displayPropertyDetails(List<Property> properties) {
        System.out.println("\nProperty Details:");
        System.out.printf("%-5s %-15s %-10s %-15s %-20s\n", "ID", "Location", "Price", "Type", "Details");
        System.out.println("---------------------------------------------------------------");
        for (Property property : properties) {
            property.displayPropertyDetails();
        }
        System.out.println("---------------------------------------------------------------");
    }

    private static void printTransactionSummary(List<Buyer> buyers) {
        System.out.println("\nTransaction Summary:");
        System.out.printf("%-20s %-20s %-15s %-15s %-15s\n", "Name", "Property Bought", "Property Type", "Money Spent", "Balance Money");
        System.out.println("-------------------------------------------------------------------------------------------");

        for (Buyer buyer : buyers) {
            String propertyBought = (buyer.getPurchasedProperty() != null) ? String.valueOf(buyer.getPurchasedProperty().getId()) : "None";
            String propertyType = (buyer.getPurchasedProperty() != null) ? buyer.getPurchasedProperty().getPropertyType() : "N/A";
            double moneySpent = (buyer.getPurchasedProperty() != null) ? buyer.getPurchasedProperty().getPrice() : 0;
            double balance = buyer.getBudget();

            System.out.printf("%-20s %-20s %-15s %-15.2f %-15.2f\n", buyer.getName(), propertyBought, propertyType, moneySpent, balance);
        }
    }
}
