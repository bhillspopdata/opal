/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.dashboard.view;

import org.obiba.opal.web.gwt.app.client.dashboard.presenter.DashboardPresenter;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DashboardView extends Composite implements DashboardPresenter.Display {

  @UiTemplate("DashboardView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DashboardView> {
  }

  //
  // Constants
  //

  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  Label participantCount;

  @UiField
  Anchor exploreVariablesLink;

  @UiField
  Anchor exploreFilesLink;

  @UiField
  Anchor jobsLink;

  @UiField
  Anchor reportsLink;

  @UiField
  Anchor unitsLink;

  @UiField
  Panel datasources;

  @UiField
  Panel units;

  @UiField
  Panel files;

  @UiField
  Panel reports;

  @UiField
  Panel jobs;

  //
  // Instance Variables
  //

  //
  // Constructors
  //

  public DashboardView() {
    super();
    initWidget(uiBinder.createAndBindUi(this));
  }

  //
  // DashboardPresenter.Display Methods
  //

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {

  }

  @Override
  public void stopProcessing() {

  }

  public void setParticipantCount(int count) {
    participantCount.setText(String.valueOf(count));
  }

  public Anchor getDatasourcesLink() {
    return exploreVariablesLink;
  }

  @Override
  public HasClickHandlers getFilesLink() {
    return exploreFilesLink;
  }

  @Override
  public HasClickHandlers getJobsLink() {
    return jobsLink;
  }

  @Override
  public HasClickHandlers getReportsLink() {
    return reportsLink;
  }

  @Override
  public HasClickHandlers getUnitsLink() {
    return unitsLink;
  }

  @Override
  public HasAuthorization getUnitsAuthorizer() {
    return new WidgetAuthorizer(units);
  }

  @Override
  public HasAuthorization getDatasourcesAuthorizer() {
    return new WidgetAuthorizer(datasources);
  }

  @Override
  public HasAuthorization getFilesAuthorizer() {
    return new WidgetAuthorizer(files);
  }

  @Override
  public HasAuthorization getJobsAuthorizer() {
    return new WidgetAuthorizer(jobs);
  }

  @Override
  public HasAuthorization getReportsAuthorizer() {
    return new WidgetAuthorizer(reports);
  }

  //
  // Methods
  //

}
