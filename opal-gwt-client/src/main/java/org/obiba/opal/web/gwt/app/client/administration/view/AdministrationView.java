/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.main.view.MainItemAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.AbstractTabLayout;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.ViewImpl;

public class AdministrationView extends ViewImpl implements AdministrationPresenter.Display {

  private final static class AdminTabComparator implements Comparator<AdminTab> {

    private static final AdminTabComparator instance = new AdminTabComparator();

    @Override
    public int compare(AdminTab o1, AdminTab o2) {
      if(o1.getPriority() == o2.getPriority()) return 0;
      return o1.getPriority() < o2.getPriority() ? -1 : 1;
    }
  }

  private class AdminTab implements Tab {

    private final TabData tabData;

    private final SimplePanel tabContent;

    private final Hyperlink tab;

    private AdminTab(TabData tabData, String token) {
      this.tabData = tabData;
      tabContent = new SimplePanel();
      tab = new Hyperlink();
      tab.setTargetHistoryToken(token);
      // TODO: localise
      tab.setText(tabData.getLabel());
      orderedTabs.add(this);
      Collections.sort(orderedTabs, AdminTabComparator.instance);
//      administrationDisplays.insert(tabContent, tab, getTabIndex());
    }

    public int getTabIndex() {
      return orderedTabs.indexOf(this);
    }

    @Override
    public void activate() {
//      administrationDisplays.selectTab(tabContent);
    }

    @Override
    public Widget asWidget() {
      return tabContent;
    }

    @Override
    public void deactivate() {
    }

    @Override
    public float getPriority() {
      return tabData.getPriority();
    }

    @Override
    public String getText() {
      return tabData.getLabel();
    }

    @Override
    public void setTargetHistoryToken(String historyToken) {

    }

    @Override
    public void setText(String text) {

    }
  }

  @UiTemplate("AdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, AdministrationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  private final List<AdminTab> orderedTabs = Lists.newLinkedList();

  @UiField
  Anchor slqDatabases;

//  @UiField
//  AbstractTabLayout administrationDisplays;

  private Widget currentTabContent;

  public AdministrationView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Tab addTab(TabData tabData, String historyToken) {
    return new AdminTab(tabData, historyToken);
  }

  @Override
  public void removeTab(Tab tab) {
//    administrationDisplays.remove(((AdminTab) tab).getTabIndex());
    orderedTabs.remove(tab);
  }

  @Override
  public void removeTabs() {
//    administrationDisplays.clear();
  }

  @Override
  public void setActiveTab(Tab tab) {
    AdminTab adminTab = (AdminTab) tab;
    if(currentTabContent != null) {
      adminTab.tabContent.setWidget(currentTabContent);
      currentTabContent = null;
    }
//    administrationDisplays.selectTab(adminTab.getTabIndex());
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    if(slot == AdministrationPresenter.TabSlot) {
      // The gwt-platform pattern is that the tab container has only one Panel with the active content being shown.
      // The AbstractTabLayout does not. At least, it doesn't expose it.
      // So we keep this around and in "setActiveTab" we associate this widget to its tab
      currentTabContent = content;
    }
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

}
