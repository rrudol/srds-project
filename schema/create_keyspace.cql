CREATE KEYSPACE IF NOT EXISTS Test
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

USE Test;