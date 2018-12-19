package CassandraBackend;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;

import java.io.IOException;
import java.util.Properties;

public class Backend {
    private Session session;

    public Backend(String configFilename) {
        Properties properties = new Properties();
        try {
            properties.load(Backend.class.getClassLoader().getResourceAsStream(configFilename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String contactPoint = properties.getProperty("contact_point");
        String keyspace = properties.getProperty("keyspace");

        Cluster cluster = Cluster.builder().addContactPoint(contactPoint).withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.ONE)).build();

        session = cluster.connect();
        StringBuilder sb =
                new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ")
                        .append("test").append(" WITH replication = {")
                        .append("'class':'").append("SimpleStrategy")
                        .append("','replication_factor':").append(3)
                        .append("};");

        String query = sb.toString();
        session.execute(query);

        try {
            session = cluster.connect(keyspace);
        } catch (Exception e) {
            System.out.println("ERR: Could not connect to the cluster " + e.getMessage() + ".");
        }
    }

    public Session getSession() {
        return session;
    }
}
