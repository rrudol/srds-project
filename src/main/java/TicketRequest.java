import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class TicketRequest extends CassandraModel {
    private static final String TABLE_NAME = "TicketRequests";
    private int trainId;
    private int clientId;
    private int seats;
    private int roomsCount;
    private int roomCapacity;
    private long timestamp;

    public TicketRequest(Session session, int trainId, int clientId, int seats, int roomsCount, int roomCapacity) {
        super(session);
        this.trainId = trainId;
        this.clientId = clientId;
        this.seats = seats;
        this.roomsCount = roomsCount;
        this.roomCapacity = roomCapacity;
    }

    public TicketRequest(Session session, int trainId, int clientId, int seats, int roomsCount, int roomCapacity, long timestamp) {
        super(session);
        this.trainId = trainId;
        this.clientId = clientId;
        this.seats = seats;
        this.roomsCount = roomsCount;
        this.roomCapacity = roomCapacity;
        this.timestamp = timestamp;
    }

    public void save() {
        createTable(TABLE_NAME);

        StringBuilder sb = new StringBuilder("INSERT INTO ")
                .append(TABLE_NAME).append("(id, trainId, customerId, seats, timestamp) ")
                .append("VALUES (").append(UUID.randomUUID())
                .append(", ").append(trainId)
                .append(", ").append(clientId)
                .append(", ").append(seats)
                .append(", ").append(System.currentTimeMillis())
                .append(");");

        String query = sb.toString();
        execute(query);
    }

    public boolean isApproved(int clientId, int trainId) {
        List<TicketRequest> ticketRequests = getAllHappy(trainId);

        ArrayList<Integer> rooms = new ArrayList<>();

        for (int i = 0; i < roomsCount; i++) {
            rooms.add(roomCapacity);
        }

//        rooms.forEach(System.out::println);

        for (TicketRequest ticketRequest : ticketRequests) {
            boolean decision = false;
            for (int i = 0; i < roomsCount; i++) {
                int availableSeats = rooms.get(i);
                if (availableSeats >= ticketRequest.seats) {
                    rooms.set(i, availableSeats - ticketRequest.seats);
                    decision = true;
                    break;
                }
            }

//            System.out.println("-");
//            rooms.forEach(System.out::println);
//            System.out.println("-" + ticketRequest.clientId + " - " + clientId + " = "+ decision );

            if (ticketRequest.clientId == clientId) {
                return decision;
            }
        }
        return false;
    }

    public List<List<Integer>> giveTickets(int trainId) {
        List<TicketRequest> ticketRequests = getAllHappy(trainId);
//        List<Integer> tickets = new ArrayList<>();
        List<List<Integer>> tickets = new ArrayList<>();

        ArrayList<Integer> rooms = new ArrayList<>();

        for (int i = 0; i < roomsCount; i++) {
            rooms.add(roomCapacity);
            List<Integer> room = new ArrayList<>();
            tickets.add(room);
        }

        for (TicketRequest ticketRequest : ticketRequests) {
            for (int i = 0; i < roomsCount; i++) {
                int availableSeats = rooms.get(i);
                if (availableSeats >= ticketRequest.seats) {
                    rooms.set(i, availableSeats - ticketRequest.seats);
//                    System.out.println("+++ " + ticketRequest.seats);
                    tickets.get(i).add(ticketRequest.seats);
//                    tickets.add();
                    break;
                }
            }
        }
        return tickets;
    }

    public List<TicketRequest> getAllHappy(int trainId) {
        StringBuilder sb =
                new StringBuilder("SELECT * FROM ").append(TABLE_NAME).append(" WHERE trainId=").append(trainId);

        String query = sb.toString();
        ResultSet rs = execute(query);

        List<TicketRequest> ticketRequests = new ArrayList<>();

        rs.forEach(r -> {
            if (r.getInt("seats") > 0) {
                ticketRequests.add(new TicketRequest(
                        session,
                        r.getInt("trainId"),
                        r.getInt("customerId"),
                        r.getInt("seats"),
                        roomsCount, roomCapacity,
                        r.getLong("timestamp")));
            } else {
                for (int i = 0; i < ticketRequests.size(); i++) {
                    // Delete users if canceled
                    if (ticketRequests.get(i).clientId == r.getInt("customerId")) {
                        ticketRequests.remove(i);
                        break;
                    }
                }
            }
        });

        // Sort by Timestamp
        ticketRequests.sort(new Comparator<TicketRequest>() {
            @Override
            public int compare(TicketRequest m1, TicketRequest m2) {
                if (m1.timestamp == m2.timestamp) {
                    return 0;
                }
                return m1.timestamp > m2.timestamp ? -1 : 1;
            }
        });

        return ticketRequests;
    }

    public List<TicketRequest> getAll(int trainId) {
        StringBuilder sb =
                new StringBuilder("SELECT * FROM ").append(TABLE_NAME).append(" WHERE trainId=").append(trainId);

        String query = sb.toString();
        ResultSet rs = execute(query);

        List<TicketRequest> ticketRequests = new ArrayList<>();

        rs.forEach(r -> {
            if (r.getInt("seats") > 0) {
                ticketRequests.add(new TicketRequest(
                        session,
                        r.getInt("trainId"),
                        r.getInt("customerId"),
                        r.getInt("seats"),
                        roomsCount, roomCapacity,
                        r.getLong("timestamp")));
            }
        });

        // Sort by Timestamp
        ticketRequests.sort(new Comparator<TicketRequest>() {
            @Override
            public int compare(TicketRequest m1, TicketRequest m2) {
                if (m1.timestamp == m2.timestamp) {
                    return 0;
                }
                return m1.timestamp > m2.timestamp ? -1 : 1;
            }
        });

        return ticketRequests;
    }

    public int getSeats() {
        return seats;
    }
}
