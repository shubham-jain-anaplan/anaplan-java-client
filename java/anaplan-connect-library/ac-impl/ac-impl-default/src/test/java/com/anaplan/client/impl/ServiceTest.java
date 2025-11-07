package com.anaplan.client.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.anaplan.client.LargeDataExport;
import com.anaplan.client.LargeDataExport.TYPE_LARGE_EXPORT;
import com.anaplan.client.Model;
import com.anaplan.client.Workspace;
import com.anaplan.client.api.AnaplanAPI;
import com.anaplan.client.dto.LargeRequestData;
import com.anaplan.client.dto.responses.WorkspaceResponse;
import com.anaplan.client.exceptions.ViewDataNotFoundException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ServiceTest extends BaseTest {

  private static final String LIST_REQUEST_RESPONSE_JSON = "responses/list_read_request_response.json";
  private static final String LIST_READ_RESPONSE_JSON = "files/list_read_response.csv";
  private static final String LIST_STATUS_RESPONSE_JSON = "responses/list_read_status_response.json";
  private static final String LIST_CANCELED_RESPONSE_JSON = "responses/list_status_canceled_response.json";
  private static final String MODEL_RESPONSE = "responses/model_response.json";
  private static final String WORKSPACE2_RESPONSE_JSON = "responses/workspace2_response.json";

  private Model mockModel;
  private AnaplanAPI api;
  private LargeDataExport largeDataExport;

  @BeforeEach
  public void setUp() throws Exception {
    mockModel = fetchMockModel();
    api = getMockService().getApiProvider().get();
    when(api.listReadRequest(mockModel.getWorkspace().getId(), mockModel.getId(), "10001"))
        .thenReturn(createFeignResponse(LIST_REQUEST_RESPONSE_JSON, LargeRequestData.class));

    when(api.getListRequestStatus(mockModel.getWorkspace().getId(), mockModel.getId(), "10001", "39EA5116127F4A989F19CCAF7F2D06F9"))
        .thenReturn(createFeignResponse(LIST_STATUS_RESPONSE_JSON, LargeRequestData.class));

    when(api.downloadCSVListRequest(mockModel.getWorkspace().getId(), mockModel.getId(), "10001", "39EA5116127F4A989F19CCAF7F2D06F9", 0))
        .thenReturn(new String(getFixture(LIST_READ_RESPONSE_JSON)));

    when(api.deleteListReadRequest(mockModel.getWorkspace().getId(), mockModel.getId(), "10001", "39EA5116127F4A989F19CCAF7F2D06F9"))
        .thenReturn(createFeignResponse(LIST_STATUS_RESPONSE_JSON, LargeRequestData.class));

    largeDataExport = LargeDataExport.getLargeDataExportService(api, TYPE_LARGE_EXPORT.LIST_EXPORT);
    when(getMockService().getLargeDataExportService()).thenReturn(largeDataExport);
  }

  @Test
  public void testGetLargeListItemsCsv() {
    String result = getMockService().getLargeListItemsCsv (mockModel.getWorkspace().getId(), mockModel.getId(), "10001");
    assertNotNull(result);
    assertEquals(326, result.length());
  }

  @Test
  public void testGetLargeListItemsCanceled() throws IOException {
    when(api.getListRequestStatus(mockModel.getWorkspace().getId(), mockModel.getId(), "10001", "39EA5116127F4A989F19CCAF7F2D06F9"))
        .thenReturn(createFeignResponse(LIST_CANCELED_RESPONSE_JSON, LargeRequestData.class));
    String result = getMockService().getLargeListItemsCsv (mockModel.getWorkspace().getId(), mockModel.getId(), "10001");
    assertNotNull(result);
    assertEquals(result.length(), 0);
  }

  @Test
  public void testListNotFound() {
    try {
      getMockService()
          .getLargeListItemsCsv(mockModel.getWorkspace().getId(), mockModel.getId(), "100001aa");
    } catch (Exception e) {
      assertTrue(e instanceof ViewDataNotFoundException);
      assertEquals(e.getMessage(), "View data not found: 100001aa");
    }
  }

  @Test
  public void testGetWorkspaceById() throws IOException {
    when(api.getWorkspace("testWorkspace2NameOrId"))
            .thenReturn(createFeignResponse(WORKSPACE2_RESPONSE_JSON, WorkspaceResponse.class));
    Workspace result = getMockService().getWorkspaceById("testWorkspace2NameOrId");

    assertNotNull(result);

    Workspace result2 = getMockService().getWorkspaceById("testWorkspace2NameOrId");

    assertEquals(result, result2);
    // Assert that API was called only once (as th result should be cached)
    Mockito.verify(mockModel.getWorkspace().getApi()).getWorkspace("testWorkspace2NameOrId");
  }

  @Test
  public void testGetModelById() {
    Model result = getMockService().getModelById(mockModel.getWorkspace().getId(), mockModel.getId());
    assertEquals(mockModel.getId(), result.getId());
  }
}
