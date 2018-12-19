import com.datastax.driver.core.*;


public class CassandraModel {
    protected Session session;

    public CassandraModel(Session session) {
        this.session = session;
    }

    public void createTable(String tableName) {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(tableName).append("(")
                .append("id uuid, ")
                .append("trainId int,")
                .append("customerId int,")
                .append("seats int,")
                .append("timestamp bigint,")
                .append("PRIMARY KEY (trainId, id));");

        String query = sb.toString();
        session.execute(query);

//        execute("CREATE INDEX IF NOT EXISTS trainIndex ON "+tableName+" (trainId);");
    }

    public ResultSet execute(String query) {;
        return session.execute(query);
    }

    public ResultSet executeQuorum(String query) {
        Statement statement = new SimpleStatement(query).setConsistencyLevel(ConsistencyLevel.QUORUM);
        return session.execute(statement);
    }
}
