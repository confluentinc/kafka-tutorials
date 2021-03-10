//Enable transactions
db.setAutoCommit(false);

try{
   //insert into the DB
   sql = db.prepareStatement("insert mydb.events values (?)");
   sql.setString(event.toString());
   sql.executeUpdate();

   //insert into Kafka
   producer.send(event.key(), event.value());

   //commit to the DB
   db.commit();
} catch (SQLException e ) {
   db.rollback();
}
