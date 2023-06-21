import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.IntStream;

class Entry implements Comparable<Entry>{
    String name;
    int price;

    public Entry(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name + " " + price;
    }

    @Override
    public int compareTo(Entry o) {
        return Integer.compare(getPrice(), o.getPrice());
    }
}

class OnlinePayments {
    HashMap<String, ArrayList<Entry>> students;

    public OnlinePayments() {
        students = new HashMap<>();
    }

    public void readItems(InputStream input) {
        Scanner sc = new Scanner(input);

        while (sc.hasNextLine()) {
            String [] line = sc.nextLine().split(";");

            String index    = line[0];
            String content  = line[1];
            int price       = Integer.parseInt(line[2]);

            students.putIfAbsent(index, new ArrayList<>());
            students.get(index).add(new Entry(content, price));
        }
    }

    public void printStudentReport(String index, OutputStream output) {
        if(!students.containsKey(index)) {
            System.out.printf("Student %s not found!\n", index);
            return;
        }

        ArrayList<Entry> items = students.get(index);

        int net         = items.stream().mapToInt(Entry::getPrice).sum();
        int provision   = Math.min(300, Math.max(3, (int) Math.round(net * 0.0114)));
        int total       = net + provision;

        try {
            output.write(String.format(
                    "Student: %s Net: %d Fee: %d Total: %d\n",
                    index,
                    net,
                    provision,
                    total
                    ).getBytes()
            );

            PriorityQueue<Entry> pq = new PriorityQueue<>(Comparator.reverseOrder());
            pq.addAll(items);

            output.write("Items:\n".getBytes());

            for(int i = 0; i < items.size(); i++)
                output.write(String.format("%d. %s\n", i+1, pq.poll()).getBytes());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

public class OnlinePaymentsTest {
    public static void main(String[] args) {
        OnlinePayments onlinePayments = new OnlinePayments();

        onlinePayments.readItems(System.in);

        IntStream.range(151020, 151025).mapToObj(String::valueOf).forEach(id -> onlinePayments.printStudentReport(id, System.out));
    }
}