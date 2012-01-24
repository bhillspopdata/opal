/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.ValuesTablePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.ValuesTablePresenter.ValueSetsFetcher;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.ValuesTablePresenter.ValueSetsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueColumn.ValueSelectionHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.IconActionCell;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.gwtplatform.mvp.client.ViewImpl;

public class ValuesTableView extends ViewImpl implements ValuesTablePresenter.Display {

  private static final int MAX_VISIBLE_COLUMNS = 5;

  private static final int PAGE_SIZE = 20;

  @UiTemplate("ValuesTableView.ui.xml")
  interface ValuesTableViewUiBinder extends UiBinder<Widget, ValuesTableView> {
  }

  private static ValuesTableViewUiBinder uiBinder = GWT.create(ValuesTableViewUiBinder.class);

  private final Widget widget;

  @UiField
  SimplePager pager;

  @UiField
  InlineLabel noValues;

  @UiField
  FlowPanel valuesPanel;

  @UiField
  Table<ValueSetDto> valuesTable;

  private ValueSetsDataProvider dataProvider;

  private List<VariableDto> listVariable;

  private TableDto table;

  private ValueSetsFetcher fetcher;

  private int firstVisibleIndex = 0;

  public ValuesTableView() {
    widget = uiBinder.createAndBindUi(this);
    valuesTable.setEmptyTableWidget(noValues);
    pager.setDisplay(valuesTable);
    pager.setPageSize(PAGE_SIZE);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setTable(TableDto table) {
    this.table = table;
    valuesTable.setRowCount(table.getValueSetCount());
    valuesTable.setPageStart(0);

    if(dataProvider != null) {
      dataProvider.removeDataDisplay(valuesTable);
      dataProvider = null;
    }
  }

  @Override
  public void setVariables(JsArray<VariableDto> variables) {
    initValuesTable();

    listVariable = JsArrays.toList(variables);
    int visible = listVariable.size() < MAX_VISIBLE_COLUMNS ? listVariable.size() : MAX_VISIBLE_COLUMNS;
    for(int i = 0; i < visible; i++) {
      valuesTable.addColumn(createColumn(getVariableAt(i)), getColumnLabel(i));
    }

    if(listVariable.size() > 1) {
      valuesTable.insertColumn(1, createEmptyColumn(), createHeader(new PreviousActionCell()));
      valuesTable.insertColumn(valuesTable.getColumnCount(), createEmptyColumn(), createHeader(new NextActionCell()));
    }

    if(dataProvider != null) {
      dataProvider.removeDataDisplay(valuesTable);
      dataProvider = null;
    }
    dataProvider = new ValueSetsDataProvider();
    dataProvider.addDataDisplay(valuesTable);
  }

  @Override
  public ValueSetsProvider getValueSetsProvider() {
    return dataProvider;
  }

  @Override
  public void setValueSetsFetcher(ValueSetsFetcher provider) {
    this.fetcher = provider;
  }

  //
  // Private methods
  //

  private String getColumnLabel(int i) {
    return listVariable.get(i).getName();
  }

  private VariableDto getVariableAt(int i) {
    return listVariable.get(i);
  }

  private ValueColumn createColumn(final VariableDto variable) {
    ValueColumn col = new ValueColumn(variable) {
      @Override
      protected int getPosition() {
        return listVariable.indexOf(variable);
      }
    };
    col.setValueSelectionHandler(new VariableValueSelectionHandler());
    return col;
  }

  private void initValuesTable() {
    while(valuesTable.getColumnCount() > 0) {
      valuesTable.removeColumn(0);
    }
    firstVisibleIndex = 0;

    TextColumn<ValueSetDto> entityColumn = new TextColumn<ValueSetDto>() {

      @Override
      public String getValue(ValueSetDto value) {
        return value.getIdentifier();
      }
    };
    setMinimumWidth(entityColumn);

    valuesTable.addColumn(entityColumn, table.getEntityType());
  }

  private Header<String> createHeader(AbstractCell<String> cell) {
    return new Header<String>(cell) {

      @Override
      public String getValue() {
        return null;
      }
    };
  }

  private TextColumn<ValueSetDto> createEmptyColumn() {
    TextColumn<ValueSetDto> emptyColumn = new TextColumn<ValueSetDto>() {

      @Override
      public String getValue(ValueSetDto object) {
        return null;
      }
    };
    setMinimumWidth(emptyColumn);
    return emptyColumn;
  }

  private void setMinimumWidth(Column<ValueSetDto, ?> column) {
    valuesTable.setColumnWidth(column, 1, Unit.PX);
  }

  //
  // Inner classes
  //

  private final class PreviousActionCell extends IconActionCell<String> {

    private PreviousActionCell() {
      super("icon-previous", new Delegate<String>() {

        @Override
        public void executeClick(String value) {
          if(firstVisibleIndex == 0) return;

          valuesTable.removeColumn(valuesTable.getColumnCount() - 2);
          int idx = firstVisibleIndex--;
          valuesTable.insertColumn(2, createColumn(getVariableAt(idx)), getColumnLabel(idx));
          valuesTable.redrawHeaders();
        }

        @Override
        public void executeMouseDown(String value) {
          // TODO Auto-generated method stub

        }
      });
    }

    @Override
    public boolean isEnabled() {
      return firstVisibleIndex > 0;
    }

  }

  private final class NextActionCell extends IconActionCell<String> {

    private NextActionCell() {
      super("icon-next", new Delegate<String>() {

        @Override
        public void executeClick(String value) {
          if(firstVisibleIndex + MAX_VISIBLE_COLUMNS >= listVariable.size() - 1) return;

          valuesTable.removeColumn(2);
          int idx = ++firstVisibleIndex + MAX_VISIBLE_COLUMNS;
          valuesTable.insertColumn(valuesTable.getColumnCount() - 1, createColumn(getVariableAt(idx)), getColumnLabel(idx));
          valuesTable.redrawHeaders();
        }

        @Override
        public void executeMouseDown(String value) {
          // TODO Auto-generated method stub

        }

      });
    }

    @Override
    public boolean isEnabled() {
      return (firstVisibleIndex + MAX_VISIBLE_COLUMNS >= listVariable.size() - 1) == false;
    }

  }

  private final class ValueSetsDataProvider extends AbstractDataProvider<ValueSetsDto.ValueSetDto> implements ValuesTablePresenter.ValueSetsProvider {

    @Override
    protected void onRangeChanged(HasData<ValueSetDto> display) {
      // Get the new range.
      final Range range = display.getVisibleRange();

      // query the valuesets
      int start = range.getStart();

      if(start > table.getValueSetCount()) return;

      int length = range.getLength();
      if(start + length > table.getValueSetCount()) {
        length = table.getValueSetCount() - start;
      }
      fetcher.request(listVariable, start, length);
    }

    @Override
    public void populateValues(int offset, ValueSetsDto valueSets) {
      updateRowData(offset, JsArrays.toList(valueSets.getValueSetsArray()));
    }
  }

  private final class VariableValueSelectionHandler implements ValueSelectionHandler {
    @Override
    public void onValueSelection(int row, int column, ValueSetDto valueSet) {
      fetcher.request(listVariable.get(column), valueSet.getIdentifier());
    }
  }

}
