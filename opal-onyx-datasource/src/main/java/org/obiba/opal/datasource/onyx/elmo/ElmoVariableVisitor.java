package org.obiba.opal.datasource.onyx.elmo;

import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.obiba.onyx.engine.variable.Category;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.impl.DefaultVariablePathNamingStrategy;
import org.obiba.opal.datasource.onyx.variable.DefaultVariableQNameStrategy;
import org.obiba.opal.datasource.onyx.variable.IVariableQNameStrategy;
import org.obiba.opal.datasource.onyx.variable.VariableVisitor;
import org.obiba.opal.elmo.OpalOntologyManager;
import org.obiba.opal.elmo.concepts.CategoricalVariable;
import org.obiba.opal.elmo.concepts.ContinuousVariable;
import org.obiba.opal.elmo.concepts.MissingCategory;
import org.obiba.opal.elmo.concepts.OccurrenceItem;
import org.obiba.opal.elmo.concepts.Opal;
import org.obiba.opal.elmo.concepts.dataValue;
import org.obiba.opal.elmo.concepts.hasCategory;
import org.openrdf.OpenRDFException;
import org.openrdf.concepts.owl.Class;
import org.openrdf.concepts.owl.DatatypeProperty;
import org.openrdf.concepts.owl.Ontology;
import org.openrdf.concepts.owl.Restriction;
import org.openrdf.elmo.sesame.SesameManager;

public class ElmoVariableVisitor implements VariableVisitor {

  final OpalOntologyManager opal;

  final SesameManager manager;

  final List<Handler> handlers = new LinkedList<Handler>();

  final IVariableQNameStrategy qnameStrategy;

  public ElmoVariableVisitor(String base, SesameManager manager) throws OpenRDFException, IOException {
    this.manager = manager;
    this.opal = new OpalOntologyManager();
    qnameStrategy = new DefaultVariableQNameStrategy(base, new DefaultVariablePathNamingStrategy());
    handlers.add(new CategoryHandler());
    handlers.add(new CategoricalHandler());
    handlers.add(new OccurrenceHandler());
    handlers.add(new ContinuousHandler());

    Ontology ontology = manager.create(QName.valueOf(base), Ontology.class);
    Ontology opalOntology = opal.getOpalNode(Opal.class, Ontology.class);
    ontology.getOwlImports().add(opalOntology);
  }

  public void visit(Variable variable) {
    for(Handler h : handlers) {
      if(h.handles(variable)) {
        h.handle(variable);
        break;
      }
    }

    for(Variable v : variable.getVariables()) {
      visit(v);
    }
  }

  public interface Handler {
    public boolean handles(Variable var);

    public void handle(Variable var);
  }

  public class CategoryHandler implements Handler {
    public void handle(Variable onyxVariable) {
      QName qname = qnameStrategy.getQName(onyxVariable);

      Category onyxCategory = (Category) onyxVariable;

      Class opalCategory = opal.getOpalClass(org.obiba.opal.elmo.concepts.Category.class);

      if(onyxCategory.getEscape() != null && onyxCategory.getEscape() == true) {
        opalCategory = opal.getOpalClass(MissingCategory.class);
      }
      Class opalOnyxVariable = manager.create(qname, Class.class);
      opalOnyxVariable.getRdfsSubClassOf().add(opalCategory);

      QName parentQName = qnameStrategy.getQName(onyxVariable.getParent());
      Class parentVariable = manager.find(Class.class, parentQName);

      for(Object c : parentVariable.getRdfsSubClassOf()) {
        if(c instanceof Restriction) {
          Restriction r = (Restriction) c;
          if(r.getOwlAllValuesFrom() != null) {
            Class allValues = (Class) r.getOwlAllValuesFrom();
            allValues.getOwlUnionOf().add(opalOnyxVariable);
          }
        }
      }
    }

    public boolean handles(Variable var) {
      return var instanceof Category;
    }
  }

  public class CategoricalHandler implements Handler {
    public void handle(Variable onyxVariable) {
      QName qname = qnameStrategy.getQName(onyxVariable);

      Class opalVariable = opal.getOpalClass(CategoricalVariable.class);
      Class opalOnyxVariable = manager.create(qname, Class.class);
      opalOnyxVariable.getRdfsSubClassOf().add(opalVariable);

      Restriction r = manager.create(Restriction.class);
      org.openrdf.concepts.owl.ObjectProperty hasCategory = opal.getOpalProperty(hasCategory.class);
      r.setOwlOnProperty(hasCategory);
      if(onyxVariable.isMultiple()) {
        r.setOwlMinCardinality(BigInteger.ONE);
      } else {
        r.setOwlCardinality(BigInteger.ONE);
      }
      opalOnyxVariable.getRdfsSubClassOf().add(r);

      r = manager.create(Restriction.class);
      Class union = manager.create(Class.class);
      org.openrdf.concepts.rdf.List<? extends Class> l = manager.create(org.openrdf.concepts.rdf.List.class);
      union.setOwlUnionOf(l);
      r.setOwlOnProperty(hasCategory);
      r.setOwlAllValuesFrom(union);
      opalOnyxVariable.getRdfsSubClassOf().add(r);
    }

    public boolean handles(Variable var) {
      return var.isCategorial();
    }
  }

  public class ContinuousHandler implements Handler {
    public void handle(Variable onyxVariable) {
      QName qname = qnameStrategy.getQName(onyxVariable);

      org.openrdf.concepts.owl.Class opalVariable = opal.getOpalClass(ContinuousVariable.class);

      org.openrdf.concepts.owl.Class opalOnyxVariable = manager.create(qname, org.openrdf.concepts.owl.Class.class);
      opalOnyxVariable.getRdfsSubClassOf().add(opalVariable);

      Restriction r = manager.create(Restriction.class);
      DatatypeProperty dataValue = opal.getOpalProperty(dataValue.class);
      r.setOwlOnProperty(dataValue);
      r.setOwlCardinality(BigInteger.ONE);
      opalOnyxVariable.getRdfsSubClassOf().add(r);

    }

    public boolean handles(Variable var) {
      return var.isCategorial() == false && var.getDataType() != null;
    }
  }

  public class OccurrenceHandler implements Handler {
    public void handle(Variable onyxVariable) {
      QName qname = qnameStrategy.getQName(onyxVariable);

      org.openrdf.concepts.owl.Class opalVariable = opal.getOpalClass(OccurrenceItem.class);

      org.openrdf.concepts.owl.Class opalOnyxVariable = manager.create(qname, org.openrdf.concepts.owl.Class.class);
      opalOnyxVariable.getRdfsSubClassOf().add(opalVariable);
    }

    public boolean handles(Variable var) {
      return var.isRepeatable();
    }
  }

}
