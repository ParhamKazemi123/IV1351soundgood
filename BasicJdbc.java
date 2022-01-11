/*
 * The MIT License (MIT)
 * Copyright (c) 2020 Leif Lindbäck
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction,including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so,subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package se.kth.iv1351.jdbcintro;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Scanner;


/**
 * A small program that illustrates how to write a simple JDBC program.
 */
public class BasicJdbc {
  private static final String TABLE_NAME = "contact_datais";
  private static final String TABLE_STOCK = "instrument_stock";
  private PreparedStatement listInstrumentStmt;
  private PreparedStatement numberRentedInstrumentsStmt;
  private PreparedStatement insertRentalStmt;
  private PreparedStatement terminateRentalStmt;
  // private PreparedStatement idCounter;
  

  private void accessDB() {
    try (Connection connection = createConnection()) {
      connection.setAutoCommit(false);
      createTable(connection);
      prepareStatements(connection);
         boolean isActive = true;
      while (isActive){
      
      Scanner scanner = new Scanner(System.in);
      
        System.out.println("\n\n1. list instrument by type\n2. rent instrument\n3. terminate rental\n");
      int answer = scanner.nextInt();
   
      switch(answer) {
        case 1:
          System.out.println("What instrument?");
          String instrument = scanner.next();
          //skriv saxofon
          listInstrumentStmt.setString(1, instrument);
          listAllInstruments();
          break;
        case 2:
            System.out.println("What is your student id?");
            //svara 1
            int student_id = scanner.nextInt();
            System.out.println("What instrument id?");
            //svara 7
            int instrument_id = scanner.nextInt();
            int staff_id = 1;
            java.sql.Timestamp returndate = java.sql.Timestamp.valueOf("2022-09-23 10:10:10.0");
            
            //int id = getInt(idCounter);
            //System.out.println(id);
            
            
            //student_id, rental_start_date, rental_end_date, staff_id, instrument_id
            numberRentedInstrumentsStmt.setInt(1,student_id);
            //insertRentalStmt.setInt(1, new_id);
            insertRentalStmt.setInt(1, student_id);
            insertRentalStmt.setTimestamp(2, getReturnDate());
            insertRentalStmt.setTimestamp(3, returndate);
            insertRentalStmt.setInt(4, staff_id);
            insertRentalStmt.setInt(5, instrument_id);
           
            rentInstrument(connection);
            
            break;
        case 3:
          System.out.println("What is your student id?");
          //svara 1
          int student_idTerminate = scanner.nextInt();
          System.out.println("What is the rental id to be terminated?");
          //svara 21
          int rental_idTerminate = scanner.nextInt();
        
          terminateRentalStmt.setInt(1,student_idTerminate);
          terminateRentalStmt.setInt(2, rental_idTerminate);
          terminateInstrument(connection);
          break;
          
        case 4:  
            System.out.println("Good_Bye");
            isActive = false;
            break;

           
      }
      
      
      }
      
    } catch (SQLException | ClassNotFoundException exc) {
      exc.printStackTrace();
    }
  }
    
    
   private Timestamp getReturnDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(System.currentTimeMillis()));
        c.add(Calendar.MONTH, 1);
        return new Timestamp(c.getTimeInMillis());
    }
   /*
  private Timestamp setDate () throws ParseException{
      
       DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
       Date date = (Date) dateFormat.parse("23/09/2022");
        long time = date.getTime();
        new Timestamp(time);
      return time;
   
      
      
  }*/
   

  private Connection createConnection() throws SQLException, ClassNotFoundException {
    Class.forName("org.postgresql.Driver");
    return DriverManager.getConnection("jdbc:postgresql://localhost:5432/soundgoodfinal",
      "postgres", "1234");
    // Class.forName("com.mysql.cj.jdbc.Driver");
    // return DriverManager.getConnection(
    // "jdbc:mysql://localhost:3306/simplejdbc?serverTimezone=UTC",
    // "root", "javajava");
  }

   private void createTable(Connection connection) {
    try (Statement stmt = connection.createStatement()) {
      if (!tableExists(connection)) {
        stmt.executeUpdate(
            "create table " + TABLE_NAME + " (name varchar(32) primary key, phone varchar(12), age int)");
      }
//vad är detta ovan
    } catch (SQLException sqle) {
      sqle.printStackTrace();
    }
  }

  private boolean tableExists(Connection connection) throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();
    ResultSet tableMetaData = metaData.getTables(null, null, null, null);
    while (tableMetaData.next()) {
      String tableName = tableMetaData.getString(3);
      if (tableName.equalsIgnoreCase(TABLE_NAME)) {
        return true;
      }
    }
    return false;
  }

   private void listAllInstruments() {
    try (ResultSet instruments = listInstrumentStmt.executeQuery()) {
      while(instruments.next()) {
        System.out.println("instrument_id: " + instruments.getString(1) + ", type: " + instruments.getString(2) + ", quantity: " + instruments.getInt(3) + ", brand: " + instruments.getString(4));
      }
    } catch (SQLException sqle) {
      sqle.printStackTrace();
    }
  }
   
    private void rentInstrument(Connection connection) throws SQLException {
    boolean statement = true;
    try (ResultSet instruments_rented = numberRentedInstrumentsStmt.executeQuery()) {
      while(instruments_rented.next() && statement) {
        System.out.println("INSTRUMENTS RENTED:" + instruments_rented.getInt(2));

        if (instruments_rented.getInt(2) <= 1) {
          System.out.println("TEST TEST ");
          insertRentalStmt.executeUpdate();
          connection.commit();
          statement = false;
          System.out.println("You have now rented an instrument!");
        } else {
          System.out.println("Can't rent, student has reached maximum rental already.");
          statement = false;
        }
      }
      if(statement) {
        insertRentalStmt.executeUpdate();
        connection.commit();
      }
    } catch (SQLException e) {
      e.printStackTrace();
      connection.rollback();
    }
  }

     /**
   * Will set rental_end_date as NULL (terminated) for the specified rental_id
   * @throws SQLException sql exception
   */
  private void terminateInstrument(Connection connection) throws SQLException {
    try {
      terminateRentalStmt.executeUpdate();
      connection.commit();
      System.out.println("Specific rental terminated");
    } catch(SQLException e) {
      e.printStackTrace();
      connection.rollback();
    }
  }
    
  private void prepareStatements(Connection connection) throws SQLException {
   // createPersonStmt = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?)");
   // findAllPersonsStmt = connection.prepareStatement("SELECT * from " + TABLE_NAME);
   // deletePersonStmt = connection.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE name = ?");
    listInstrumentStmt = connection.prepareStatement("SELECT * FROM " + TABLE_STOCK + " WHERE type = ?");
    numberRentedInstrumentsStmt = connection.prepareStatement("SELECT student_id, COUNT(student_id) AS rented_instruments FROM renting_instruments WHERE student_id = ? GROUP BY student_id");
    //idCounter = connection.prepareStatement("SELECT COUNT(id) FROM renting_instruments ");
    insertRentalStmt = connection.prepareStatement("INSERT INTO renting_instruments (  student_id, start_date, end_date, staff_id ,instrument_id) VALUES( ?, ?, ? , ?, ?)");
    terminateRentalStmt = connection.prepareStatement("UPDATE renting_instruments SET end_date = CURRENT_TIMESTAMP WHERE student_id = ? AND id = ?");
  }

  public static void main(String[] args) {
    new BasicJdbc().accessDB();
  }
}