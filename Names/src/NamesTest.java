import java.util.*;

class Names {
    HashMap<String, Integer> namesCounter;

    public Names() {
        namesCounter = new HashMap<>();
    }

    public void addName(String name) {
        namesCounter.putIfAbsent(name, 0);
        namesCounter.put(name, namesCounter.get(name) + 1);
    }

    public void printN(int n) {
        ArrayList<String> filteredNames = new ArrayList<>();

        for(String name : namesCounter.keySet()) {
            if(namesCounter.get(name) < n) continue;

            filteredNames.add(name);
        }

        filteredNames.sort(Comparator.naturalOrder());

        for (String name : filteredNames) {
            Set<Character> letters = new HashSet<>();

            for(Character c : name.toCharArray())
                letters.add(Character.toLowerCase(c));

            System.out.printf("%s (%d) %d\n",
                    name,
                    namesCounter.get(name),
                    letters.size()
            );
        }
    }

    public String findName(int len, int x) {
        ArrayList<String> filteredNames = new ArrayList<>();

        for(String name : namesCounter.keySet()) {
            if(name.length() >= len) continue;

            filteredNames.add(name);
        }

        filteredNames.sort(Comparator.naturalOrder());

        return filteredNames.get(x % filteredNames.size());
    }
}


public class NamesTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        scanner.nextLine();
        Names names = new Names();
        for (int i = 0; i < n; ++i) {
            String name = scanner.nextLine();
            names.addName(name);
        }
        n = scanner.nextInt();
        System.out.printf("===== PRINT NAMES APPEARING AT LEAST %d TIMES =====\n", n);
        names.printN(n);
        System.out.println("===== FIND NAME =====");
        int len = scanner.nextInt();
        int index = scanner.nextInt();
        System.out.println(names.findName(len, index));
        scanner.close();

    }
}