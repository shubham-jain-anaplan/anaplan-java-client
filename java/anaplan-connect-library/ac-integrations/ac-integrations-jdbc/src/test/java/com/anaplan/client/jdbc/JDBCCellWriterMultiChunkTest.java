package com.anaplan.client.jdbc;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anaplan.client.CellWriter.DataRow;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.opencsv.CSVParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JDBCCellWriterMultiChunkTest {

  private static final String testJdbcQueryProperties = "/test-jdbc-query-large-exports.properties";
  private static final String testJdbcQueryBatchSizeProperties="/test-jdbc-query-exports-batchsize.properties";
  private JDBCCellWriter jdbcCellWriter;
  private JDBCConfig jdbcConfig;


  private JDBCConfig loadConfig(String properties) throws IOException {
    Properties jdbcProperties = new Properties();
    try {
      jdbcProperties.load(JDBCCellWriter.class.getResourceAsStream(properties));
    } catch (IOException e) {
      throw new RuntimeException("Cannot find test properties!", e);
    }
    JDBCConfig jdbcConfig = new JDBCConfig();
    jdbcConfig.setJdbcConnectionUrl(jdbcProperties.getProperty("jdbc.connect.url"));
    jdbcConfig.setJdbcQuery(jdbcProperties.getProperty("jdbc.query"));
    jdbcConfig.setJdbcParams(new CSVParser().parseLine(jdbcProperties.getProperty("jdbc.params")));

    jdbcConfig.setJdbcUsername(jdbcProperties.getProperty("jdbc.username"));
    jdbcConfig.setJdbcPassword(jdbcProperties.getProperty("jdbc.password").toCharArray());
    jdbcConfig.setBatchSize(Integer.parseInt(jdbcProperties.getProperty("jdbc.batch.size") != null ? jdbcProperties.getProperty("jdbc.batch.size") : "0"));

    return jdbcConfig;
  }

  @BeforeEach
  public void setUp() throws SQLException, IOException {
    jdbcConfig = loadConfig(testJdbcQueryProperties);
    jdbcCellWriter = new JDBCCellWriter(jdbcConfig);
  }

  @AfterEach
  public void tearDown() {
    jdbcCellWriter.close();
  }

  @Test
  void testDefaultBatchSize() throws NoSuchFieldException, IllegalAccessException {
    Field batchSizeField = jdbcCellWriter.getClass().getDeclaredField("batchSize");
    batchSizeField.setAccessible(true);
    int batchSize = (int) batchSizeField.get(jdbcCellWriter);
    assertTrue(batchSize == 1001);
  }

  @Test
  void testConfigurableBatchSize()
          throws AnaplanAPIException, IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    JDBCConfig jdbcConfig = loadConfig(testJdbcQueryBatchSizeProperties);
    JDBCCellWriter jdbcCellWriter = new JDBCCellWriter(jdbcConfig);
    int[] mapcols = {};
    boolean batchRemoved = false;
    InputStream inputStream2 = new FileInputStream("src/test/resources/files/xef.txt");
    DataRow dr = new DataRow();
    dr.setMapcols(mapcols);
    dr.setInputStream(inputStream2);
    dr.setExportId("exportId");
    dr.setMaxRetryCount(5);
    dr.setRetryTimeout(10);
    dr.setChunks(6);
    dr.setChunkId("0");
    dr.setColumnCount(3);
    dr.setSeparator(",");
    LineNumberReader lnr = new LineNumberReader( new InputStreamReader(dr.getInputStream()));
    CSVFormat anaplanCSVFormat = CSVFormat.RFC4180.builder().setDelimiter(dr.getSeparator().charAt(0)).build();
    List<String[]> rowBatch = new ArrayList<>();
    Method method = jdbcCellWriter.getClass().getDeclaredMethod("processRowBatch", DataRow.class,
            CSVFormat.class, LineNumberReader.class, boolean.class, List.class);
    method.setAccessible(true);

    assertEquals(4, ((List<String[]>) method.invoke(jdbcCellWriter,dr,anaplanCSVFormat,lnr,batchRemoved,rowBatch)).size());
    jdbcCellWriter.close();
  }

  @Test
  void testSqlInsertQueryMultipleRows()
          throws AnaplanAPIException, IOException, SQLException {
    int[] mapcols = {};
    InputStream inputStream1 = new FileInputStream("src/test/resources/files/xee.txt");
    InputStream inputStream2 = new FileInputStream("src/test/resources/files/xef.txt");
    InputStream inputStream3 = new FileInputStream("src/test/resources/files/xeg.txt");
    InputStream inputStream4 = new FileInputStream("src/test/resources/files/xeh.txt");
    InputStream inputStream5 = new FileInputStream("src/test/resources/files/xei.txt");
    InputStream inputStream6 = new FileInputStream("src/test/resources/files/xej.txt");
    DataRow dr = new DataRow();
    dr.setMapcols(mapcols);
    dr.setInputStream(inputStream1);
    dr.setExportId("exportId");
    dr.setMaxRetryCount(5);
    dr.setRetryTimeout(10);
    dr.setChunks(6);
    dr.setChunkId("0");
    dr.setColumnCount(12);
    dr.setSeparator(",");
    assertEquals(1049, jdbcCellWriter.writeDataRow(dr));

    dr.setChunkId("1");
    dr.setInputStream(inputStream2);
    assertEquals(1059, jdbcCellWriter.writeDataRow(dr));
    dr.setChunkId("2");
    dr.setInputStream(inputStream3);
    assertEquals(2159, jdbcCellWriter.writeDataRow(dr));
    dr.setChunkId("3");
    dr.setInputStream(inputStream4);
    assertEquals(3159, jdbcCellWriter.writeDataRow(dr));
    dr.setInputStream(inputStream5);
    dr.setChunkId("4");
    assertEquals(4158, jdbcCellWriter.writeDataRow(dr));
    dr.setChunkId("5");
    dr.setInputStream(inputStream6);
    assertEquals(6158, jdbcCellWriter.writeDataRow(dr));

  }


  @Test
  void testExportJdbcDuplicate()
          throws AnaplanAPIException, IOException, SQLException {
    int[] mapcols = {};
    InputStream inputStream1 = new FileInputStream("src/test/resources/files/jdbc_exp_1.txt");
    InputStream inputStream2 = new FileInputStream("src/test/resources/files/jdbc_exp_2.txt");
    DataRow dr = new DataRow();
    dr.setMapcols(mapcols);
    dr.setInputStream(inputStream1);
    dr.setExportId("exportId");
    dr.setMaxRetryCount(5);
    dr.setRetryTimeout(10);
    dr.setChunks(2);
    dr.setChunkId("0");
    dr.setColumnCount(12);
    dr.setSeparator(",");
    assertEquals(999, jdbcCellWriter.writeDataRow(dr));
    dr.setChunkId("1");
    dr.setInputStream(inputStream2);
    assertEquals(1010, jdbcCellWriter.writeDataRow(dr));
  }


}
