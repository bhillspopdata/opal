/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidatableWidgetPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.VariableAddRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;

public class AddDerivedVariableDialogPresenter extends ValidatableWidgetPresenter<AddDerivedVariableDialogPresenter.Display> {

  public interface Display extends WidgetDisplay {

    void showDialog();

    void hideDialog();

    void addVariableSuggestion(String suggestion);

    void clearVariableSuggestions();

    HasText getVariableName();

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addAddVariableClickHandler(ClickHandler handler);
  }

  @Inject
  public AddDerivedVariableDialogPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public void refreshDisplay() {

  }

  @Override
  public void revealDisplay() {

  }

  @Override
  protected void onBind() {
    addHandlers();
    addValidators();
  }

  private void addValidators() {
    addValidator(new RequiredTextValidator(getDisplay().getVariableName(), "CopyFromVariableNameIsRequired"));
  }

  private void addHandlers() {
    super.registerHandler(getDisplay().addAddVariableClickHandler(new AddVariableClickHandler()));
    super.registerHandler(getDisplay().addCancelClickHandler(new CancelClickHandler()));
    super.registerHandler(eventBus.addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredHandler()));
  }

  @Override
  protected void onUnbind() {

  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {

  }

  private String getVariableName() {
    return getDisplay().getVariableName().getText();
  }

  void refreshVariableNameSuggestions(ViewDto viewDto) {
    getDisplay().clearVariableSuggestions();

    // Add the derived variables to the suggestions.
    VariableListViewDto variableListDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);
    for(VariableDto variable : JsArrays.toList(variableListDto.getVariablesArray())) {
      getDisplay().addVariableSuggestion(variable.getName());
    }

    // Add the variables to the suggestions.
    String[] tableNameParts;
    for(int i = 0; i < viewDto.getFromArray().length(); i++) {
      GWT.log(viewDto.getFromArray().get(i));
      tableNameParts = viewDto.getFromArray().get(i).split("\\.");
      ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource("/datasource/" + tableNameParts[0] + "/table/" + tableNameParts[1] + "/variables").get().withCallback(new VariablesDtoCallBack()).send();
    }
  }

  public class VariablesDtoCallBack implements ResourceCallback<JsArray<VariableDto>> {

    @Override
    public void onResource(Response response, JsArray<VariableDto> resource) {
      for(int i = 0; i < resource.length(); i++) {
        getDisplay().addVariableSuggestion(resource.get(i).getName());
      }
    }
  }

  private class ViewConfigurationRequiredHandler implements ViewConfigurationRequiredEvent.Handler {

    @Override
    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      ViewDto viewDto = event.getView();
      viewDto.setFromArray(JsArrays.toSafeArray(viewDto.getFromArray()));
      VariableListViewDto variableListDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);
      variableListDto.setVariablesArray(JsArrays.toSafeArray(variableListDto.getVariablesArray()));

      refreshVariableNameSuggestions(event.getView());
    }

  }

  private class CancelClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      getDisplay().hideDialog();
    }

  }

  private class AddVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(validate()) {
        getDisplay().hideDialog();
        eventBus.fireEvent(new VariableAddRequiredEvent(getVariableName()));
      }
    }
  }
}
