package com.anaplan.client.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.anaplan.client.LargeDataExport;
import com.anaplan.client.LargeDataExport.TYPE_LARGE_EXPORT;
import com.anaplan.client.Model;
import com.anaplan.client.Module;
import com.anaplan.client.api.AnaplanAPI;
import com.anaplan.client.dto.ExportData;
import com.anaplan.client.dto.ViewRequestData;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ModuleTest extends BaseTest{

  private static final String VIEW_REQUEST_RESPONSE_JSON = "responses/view_read_request_response.json";
  private static final String VIEW_READ_RESPONSE_JSON = "files/view_read_response.csv";
  private static final String VIEW_STATUS_RESPONSE_JSON = "responses/view_read_status_response.json";

  private static final String VIEW_REQUEST_SC_RESPONSE_JSON = "responses/view_sc_read_request_response.json";
  private static final String VIEW_READ_SC_RESPONSE_JSON = "files/view_sc_read_response.csv";
  private static final String VIEW_STATUS_SC_RESPONSE_JSON = "responses/view_sc_read_status_response.json";

  private static final String VIEW_STATUS_CANCELED_JSON = "responses/view_canceled_status_response.json";

  private Model mockModel;
  private Module module;
  private AnaplanAPI api;
  private LargeDataExport largeDataExport;

  @BeforeEach
  public void setUp() throws Exception {
    mockModel = fetchMockModel();
    module = Mockito.mock(Module.class);

    api = getMockService().getApiProvider().get();
    largeDataExport = LargeDataExport.getLargeDataExportService(api, TYPE_LARGE_EXPORT.VIEW_EXPORT);
    when(module.getLargeDataExportService()).thenReturn(largeDataExport);
    when(module.getApi()).thenReturn(api);
    ExportData exportType = new ExportData("TABULAR_MULTI_COLUMN");
    when(api.viewReadRequest(mockModel.getWorkspace().getId(), mockModel.getId(), "102000000007", exportType))
        .thenReturn(createFeignResponse(VIEW_REQUEST_RESPONSE_JSON, ViewRequestData.class));

    when(api.getViewRequestStatus(mockModel.getWorkspace().getId(), mockModel.getId(), "102000000007", "C50F4597A1D3457E9A86FCC21FBA5533"))
        .thenReturn(createFeignResponse(VIEW_STATUS_RESPONSE_JSON, ViewRequestData.class));

    when(api.downloadCSVViewRequestPage(mockModel.getWorkspace().getId(), mockModel.getId(), "102000000007", "C50F4597A1D3457E9A86FCC21FBA5533", 0))
        .thenReturn(new String(getFixture(VIEW_READ_RESPONSE_JSON)));

    when(api.deleteViewReadRequest(mockModel.getWorkspace().getId(), mockModel.getId(), "102000000007", "C50F4597A1D3457E9A86FCC21FBA5533"))
        .thenReturn(createFeignResponse(VIEW_STATUS_RESPONSE_JSON, ViewRequestData.class));

  }

  @Test
  public void testGetLargeViewCsv()
      throws InterruptedException {
    String result = largeDataExport.getRequestData(mockModel.getWorkspace().getId(), mockModel.getId(), "102000000007", "C50F4597A1D3457E9A86FCC21FBA5533");
    assertNotNull(result);
    assertEquals(result.length(), 596);
  }

  @Test
  public void testGetLargeViewCanceled()
      throws IOException, InterruptedException {
    when(api.getViewRequestStatus(mockModel.getWorkspace().getId(), mockModel.getId(), "102000000007", "C50F4597A1D3457E9A86FCC21FBA5533"))
        .thenReturn(createFeignResponse(VIEW_STATUS_CANCELED_JSON, ViewRequestData.class));
    String result = largeDataExport.getRequestData(mockModel.getWorkspace().getId(), mockModel.getId(), "102000000007", "C50F4597A1D3457E9A86FCC21FBA5533");
    assertNotNull(result);
    assertEquals(result.length(), 0);
  }

  @Test
  public void testGetLargeSingleColumnViewCsv()
      throws IOException, InterruptedException {
    ExportData exportType = new ExportData("TABULAR_SINGLE_COLUMN");
    when(api.viewReadRequest(mockModel.getWorkspace().getId(), mockModel.getId(), "102000000007", exportType))
        .thenReturn(createFeignResponse(VIEW_REQUEST_SC_RESPONSE_JSON, ViewRequestData.class));

    when(api.getViewRequestStatus(mockModel.getWorkspace().getId(), mockModel.getId(), "102000000007", "C50F4597A1D3457E9A86FCC21FBA5533"))
        .thenReturn(createFeignResponse(VIEW_STATUS_SC_RESPONSE_JSON, ViewRequestData.class));

    when(api.downloadCSVViewRequestPage(mockModel.getWorkspace().getId(), mockModel.getId(), "102000000007", "C50F4597A1D3457E9A86FCC21FBA5533", 0))
        .thenReturn(new String(getFixture(VIEW_READ_SC_RESPONSE_JSON)));

    when(api.deleteViewReadRequest(mockModel.getWorkspace().getId(), mockModel.getId(), "102000000007", "C50F4597A1D3457E9A86FCC21FBA5533"))
        .thenReturn(createFeignResponse(VIEW_STATUS_SC_RESPONSE_JSON, ViewRequestData.class));

    String result = largeDataExport.getRequestData(mockModel.getWorkspace().getId(), mockModel.getId(), "102000000007", "C50F4597A1D3457E9A86FCC21FBA5533");
    assertNotNull(result);
    assertEquals(result.length(), 11798);
  }

}
