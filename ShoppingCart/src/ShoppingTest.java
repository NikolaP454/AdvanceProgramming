import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

class InvalidOperationException extends Exception {
    public InvalidOperationException(String message) {
        super(message);
    }
}

class ProductFactory {
    public static Product generateProduct(String data) throws InvalidOperationException {
        String [] splitData = data.split(";");

        String type     = splitData[0];
        String id       = splitData[1];
        String name     = splitData[2];
        int basePrice   = Integer.parseInt(splitData[3]);

        if(splitData[4].equals("0")) {
            throw new InvalidOperationException(
                    String.format("The quantity of the product with id %s can not be 0.", id)
            );
        }

        switch (type) {
            case "WS": return new WholeSaleProduct(id, name, basePrice, Integer.parseInt(splitData[4]));
            case "PS": return new ByGramProduct(id, name, basePrice, Double.parseDouble(splitData[4]));
        }

        return null;
    }
}

abstract class Product implements Comparable<Product> {
    String id;
    String name;
    double basePrice;

    public Product(String id, String name, int basePrice) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
    }

    public double blackFridayReduce() {
        double beforeDiscountPrice = getTotalPrice();
        basePrice *= 0.9;

        return beforeDiscountPrice - getTotalPrice();
    }

    public String getId() {
        return id;
    }

    public abstract double getTotalPrice();

    @Override
    public String toString() {
        return String.format("%s - %.2f\n", id, getTotalPrice());
    }

    @Override
    public int compareTo(Product o) {
        return Double.compare(getTotalPrice(), o.getTotalPrice());
    }
}

class WholeSaleProduct extends Product {
    int numberOfProduct;

    public WholeSaleProduct(String id, String name, int basePrice, int numberOfProduct) {
        super(id, name, basePrice);
        this.numberOfProduct = numberOfProduct;
    }

    @Override
    public double getTotalPrice() {
        return numberOfProduct * basePrice;
    }
}

class ByGramProduct extends Product {
    double grams;

    public ByGramProduct(String id, String name, int basePrice, double grams) {
        super(id, name, basePrice);
        this.grams = grams;
    }

    @Override
    public double getTotalPrice() {
        return grams * 0.001 * basePrice;
    }
}

class ShoppingCart {
    ArrayList<Product> products;

    public ShoppingCart() {
        products = new ArrayList<>();
    }

    public void addItem(String itemData) throws InvalidOperationException {
        products.add(ProductFactory.generateProduct(itemData));
    }

    public void printShoppingCart(OutputStream os) {
        products.sort(Comparator.reverseOrder());

        for (Product product : products) {
            byte [] toWrite = product.toString().getBytes();

            try {
                os.write(toWrite);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void blackFridayOffer(List<Integer> discountItems, OutputStream os) throws InvalidOperationException{
        if (discountItems.size() == 0)
            throw new InvalidOperationException("There are no products with discount.");

        for(Product product : products) {
            if(!discountItems.contains(Integer.parseInt(product.getId())))
                continue;

            String output = String.format("%s - %.2f\n", product.getId(), product.blackFridayReduce());

            try {
                os.write(output.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

public class ShoppingTest {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ShoppingCart cart = new ShoppingCart();

        int items = Integer.parseInt(sc.nextLine());
        for (int i = 0; i < items; i++) {
            try {
                cart.addItem(sc.nextLine());
            } catch (InvalidOperationException e) {
                System.out.println(e.getMessage());
            }
        }

        List<Integer> discountItems = new ArrayList<>();
        int discountItemsCount = Integer.parseInt(sc.nextLine());
        for (int i = 0; i < discountItemsCount; i++) {
            discountItems.add(Integer.parseInt(sc.nextLine()));
        }

        int testCase = Integer.parseInt(sc.nextLine());
        if (testCase == 1) {
            cart.printShoppingCart(System.out);
        } else if (testCase == 2) {
            try {
                cart.blackFridayOffer(discountItems, System.out);
            } catch (InvalidOperationException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Invalid test case");
        }
    }
}