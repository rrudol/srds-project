CREATE KEYSPACE IF NOT EXISTS Test
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

USE Test;

CREATE TABLE TicketRequest (
  trainId int,
  customerId int,
  seets int,
  timestamp int,
  PRIMARY KEY (trainId)
);