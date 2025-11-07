package com.anaplan.client.impl;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.anaplan.client.CellWriter.DataRow;
import com.anaplan.client.dto.ListItem;
import com.anaplan.client.jdbc.JDBCCellWriter;
import com.anaplan.client.jdbc.JDBCConfig;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Test;

class JDBCTest extends BaseTest{

  //This is the line cutted by first chunk
  String line = "e,\"f,g\"";
  static Method method;
  Field lastLine;

  /**
   * This test simulate that a chunk cut a line where the separator is in the column value
   * In this case the chunks split a line like: 'e,"f,g",a,b' in 2. First chunk get 'e,"f,g"' and the next chunk get ',a,b'
   * In metadata we know that we need to have 4 columns and the separator is ",".
   * We combine that 2 line in one to restore it: 'e,"f,g"' + ',a,b' and then we split it by delimiter (,) to check if we have the columns number check.
   * Before was a simple split by "," and the result was 5 <> 4 like in metadata so the results was 2 lines
   * After fix we split by csv and we have 4 columns and the result is one line that match the metadata
   * @throws NoSuchMethodException
   * @throws NoSuchFieldException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  @Test
  void testMultiChunks()
      throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException {

    DataRow dataRow = new DataRow();
    dataRow.setSeparator(",");
    dataRow.setChunks(2);
    dataRow.setColumnCount(4);
    dataRow.setNoOfChunks(2);
    dataRow.setChunkId("1");

    //The second chunk has first line the last values from the line cutted by first chunk
    StringReader stringReader = new StringReader(",a,b");
    LineNumberReader lnr = new LineNumberReader(stringReader);

    CSVFormat anaplanCSVFormat = CSVFormat.RFC4180.builder().setDelimiter(dataRow.getSeparator().charAt(0)).build();

    JDBCConfig jdbcConfig = new JDBCConfig();
    jdbcConfig.setJdbcQuery("");

    JDBCCellWriter jdbcCellWriter = new JDBCCellWriter(jdbcConfig);
    method = JDBCCellWriter.class.getDeclaredMethod("processRowBatch", DataRow.class, CSVFormat.class,
        LineNumberReader.class, boolean.class, List.class);
    method.setAccessible(true);

    lastLine = JDBCCellWriter.class.getDeclaredField("lastRow");
    lastLine.setAccessible(true);
    lastLine.set(jdbcCellWriter, line);

    List<String[]> rowBatch = new ArrayList<>();

    List<String[]> result = (List<String[]>) method.invoke(jdbcCellWriter,dataRow, anaplanCSVFormat, lnr, false, rowBatch);
    assertThat(result.size(),is(1));
    assertThat(result.get(0).length,is(dataRow.getColumnCount()));
    assertThat(result.get(0)[0],is("e"));
    assertThat(result.get(0)[1],is("f,g"));
    assertThat(result.get(0)[2],is("a"));
    assertThat(result.get(0)[3],is("b"));
  }
}
