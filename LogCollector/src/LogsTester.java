import java.util.*;

class LogFactory {
    public static Log generateLogFromString(String entry) {
        String [] entrySplit = entry.split(" ");

        String serviceName = entrySplit[0];
        String microServiceName = entrySplit[1];
        String type = entrySplit[2];
        int timestamp = Integer.parseInt(entrySplit[entrySplit.length - 1]);

        StringBuilder sb = new StringBuilder();

        for(int i = 3; i < entrySplit.length - 1; i++) {
            sb.append(entrySplit[i]).append(" ");
        }

        switch (type) {
            case "INFO": return new InfoLog(serviceName, microServiceName, sb.toString().trim(), timestamp);
            case "WARN": return new WarningLog(serviceName, microServiceName, sb.toString().trim(), timestamp);
            case "ERROR": return new ErrorLog(serviceName, microServiceName, sb.toString().trim(), timestamp);
        }

        return null;
    }
}

class LogComparatorFactory {
    public static Comparator<Log> generateComparator(String type) {
        switch (type) {
            case "NEWEST_FIRST": return oldestComparator().reversed();
            case "OLDEST_FIRST": return oldestComparator();
            case "MOST_SEVERE_FIRST": return leastSevere().thenComparing(oldestComparator()).reversed();
            case "LEAST_SEVERE_FIRST": return leastSevere().thenComparing(oldestComparator());
        }

        return null;
    }
    private static Comparator<Log> leastSevere() {
        return Comparator.comparingInt(Log::getSeverity);
    }

    private static Comparator<Log> oldestComparator() {
        return Comparator.comparingInt(Log::getTimestamp);
    }
}

abstract class Log {
    String serviceName;
    String microServiceName;
    String message;
    int timestamp;

    public Log(String serviceName, String microServiceName, String message, int timestamp) {
        this.serviceName = serviceName;
        this.microServiceName = microServiceName;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMicroServiceName() {
        return microServiceName;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public abstract String toString();
    public abstract int getSeverity();
}

class InfoLog extends Log {
    public InfoLog(String serviceName, String microServiceName, String message, int timestamp) {
        super(serviceName, microServiceName, message, timestamp);
    }

    @Override
    public String toString() {
        return serviceName
                + "|" + microServiceName
                + " [INFO] " + message + " "
                + timestamp + " T:" + timestamp;
    }

    @Override
    public int getSeverity() {
        return 0;
    }
}

class WarningLog extends Log {

    public WarningLog(String serviceName, String microServiceName, String message, int timestamp) {
        super(serviceName, microServiceName, message, timestamp);
    }

    @Override
    public String toString() {
        return serviceName
                + "|" + microServiceName
                + " [WARN] " + message + " "
                + timestamp + " T:" + timestamp;
    }

    @Override
    public int getSeverity() {
        if(message.contains("might cause error"))
            return 2;

        return 1;
    }
}

class ErrorLog extends Log {

    public ErrorLog(String serviceName, String microServiceName, String message, int timestamp) {
        super(serviceName, microServiceName, message, timestamp);
    }

    @Override
    public String toString() {
        return serviceName
                + "|" + microServiceName
                + " [ERROR] " + message + " "
                + timestamp + " T:" + timestamp;
    }

    @Override
    public int getSeverity() {
        int severity = 3;

        if(message.contains("fatal"))
            severity += 2;

        if(message.contains("exception"))
            severity += 3;

        return severity;
    }
}

class Service implements Comparable<Service>{
    String name;
    Set<String> microservices;
    int logsCount;
    int totalSeverity;
    Map<String, Set<Log>> microservicesLogs;

    public Service(String name) {
        this.name = name;

        this.microservices = new HashSet<>();
        this.microservicesLogs = new TreeMap<>();

        this.logsCount = 0;
        this.totalSeverity = 0;
    }

    public void addLog(Log log) {
        String micro = log.getMicroServiceName();
        microservices.add(micro);

        microservicesLogs.putIfAbsent(micro, new HashSet<>());
        microservicesLogs.get(micro).add(log);

        logsCount++;
        totalSeverity += log.getSeverity();
    }

    public float getAverageSeverity() {
        return (float)(totalSeverity)/logsCount;
    }


    @Override
    public String toString() {
       StringBuilder sb = new StringBuilder("Service name: ");

       sb.append(name);
       sb.append(" Count of microservices: ").append(microservices.size());
       sb.append(" Total logs in service: ").append(logsCount);

       sb.append(" Average severity for all logs: ");
       sb.append(String.format("%.2f", getAverageSeverity()));

       sb.append(" Average number of logs per microservice: ");
       sb.append(String.format("%.2f", (float)(logsCount)/ microservices.size()));

       return sb.toString();
    }

    @Override
    public int compareTo(Service o) {
        return Float.compare(getAverageSeverity(), o.getAverageSeverity());
    }
}

class LogCollector {
    ArrayList<Log> logger;

    public LogCollector() {
        logger = new ArrayList<>();
    }

    public void addLog(String log) {
        Log toAdd = LogFactory.generateLogFromString(log);
        logger.add(toAdd);
    }

    public void printServicesBySeverity() {
        Map<String, Service> services = new TreeMap<>();

        for(Log entry : logger) {
            String service = entry.getServiceName();

            services.putIfAbsent(service, new Service(service));
            services.get(service).addLog(entry);
        }

        PriorityQueue<Service> pq = new PriorityQueue<>(Comparator.reverseOrder());

        pq.addAll(services.values());

        while (!pq.isEmpty()) {
            System.out.println(pq.poll());
        }
    }

    public Map<Integer, Integer> getSeverityDistribution (String service, String microservice) {
        Map<Integer, Integer> result = new TreeMap<>();

        for(Log entry : logger) {
            if(! entry.getServiceName().equals(service)) continue;
            if(microservice == null || entry.getMicroServiceName().equals(microservice)) {
                int severity = entry.getSeverity();

                result.putIfAbsent(severity, 0);
                result.put(severity, result.get(severity) + 1);
            }
        }

        return result;
    }

    public void displayLogs(String service, String microservice, String order) {
        PriorityQueue<Log> pq = new PriorityQueue<>(LogComparatorFactory.generateComparator(order));

        for(Log entry : logger) {
            if(! entry.getServiceName().equals(service)) continue;
            if(microservice == null || entry.getMicroServiceName().equals(microservice)) {
                pq.add(entry);
            }
        }

        System.out.print("displayLogs " + service);
        if(microservice != null)
            System.out.print(" " + microservice);
        System.out.println(" " + order);


        while (! pq.isEmpty()) {
            System.out.println(pq.poll());
        }
    }
}


public class LogsTester {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        LogCollector collector = new LogCollector();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.startsWith("addLog")) {
                collector.addLog(line.replace("addLog ", ""));
            } else if (line.startsWith("printServicesBySeverity")) {
                collector.printServicesBySeverity();
            } else if (line.startsWith("getSeverityDistribution")) {
                String[] parts = line.split("\\s+");
                String service = parts[1];
                String microservice = null;
                if (parts.length == 3) {
                    microservice = parts[2];
                }
                collector.getSeverityDistribution(service, microservice).forEach((k,v)-> System.out.printf("%d -> %d%n", k,v));
            } else if (line.startsWith("displayLogs")){
                String[] parts = line.split("\\s+");
                String service = parts[1];
                String microservice = null;
                String order = null;
                if (parts.length == 4) {
                    microservice = parts[2];
                    order = parts[3];
                } else {
                    order = parts[2];
                }
                collector.displayLogs(service, microservice, order);
            }
        }
    }
}