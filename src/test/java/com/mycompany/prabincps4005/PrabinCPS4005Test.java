package com.mycompany.prabincps4005;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class PrabinCPS4005Test {
  private PrabinCPS4005 prabinCPS4005;
  private Scanner mockScanner;
  private DbConn mockConnection;

  @BeforeEach
  public void setUp() {
    mockScanner = mock(Scanner.class);
    mockConnection = mock(DbConn.class);
    prabinCPS4005 = new PrabinCPS4005(mockScanner, mockConnection);
  }

  @Test
  public void testCreateClient() throws SQLException {
    when(mockScanner.nextLine())
            .thenReturn("John Doe")
            .thenReturn("123 Main St")
            .thenReturn("555-1234")
            .thenReturn("john@example.com");

    prabinCPS4005.createClient();

    verify(mockConnection, times(1)).createClient("John Doe", "123 Main St", "555-1234", "john@example.com");
  }

  @Test
  public void testViewClient() throws SQLException {
    when(mockScanner.nextInt()).thenReturn(1);
    when(mockScanner.nextLine()).thenReturn("");

    Client mockClient = new Client(1, "John Doe", "123 Main St", "555-1234", "john@example.com");
    when(mockConnection.viewClient(1)).thenReturn(mockClient);

    prabinCPS4005.viewClient();

    verify(mockScanner, times(1)).nextInt();
    verify(mockScanner, times(1)).nextLine();
    verify(mockConnection, times(1)).viewClient(1);
  }

  @Test
  public void testViewAllCase() throws SQLException, InterruptedException {
    Date dateFiled1 = Date.valueOf("2022-01-01");
    Date dateClosed1 = Date.valueOf("2022-01-10");
    Date dateFiled2 = Date.valueOf("2022-02-01");
    Date dateClosed2 = Date.valueOf("2022-02-10");

    Client client1 = new Client(1, "John Doe", "123 Main St", "555-1234", "john@example.com");
    Client client2 = new Client(2, "Jane Doe", "456 Elm St", "555-5678", "jane@example.com");

    Case mockCase1 = new Case(1, "Case001", "Title1", "Description1", "Open", dateFiled1, dateClosed1, client1);
    Case mockCase2 = new Case(2, "Case002", "Title2", "Description2", "Closed", dateFiled2, dateClosed2, client2);

    when(mockConnection.getAllCases()).thenReturn(Arrays.asList(mockCase1, mockCase2));

    ExecutorService executorService = Executors.newFixedThreadPool(5);

    for (int i = 0; i < 5; i++) {
      executorService.execute(() -> prabinCPS4005.viewAllCase());
    }

    executorService.shutdown();
    executorService.awaitTermination(1, TimeUnit.MINUTES);

    verify(mockConnection, times(5)).getAllCases();
  }
}
