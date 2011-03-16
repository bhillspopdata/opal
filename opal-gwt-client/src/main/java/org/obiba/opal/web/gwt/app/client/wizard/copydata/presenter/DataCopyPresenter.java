/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.copydata.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.job.presenter.JobListPresenter;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.TableListUpdateEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.Wizard;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.CopyCommandOptionsDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class DataCopyPresenter extends WidgetPresenter<DataCopyPresenter.Display> implements Wizard {

  @Inject
  private TableListPresenter tableListPresenter;

  @Inject
  private JobListPresenter jobListPresenter;

  private String datasourceName;

  private TableDto table;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public DataCopyPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  public DataCopyPresenter(Display display, EventBus eventBus, TableListPresenter tableListPresenter) {
    this(display, eventBus);
    this.tableListPresenter = tableListPresenter;
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    initDisplayComponents();
    tableListPresenter.clear();
    if(datasourceName != null) {
      tableListPresenter.selectDatasourceTables(datasourceName);
    } else if(table != null) {
      tableListPresenter.selectTable(table);
    }
  }

  protected void initDisplayComponents() {
    tableListPresenter.bind();
    getDisplay().setTableWidgetDisplay(tableListPresenter.getDisplay());

    super.registerHandler(getDisplay().addCancelClickHandler(new CancelClickHandler()));
    super.registerHandler(getDisplay().addCloseClickHandler(new FinishClickHandler()));
    super.registerHandler(getDisplay().addSubmitClickHandler(new SubmitClickHandler()));
    super.registerHandler(getDisplay().addJobLinkClickHandler(new JobLinkClickHandler(eventBus, jobListPresenter)));
    super.registerHandler(eventBus.addHandler(TableListUpdateEvent.getType(), new TablesToExportChangedHandler()));
    getDisplay().setTablesValidator(new TablesValidator());
    getDisplay().setDestinationValidator(new DestinationValidator());
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    tableListPresenter.unbind();
    datasourceName = null;
    table = null;
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    initDatasources();
    getDisplay().showDialog();
  }

  private void initDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> resource) {
        List<DatasourceDto> datasources = null;
        if(resource != null && resource.length() > 0) {
          datasources = filterDatasources(resource);

        }
        if(datasources != null && datasources.size() > 0) {
          getDisplay().setDatasources(datasources);
        } else {
          eventBus.fireEvent(NotificationEvent.newBuilder().error("NoDataToCopy").build());
        }
      }
    }).send();
  }

  private List<DatasourceDto> filterDatasources(JsArray<DatasourceDto> datasources) {

    List<DatasourceDto> filteredDatasources = new ArrayList<DatasourceDto>();
    Set<String> originDatasourceName = getOriginDatasourceNames();
    for(DatasourceDto datasource : JsArrays.toList(datasources)) {
      if(!originDatasourceName.contains(datasource.getName())) {
        filteredDatasources.add(datasource);
      }
    }
    return filteredDatasources;
  }

  private Set<String> getOriginDatasourceNames() {
    Set<String> originDatasourceNames = new HashSet<String>();
    for(TableDto table : tableListPresenter.getTables()) {
      originDatasourceNames.add(table.getDatasourceName());
    }
    // can't copy in itself
    if(datasourceName != null) {
      originDatasourceNames.add(datasourceName);
    } else if(table != null) {
      originDatasourceNames.add(table.getDatasourceName());
    }
    return originDatasourceNames;
  }

  //
  // Wizard Methods
  //

  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length != 0) {
      if(event.getEventParameters()[0] instanceof String) {
        datasourceName = (String) event.getEventParameters()[0];
      } else if(event.getEventParameters()[0] instanceof TableDto) {
        table = (TableDto) event.getEventParameters()[0];
      } else {
        throw new IllegalArgumentException("unexpected event parameter type (expected String)");
      }
    }
  }

  //
  // Interfaces and classes
  //

  private final class DestinationValidator implements ValidationHandler {
    @Override
    public boolean validate() {
      List<String> errors = formValidationErrors();
      if(errors.size() > 0) {
        eventBus.fireEvent(NotificationEvent.newBuilder().error(errors).build());
        return false;
      }
      return true;
    }

    private List<String> formValidationErrors() {
      List<String> result = new ArrayList<String>();

      return result;
    }
  }

  private final class TablesValidator implements ValidationHandler {
    @Override
    public boolean validate() {
      if(tableListPresenter.getTables().size() == 0) {
        eventBus.fireEvent(NotificationEvent.newBuilder().error("ExportDataMissingTables").build());
        return false;
      }
      return true;
    }
  }

  class SubmitClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      getDisplay().renderPendingConclusion();
      ResourceRequestBuilderFactory.newBuilder().forResource("/shell/copy").post() //
      .withResourceBody(CopyCommandOptionsDto.stringify(createCopycommandOptions())) //
      .withCallback(400, new ClientFailureResponseCodeCallBack()) //
      .withCallback(201, new SuccessResponseCodeCallBack()).send();
    }

    private CopyCommandOptionsDto createCopycommandOptions() {
      CopyCommandOptionsDto dto = CopyCommandOptionsDto.create();

      JsArrayString selectedTables = JavaScriptObject.createArray().cast();
      if(table != null) {
        selectedTables.push(table.getDatasourceName() + "." + table.getName());
      } else {
        for(TableDto table : tableListPresenter.getTables()) {
          selectedTables.push(table.getDatasourceName() + "." + table.getName());
        }
      }

      dto.setTablesArray(selectedTables);
      dto.setDestination(getDisplay().getSelectedDatasource());
      dto.setNonIncremental(!getDisplay().isIncremental());
      dto.setNoVariables(!getDisplay().isWithVariables());
      if(getDisplay().isUseAlias()) dto.setTransform("attribute('alias').isNull().value ? name() : attribute('alias')");

      return dto;
    }
  }

  class ClientFailureResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      eventBus.fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      getDisplay().renderFailedConclusion();
    }
  }

  class SuccessResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      String location = response.getHeader("Location");
      String jobId = location.substring(location.lastIndexOf('/') + 1);
      getDisplay().renderCompletedConclusion(jobId);
    }
  }

  static class JobLinkClickHandler implements ClickHandler {

    private final EventBus eventBus;

    private final JobListPresenter jobListPresenter;

    public JobLinkClickHandler(EventBus eventBus, JobListPresenter jobListPresenter) {
      super();
      this.eventBus = eventBus;
      this.jobListPresenter = jobListPresenter;
    }

    @Override
    public void onClick(ClickEvent arg0) {
      eventBus.fireEvent(WorkbenchChangeEvent.newBuilder(jobListPresenter).forResource("/shell/commands").build());
    }
  }

  class CancelClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      getDisplay().hideDialog();
      unbind();
    }
  }

  class FinishClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      getDisplay().hideDialog();
      unbind();
    }
  }

  public class TablesToExportChangedHandler implements TableListUpdateEvent.Handler {

    @Override
    public void onTableListUpdate(TableListUpdateEvent event) {
      initDatasources();
    }
  }

  public interface Display extends WidgetDisplay {

    void showDialog();

    void hideDialog();

    void setTablesValidator(ValidationHandler validationHandler);

    void setDestinationValidator(ValidationHandler handler);

    /** Set a collection of datasources retrieved from Opal. */
    void setDatasources(List<DatasourceDto> datasources);

    /** Get the datasource selected by the user. */
    String getSelectedDatasource();

    /** Get the form submit button. */
    HandlerRegistration addSubmitClickHandler(ClickHandler handler);

    /** Display the conclusion step */
    void renderCompletedConclusion(String jobId);

    void renderFailedConclusion();

    void renderPendingConclusion();

    /** Add a handler to the job list */
    HandlerRegistration addJobLinkClickHandler(ClickHandler handler);

    boolean isIncremental();

    boolean isWithVariables();

    boolean isUseAlias();

    void setTableWidgetDisplay(TableListPresenter.Display display);

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addCloseClickHandler(ClickHandler handler);

  }

}
