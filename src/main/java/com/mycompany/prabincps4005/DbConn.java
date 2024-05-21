package com.mycompany.prabincps4005;


import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DbConn {

    private Connection connection;

    public DbConn() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:assignmentcps4005.db");
            dbIntializeAndPopulate();
        } catch (SQLException e) {
            System.out.println("Error while connecting to database" + e.getMessage());
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found: " + ex.getMessage());
        }
    }

    private void dbIntializeAndPopulate() {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS clients (client_id INTEGER PRIMARY KEY, client_name TEXT NOT NULL, client_address TEXT, client_phone TEXT, client_email TEXT)");
            st.execute("CREATE TABLE IF NOT EXISTS cases (case_id INTEGER PRIMARY KEY, case_number TEXT UNIQUE, case_title TEXT NOT NULL, case_description TEXT, case_status TEXT, date_filed TEXT, date_closed TEXT, client_id INTEGER, FOREIGN KEY (client_id) REFERENCES clients(client_id) ON DELETE SET NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS documents (document_id INTEGER PRIMARY KEY, case_id INTEGER, document_name TEXT NOT NULL, document_type TEXT, document_path TEXT, FOREIGN KEY (case_id) REFERENCES cases(case_id) ON DELETE CASCADE)");
            st.execute("CREATE TABLE IF NOT EXISTS important_dates (date_id INTEGER PRIMARY KEY, case_id INTEGER, event_date TEXT, event_description TEXT, FOREIGN KEY (case_id) REFERENCES cases(case_id) ON DELETE CASCADE)");
            populateDataIntoTables();
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
        }
    }

    private void populateDataIntoTables() {
        try (Statement st = connection.createStatement()) {
            // Populate clients table
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM clients")) {
                if (rs.getInt(1) == 0) {
                    st.execute("INSERT INTO clients (client_name, client_address, client_phone, client_email) VALUES "
                            + "('John Doe', '123 Main St, Anytown', '555-1234', 'john.doe@example.com'),"
                            + "('Jane Smith', '456 Oak St, Anycity', '555-5678', 'jane.smith@example.com')");
                }
            }

            // Populate cases table
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM cases")) {
                if (rs.getInt(1) == 0) {
                    st.execute("INSERT INTO cases (case_number, case_title, case_description, case_status, date_filed, date_closed, client_id) VALUES "
                            + "('CASE001', 'Trademark Infringement', 'Trademark infringement against XYZ Corp.', 'Open', '1673720100000', '1673720100000', 1),"
                            + "('CASE002', 'Patent Dispute', 'Patent dispute regarding new technology', 'Closed', '1679249700000', '1691604900000', 2)");
                }
            }

            // Populate documents table
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM documents")) {
                if (rs.getInt(1) == 0) {
                    st.addBatch("INSERT INTO documents (case_id, document_name, document_type, document_path) VALUES "
                            + "(1, 'Complaint', 'Legal', 'complaint.txt'),"
                            + "(1, 'Evidence', 'Legal', 'evidence.txt'),"
                            + "(2, 'Settlement Agreement', 'Legal', 'settlement_agreement.txt')");
                    st.executeBatch();
                }
            }

            // Populate important_dates table
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM important_dates")) {
                if (rs.getInt(1) == 0) {
                    st.addBatch("INSERT INTO important_dates (case_id, event_date, event_description) VALUES "
                            + "(1, '1673720100000', 'Date filed'),"
                            + "(1, '1675966500000', 'Pre-trial conference'),"
                            + "(1, '1683224100000', 'Court hearing'),"
                            + "(2, '1679249700000', 'Date filed'),"
                            + "(2, '1686766500000', 'Mediation session')");
                    st.executeBatch();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error populating data into tables: " + e.getMessage());
        }
    }

    public void dbClose() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing the database conenction" + e.getMessage());
        }
    }

    //parseDate
    private Date parseDate(String dateString) {
        try {
            return new Date(Long.parseLong(dateString));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    //authentication
  public boolean authenticateUser(String name, String address) throws SQLException {
    boolean isAuth = false;
    String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, name);
      pstmt.setString(2, address);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        isAuth=true;
      }else
        isAuth=false;
    }
    return isAuth;
  }
    
    //client connection with db
    public void createClient(String name, String address, String phone, String email) throws SQLException {
        String sql = "insert into clients(client_name,client_address, client_phone, client_email) values(?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, address);
            pstmt.setString(3, phone);
            pstmt.setString(4, email);
            pstmt.executeUpdate();
        }
    }

    public Client viewClient(int id) throws SQLException {
        String sql = "select * from clients where client_id =?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Client(rs.getInt("client_id"), rs.getString("client_name"),
                        rs.getString("client_address"), rs.getString("Client_phone"),
                        rs.getString("client_email"));
            }
        }
        return null;
    }

    public List<Client> getAllClients() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "select * from clients";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("client_id");
                String name = rs.getString("client_name");
                String address = rs.getString("client_address");
                String phone = rs.getString("client_phone");
                String email = rs.getString("client_email");
                clients.add(new Client(id, name, address, phone, email));
            }
        }
        return clients;
    }

    public void updateClient(int id, String newName, String newAddress, String newPhone, String newEmail) throws SQLException {
        String sql = "update clients set client_name = ?, client_address = ?,client_phone = ?,client_email = ? where client_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, newAddress);
            pstmt.setString(3, newPhone);
            pstmt.setString(4, newEmail);
            pstmt.setInt(5, id);
            pstmt.executeUpdate();
        }
    }

    public void deleteClient(int id) throws SQLException {
        String sql = "delete from clients where client_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    //case connection with db
    public void createCase(String number, String title, String description, String status, String dateFiled, String dateClosed, int clientId) throws SQLException {
        String sql = "INSERT INTO cases (case_number, case_title, case_description, case_status, date_filed, date_closed, client_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, number);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setString(4, status);
            pstmt.setDate(5, java.sql.Date.valueOf(dateFiled));
            pstmt.setDate(6, java.sql.Date.valueOf(dateClosed));
            pstmt.setInt(7, clientId);
            pstmt.executeUpdate();
        }
    }

    public Case viewCase(int id) throws SQLException {
        String sql = "select * from cases where case_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int caseId = rs.getInt("case_id");
                String caseNumber = rs.getString("case_number");
                String caseTitle = rs.getString("case_title");
                String caseDescription = rs.getString("case_description");
                String caseStatus = rs.getString("case_status");

                String dateFiledText = rs.getString("date_filed");
                String dateClosedText = rs.getString("date_closed");

                Date dateFiled = parseDate(dateFiledText);
                Date dateClosed = parseDate(dateClosedText);

                int clientId = rs.getInt("client_id");
                Client client = viewClient(clientId);
                return new Case(caseId, caseNumber, caseTitle, caseDescription, caseStatus, dateFiled, dateClosed, client);
            }
        }
        return null;
    }

    public List<Case> getAllCases() throws SQLException {
        List<Case> cases = new ArrayList<>();
        String sql = "select * from cases";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int caseId = rs.getInt("case_id");
                String caseNumber = rs.getString("case_number");
                String caseTitle = rs.getString("case_title");
                String caseDescription = rs.getString("case_description");
                String caseStatus = rs.getString("case_status");

                String dateFiledText = rs.getString("date_filed");
                String dateClosedText = rs.getString("date_closed");

                Date dateFiled = parseDate(dateFiledText);
                Date dateClosed = parseDate(dateClosedText);

                int clientId = rs.getInt("client_id");
                Client client = viewClient(clientId);
                cases.add(new Case(caseId, caseNumber, caseTitle, caseDescription, caseStatus, dateFiled, dateClosed, client));
            }
        }
        return cases;
    }

    public void updateCase(int id, String newCaseNumber, String newCaseTitle, String newCaseDescription, String newCaseStatus, Date newDateFiled, Date newDateClosed) throws SQLException {
        String sql = "UPDATE cases SET case_number = ?, case_title = ?, case_description = ?, case_status = ?, date_filed = ?, date_closed = ? WHERE case_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newCaseNumber);
            pstmt.setString(2, newCaseTitle);
            pstmt.setString(3, newCaseDescription);
            pstmt.setString(4, newCaseStatus);
            pstmt.setDate(5, new java.sql.Date(newDateFiled.getTime()));
            pstmt.setDate(6, new java.sql.Date(newDateClosed.getTime()));
            pstmt.setInt(7, id);
            pstmt.executeUpdate();
        }
    }

    public void deleteCase(int id) throws SQLException {
        String sql = "DELETE FROM cases WHERE case_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    //date case
    public void createDate(int caseId, Date eventDate, String eventDescription) throws SQLException {
        String sql = "INSERT INTO important_dates (case_id, event_date, event_description) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, caseId);
            pstmt.setDate(2, new java.sql.Date(eventDate.getTime()));
            pstmt.setString(3, eventDescription);
            pstmt.executeUpdate();
        }
    }

    public void updateDate(int dataId, int caseId, Date eventDate, String eventDescription) throws SQLException {
        String sql = "UPDATE important_dates SET case_id = ?, event_date = ?, event_description = ? WHERE date_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, caseId);
            pstmt.setDate(2, new java.sql.Date(eventDate.getTime()));
            pstmt.setString(3, eventDescription);
            pstmt.setInt(4, dataId);
            pstmt.executeUpdate();
        }
    }

    public ImportantDates viewDate(int dataId) throws SQLException {
        String sql = "SELECT * FROM important_dates WHERE date_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, dataId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Date eventDate = parseDate(rs.getString("event_date"));
                String eventDescription = rs.getString("event_description");
                int caseId = rs.getInt("case_id");
                Case clientCase = viewCase(caseId);
                return new ImportantDates(dataId, clientCase, eventDate, eventDescription);
            }
        }
        return null;
    }

    public List<ImportantDates> viewAllDate() throws SQLException {
        List<ImportantDates> dataList = new ArrayList<>();
        String sql = "SELECT * FROM important_dates";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int dataId = rs.getInt("date_id");
                int caseId = rs.getInt("case_id");
                Case clientCase = viewCase(caseId);
                Date eventDate = parseDate(rs.getString("event_date"));
                String eventDescription = rs.getString("event_description");
                dataList.add(new ImportantDates(dataId, clientCase, eventDate, eventDescription));
            }
        }
        return dataList;
    }

    public void deleteDate(int dataId) throws SQLException {
        String sql = "DELETE FROM important_dates WHERE date_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, dataId);
            pstmt.executeUpdate();
        }
    }

    //document
    public void createDocument(int caseId, String documentName, String documentType, String documentPath) throws SQLException {
        String sql = "INSERT INTO documents (case_id, document_name, document_type, document_path) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, caseId);
            pstmt.setString(2, documentName);
            pstmt.setString(3, documentType);
            pstmt.setString(4, documentPath);
            pstmt.executeUpdate();
        }
    }

    public void updateDocument(int documentId, int caseId, String documentName, String documentType, String documentPath) throws SQLException {
        String sql = "UPDATE documents SET case_id = ?, document_name = ?, document_type = ?, document_path = ? WHERE document_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, caseId);
            pstmt.setString(2, documentName);
            pstmt.setString(3, documentType);
            pstmt.setString(4, documentPath);
            pstmt.setInt(5, documentId);
            pstmt.executeUpdate();
        }
    }

    public Documents viewDocument(int documentId) throws SQLException {
        String sql = "SELECT * FROM documents WHERE document_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, documentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int caseId = rs.getInt("case_id");
                Case clientCase = viewCase(caseId);
                String documentName = rs.getString("document_name");
                String documentType = rs.getString("document_type");
                String documentPath = rs.getString("document_path");
                return new Documents(documentId, clientCase, documentName, documentType, documentPath);
            }
        }
        return null;
    }

    public List<Documents> viewAllDocuments() throws SQLException {
        List<Documents> documentList = new ArrayList<>();
        String sql = "SELECT * FROM documents";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int documentId = rs.getInt("document_id");
                int caseId = rs.getInt("case_id");
                Case clientCase = viewCase(caseId);
                String documentName = rs.getString("document_name");
                String documentType = rs.getString("document_type");
                String documentPath = rs.getString("document_path");
                documentList.add(new Documents(documentId, clientCase, documentName, documentType, documentPath));
            }
        }
        return documentList;
    }

    public void deleteDocument(int documentId) throws SQLException {
        String sql = "DELETE FROM documents WHERE document_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, documentId);
            pstmt.executeUpdate();
        }
    }

}
