/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.exportdata.view;

import java.util.Date;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.exportdata.presenter.DataExportUiHandlers;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.github.gwtbootstrap.client.ui.Alert;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 * View of the dialog used to export data from Opal.
 */
public class DataExportView extends ModalPopupViewWithUiHandlers<DataExportUiHandlers>
    implements DataExportPresenter.Display {

  private final Translations translations;

  private String username;

  interface Binder extends UiBinder<Widget, DataExportView> {}

  @UiField
  Modal modal;

  @UiField
  Alert exportNTable;

  @UiField
  Panel unitsPanel;

  @UiField
  Chooser units;

  @UiField
  SimplePanel filePanel;

  @UiField
  Chooser fileFormat;

  private FileSelectionPresenter.Display fileSelection;

  @Inject
  public DataExportView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    modal.setTitle(translations.exportData());
    initWigits();
  }

  private void initWigits() {
    fileFormat.addItemToGroup(translations.csvLabel(), ImportConfig.ImportFormat.CSV.name());
    fileFormat.addItemToGroup(translations.opalXmlLabel(), ImportConfig.ImportFormat.XML.name());

  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @UiHandler("submitButton")
  public void onSubmit(ClickEvent event) {
    getUiHandlers().onSubmit(getFileFormat(), getOutFile(), getSelectedUnit());
  }

  private String getSelectedUnit() {
    return units.getSelectedIndex() == 0 ? null : units.getSelectedValue();
  }

  @Override
  public void setUnits(JsArray<FunctionalUnitDto> unitDtos) {
    units.clear();
    units.addItem(translations.opalDefaultIdentifiersLabel());
    for(int i = 0; i < unitDtos.length(); i++) {
      units.addItem(unitDtos.get(i).getName());
    }
    units.setSelectedIndex(0);
    unitsPanel.setVisible(unitDtos.length() > 0);
  }

  private String getOutFile() {
    Date date = new Date();
    DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyyMMddHHmmss");

    String suffix = "";
    if(!fileSelection.getFile().endsWith("/")) {
      suffix += "/";
    }
    suffix += "export-" + username + "-" + dateFormat.format(date);

    if("xml".equalsIgnoreCase(getFileFormat())) {
      return fileSelection.getFile() + suffix + ".zip";
    }

    return fileSelection.getFile() + suffix;
  }

  private String getFileFormat() {
    return fileFormat.getValue(fileFormat.getSelectedIndex());
  }

  @Override
  public void setFileWidgetDisplay(FileSelectionPresenter.Display display) {
    filePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setEnabled(true);
    fileSelection.setFieldWidth("20em");
    fileFormat.setEnabled(true);
  }

  @Override
  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public void showExportNAlert(String message) {
    exportNTable.setText(message);
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }
}