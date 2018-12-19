import CassandraBackend.Backend;
import com.datastax.driver.core.Session;

public class Train {
    private int id;
    private Session session;

    private int roomsCount;
    private int roomCapacity;

    public Train(Backend backend, int trainId, int roomsCount, int roomCapacity) {
        this.session = backend.getSession();
        this.roomsCount = roomsCount;
        this.roomCapacity = roomCapacity;
        id = trainId;
    }

    public int getId() {
        return id;
    }

    public int getRoomCapacity() {
        return roomCapacity;
    }

    public int getRoomsCount() {
        return roomsCount;
    }

    public boolean buyTicket(int clientId, int seats, int checkDelay) {
        TicketRequest ticketRequest = new TicketRequest(session, id, clientId, seats, roomsCount, roomCapacity);
        ticketRequest.save();
        try {
            Thread.sleep(checkDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ticketRequest.isApproved(clientId, id);
    }

    public void cancelTicket(int clientId) {
        TicketRequest ticketRequest = new TicketRequest(session, id, clientId, -1, roomsCount, roomCapacity);
        ticketRequest.save();
    }

    public boolean go(int clientId) {
        TicketRequest ticketRequest = new TicketRequest(session, id, clientId, 0, roomsCount, roomCapacity);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ticketRequest.isApproved(clientId, id);
    }
}
