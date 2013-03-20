/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import org.obiba.magma.ValueTable;

/**
 * Manager of {@code ValueTable} variables indices.
 */
public interface VariablesIndexManager extends IndexManager {

  /**
   * Get {@code ValueTable} variables index.
   * @param valueTable
   * @return
   */
  ValueTableVariablesIndex getIndex(ValueTable valueTable);

}
