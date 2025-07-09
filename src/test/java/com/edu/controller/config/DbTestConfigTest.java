package com.edu.controller.config;

import com.edu.controller.config.DbTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = DbTestConfig.class)
class DbTestConfigTest {

    @MockBean
    private DataSource dataSource;

    @MockBean
    private Connection connection;

    @MockBean
    private DatabaseMetaData metaData;

    @Test
    void testConnectionBeanRunsSuccessfully(ApplicationContext context) throws Exception {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn("jdbc:h2:mem:testdb");

        // Act
        Runnable runner = context.getBean(Runnable.class);
        assertDoesNotThrow(() -> runner.run());

        // Verify
        verify(dataSource).getConnection();
        verify(connection).getMetaData();
        verify(metaData).getURL();
    }
}
