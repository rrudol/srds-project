import com.datastax.driver.core.Session;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Stats {
    private static Stats INSTANCE;

    private final int ticketCost = 20;
    private final int refundCost = 100;

    private int happyClients = 0;
    private int sadClients = 0;
    private int cancelingClients = 0;
    private int angryClients = 0;

    private ArrayList<Integer> reservations = new ArrayList<>();
    private ArrayList<Integer> sad = new ArrayList<>();

    private Stats(){}

    public static Stats getInstance(){
        if(INSTANCE==null)
            INSTANCE = new Stats();
        return INSTANCE;
    }

    public void gotReservation(int id, int seats) {
        log(id, "I got reservation! {"+seats+"}");
    }

    public void go(int id, int seats) {
        reservations.add(seats);
        log(id, "Ready for a ride!");
        happyClients++;
    }

    public void cancel(int id) {
        log(id, "I cancel the ticket");
        cancelingClients++;
    }

    public void stay(int id, int seats) {
        sad.add(seats);
        log(id,"I can't go :(");
        sadClients++;
    }

    public void mistake(int id) {
        log(id,"This is my seat!");
        angryClients++;
    }

    public void showStats() {
        System.out.println("happyClients = "+happyClients);
        System.out.println("sadClients = "+sadClients);
        System.out.println("cancelingClients = "+cancelingClients);
        System.out.println("angryClients = "+angryClients);
        System.out.println("------\nHappy Clients:");
        reservations.forEach(r -> System.out.print("" + r + ", "));
        System.out.println("\n------");

        System.out.println("------\nSad Clients:");
        sad.forEach(r -> System.out.print("" + r + ", "));
        System.out.println("\n------");

    }

    public void showTickets(Session session, int trainId, int roomsCount, int roomCapacity) {
        TicketRequest ticketRequest = new TicketRequest(session, trainId, 0, 0, roomsCount, roomCapacity);
        System.out.println("------");
        List<List<Integer>> tickets = ticketRequest.giveTickets(trainId);

        for (int i = 0; i < tickets.size(); i++) {
            List<Integer> room = tickets.get(i);
            System.out.println("Room #"+i + " max="+roomCapacity+"");
            for (Integer c : room) {
                System.out.print("["+c+"], ");
            }
            System.out.println("\n----");
        }

//        for (List<Integer> room : ticketRequest.giveTickets(trainId)) {
//
//        }
        System.out.println("Stream of clients: ");
        ticketRequest.getAll(trainId).forEach(c -> System.out.print("" + c.getSeats() + ", "));
        System.out.println("-");
//        ticketRequest.giveTickets(trainId).forEach(room -> room.forEach(s -> System.out.print("" + s + ", ")));
    }

    public void showMoney() {
        System.out.println("We earned "+happyClients * ticketCost + "$");
        System.out.println("We lost "+angryClients * refundCost + "$");
        System.out.println("Score "+(happyClients * ticketCost - angryClients * refundCost)  + "$");
    }

    private void log(int id, String message) {
        System.out.println("[" + id + "] " + message);
    }
}
