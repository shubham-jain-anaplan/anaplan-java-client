package com.anaplan.client.listwriter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anaplan.client.Utils;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Date;
import org.junit.jupiter.api.Test;

class UtilsTest {

  @Test
  void fileAbsoluteTest () throws FileNotFoundException {
    assertTrue((Utils.getAbsolutePath(Paths.get("a.txt")).isAbsolute()));
  }

  @Test
  void fileAbsoluteNullTest () {
    assertThrows(FileNotFoundException.class , () -> Utils.getAbsolutePath(null));
  }

  @Test
  void propertyFileNotPresentShouldReturnDefault() {
    assertThat(Utils.getPropertiesFromClassPathPomProperties("AC_VERSION_PROPERTY_KEY", "4.0.1"), is("4.0.1"));
  }

  @Test
  void getDateFromRetryAfterSeconds() {
    Date date = Utils.getDateFromRetryAfter("100");
    Date now = new Date();
    assertTrue(date.getTime() - now.getTime() < 100000);
    assertTrue(date.getTime() - now.getTime() > 99800);
  }

  @Test
  void getDateFromRetryAfterDateFormat() {
    Date date = Utils.getDateFromRetryAfter("Fri, 06 Jan 2023 23:59:59");
    Date now = new Date();
    assertTrue(date.getTime() - now.getTime() < 0);
  }
}
