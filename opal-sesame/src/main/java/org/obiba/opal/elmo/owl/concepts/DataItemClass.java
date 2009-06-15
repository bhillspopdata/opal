/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.elmo.owl.concepts;

import java.util.Set;

import org.obiba.opal.elmo.concepts.Opal;
import org.openrdf.elmo.annotations.rdf;

/**
 * 
 */
public interface DataItemClass extends OpalClass {

  @rdf(Opal.NS + "parent")
  public DataItemClass getParent();

  public void setParent(DataItemClass parent);

  @rdf(Opal.NS + "child")
  public Set<DataItemClass> getChildren();

  public void setChildren(Set<DataItemClass> children);

  @rdf(Opal.NS + "multiple")
  public boolean isMultiple();

  public void setMultiple(boolean multiple);

  @rdf(Opal.NS + "condition")
  public String getCondition();

  public void setCondition(String condition);

  @rdf(Opal.NS + "occurrence")
  public String getOccurrence();

  public void setOccurrence(String occurrence);

  @rdf(Opal.NS + "source")
  public String getSource();

  public void setSource(String source);

  @rdf(Opal.NS + "validation")
  public String getValidation();

  public void setValidation(String validation);
}
