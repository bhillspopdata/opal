/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.FunctionalUnitService;
import org.obiba.opal.web.magma.support.InvalidRequestException;

import au.com.bytecode.opencsv.CSVReader;

/**
 *
 */
public abstract class AbstractFunctionalUnitResource {

  protected abstract FunctionalUnitService getFunctionalUnitService();

  protected abstract OpalRuntime getOpalRuntime();

  protected File resolveLocalFile(String path) {
    try {
      // note: does not ensure that file exists
      return getOpalRuntime().getFileSystem().getLocalFile(resolveFileInFileSystem(path));
    } catch(FileSystemException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return getOpalRuntime().getFileSystem().getRoot().resolveFile(path);
  }

  protected FunctionalUnit resolveFunctionalUnit(String unit) {
    FunctionalUnit functionalUnit = getFunctionalUnitService().getFunctionalUnit(unit);
    if(functionalUnit == null) throw new NoSuchFunctionalUnitException(unit);
    return functionalUnit;
  }

  protected List<FunctionalUnit> getUnitsFromIdentifiersMap(CSVReader reader) throws IOException {
    return getUnitsFromName(reader.readNext());
  }

  protected List<FunctionalUnit> getUnitsFromName(String... unitNames) {
    // find the units
    List<FunctionalUnit> units = new ArrayList<FunctionalUnit>();
    List<String> visitedUnitNames = new ArrayList<String>();
    for(int i = 0; i < unitNames.length; i++) {
      String unit = unitNames[i];
      if(!visitedUnitNames.contains(unit)) {
        visitedUnitNames.add(unit);
        FunctionalUnit functionalUnit;
        if(unit.equals(FunctionalUnit.OPAL_INSTANCE)) {
          functionalUnit = FunctionalUnit.OPAL;
        } else {
          functionalUnit = resolveFunctionalUnit(unit);
        }

        units.add(functionalUnit);
      } else {
        throw new InvalidRequestException("DuplicateFunctionalUnitNames");
      }
    }
    return units;
  }
}
