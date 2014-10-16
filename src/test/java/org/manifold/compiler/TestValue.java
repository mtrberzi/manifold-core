package org.manifold.compiler;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;
import org.manifold.compiler.middle.SchematicException;

public class TestValue {

  class FacadeValue extends Value {

    public FacadeValue(Value type, Integer schematicTypeID) {
      super(type, schematicTypeID);
    }

    @Override
    public boolean isElaborationtimeKnowable() {
      return false;
    }

    @Override
    public boolean isRuntimeKnowable() {
      return false;
    }

    @Override
    public void accept(SchematicValueVisitor visitor) {
      throw new UnsupportedOperationException();
    }

  }

  @Test
  public void testRetrieveType() throws SchematicException {
    NodeTypeValue nDef = new NodeTypeValue(new HashMap<>(), new HashMap<>());
    Value dom = new NodeValue(nDef, new HashMap<>(), new HashMap<>());
    TypeValue expected = nDef;
    TypeValue actual = dom.getType();
    assertEquals(expected, actual);
  }

  @Test
  public void testGetSchematicTypeID() throws SchematicException {
    NodeTypeValue nDef = new NodeTypeValue(new HashMap<>(), new HashMap<>());
    Integer typeID = 42;
    Value v = new FacadeValue(nDef, typeID);
    assertEquals(typeID, v.getSchematicTypeID());
  }
}
