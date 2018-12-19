import CassandraBackend.Backend;

public class Main {
    public static void main(String[] args) {
        final int trainId = -15;
        final int roomCount = 100;
        final int roomCapacity = 6;
        final int clientCount = 200;
        final int simulationTime = 100;
        final int checkDelay = 100; // Depends on system overload

        Backend backend = new Backend("config.properties");

        Train train = new Train(backend, trainId, roomCount, roomCapacity);

        Client.spawnAndSendToTrain(clientCount, train, 1000, simulationTime, checkDelay, roomCapacity);

        Stats.getInstance().showStats();
//        Stats.getInstance().showTickets(backend.getSession(), train.getId(), train.getRoomsCount(), train.getRoomCapacity());
    }
}
