package org.manifold.compiler;


public abstract class Value {

  private TypeValue type = null;
  private Integer schematicTypeID = null;

  protected Value(Value type) {
    // Allow a "null" type so that TypeTypeValue can escape the circular
    // dependency on itself (it will override getType)
    assert type instanceof TypeValue || type == null;
    this.type = (TypeValue) type;
    this.schematicTypeID = null;
  }

  protected Value(Value type, Integer schematicTypeID) {
 // Allow a "null" type so that TypeTypeValue can escape the circular
    // dependency on itself (it will override getType)
    assert type instanceof TypeValue || type == null;
    this.type = (TypeValue) type;
    this.schematicTypeID = schematicTypeID;
  }

  public TypeValue getType() {
    return this.type;
  }

  public Integer getSchematicTypeID() {
    return this.schematicTypeID;
  }

  /*
   * Executed during formal verification pass. Any errors should result in an
   * exception.
   */
  public void verify() throws Exception {}

  /*
   * Returns true if this value can be known during elaboration.
   * Either this or isRuntimeKnowable or both must return true.
   */
  public abstract boolean isElaborationtimeKnowable();

  /*
   * Returns true if this value is able to be represented the intermediate.
   */
  public abstract boolean isRuntimeKnowable();

  /*
   * Allows SchematicValueVisitors to process this Value.
   * Must be overridden in every subclass of Value that can be instantiated,
   * usually with code of the form "visitor.visit(this);".
   */

  public abstract void accept(SchematicValueVisitor visitor);

}
