package org.manifold.compiler.middle;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.manifold.compiler.BooleanTypeValue;
import org.manifold.compiler.ConnectionType;
import org.manifold.compiler.ConnectionValue;
import org.manifold.compiler.ConstraintType;
import org.manifold.compiler.ConstraintValue;
import org.manifold.compiler.IntegerTypeValue;
import org.manifold.compiler.MultipleAssignmentException;
import org.manifold.compiler.MultipleDefinitionException;
import org.manifold.compiler.NodeTypeValue;
import org.manifold.compiler.NodeValue;
import org.manifold.compiler.PortTypeValue;
import org.manifold.compiler.RealTypeValue;
import org.manifold.compiler.StringTypeValue;
import org.manifold.compiler.TypeValue;
import org.manifold.compiler.UndeclaredIdentifierException;
import org.manifold.compiler.UndefinedBehaviourError;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;

/**
 * A Schematic contains all the information needed by the intermediate
 * representation. This includes type definitions, node/connection definitions,
 * node/connection instantiations, and constraint definitions/instantiations.
 */
public class Schematic {
  private final String name;

  public String getName() {
    return name;
  }

  // Type identifiers for this schematic;
  // indexed by the (string) type-name of the object.
  private final BiMap<String, Integer> typeIdentifiers;
  private Integer nextTypeIdentifier = 1;

  // Specific kinds of object type.
  private final Map<String, Integer> userDefinedTypes;
  private final Map<String, Integer> portTypes;
  private final Map<String, Integer> nodeTypes;
  private final Map<String, Integer> connectionTypes;
  private final Map<String, Integer> constraintTypes;

  // Object types defined in this schematic;
  // indexed by the (integer) type-identifier.
  private final Map<Integer, TypeValue> typeDefinitions;

  // Maps containing instantiated objects for this schematic; they are all
  // indexed by the (string) instance-name of the object.
  private final Map<String, NodeValue> nodes;
  private final Map<NodeValue, String> reverseNodeMap;
  private final Map<String, ConnectionValue> connections;
  private final Map<ConnectionValue, String> reverseConnectionMap;
  private final Map<String, ConstraintValue> constraints;
  private final Map<ConstraintValue, String> reverseConstraintMap;

  public Schematic(String name) {
    this.name = name;

    this.typeIdentifiers = HashBiMap.create();
    this.typeDefinitions = new HashMap<>();

    this.userDefinedTypes = new HashMap<>();
    this.portTypes = new HashMap<>();
    this.nodeTypes = new HashMap<>();
    this.connectionTypes = new HashMap<>();
    this.constraintTypes = new HashMap<>();

    populateDefaultType();

    this.nodes = new HashMap<>();
    this.reverseNodeMap = new HashMap<>();
    this.connections = new HashMap<>();
    this.reverseConnectionMap = new HashMap<>();
    this.constraints = new HashMap<>();
    this.reverseConstraintMap = new HashMap<>();
  }

  /*
   * Add "library standard" type definitions for basic types such as integer,
   * string, and boolean.
   */
  private void populateDefaultType() {
    TypeValue boolType = BooleanTypeValue.getInstance();
    TypeValue intType = IntegerTypeValue.getInstance();
    TypeValue stringType = StringTypeValue.getInstance();
    TypeValue realType = RealTypeValue.getInstance();

    try {
      addUserDefinedType("Bool", boolType);
      addUserDefinedType("Int", intType);
      addUserDefinedType("String", stringType);
      addUserDefinedType("Real", realType);
    } catch (MultipleDefinitionException mde) {
      // this should not actually be possible unless there is something wrong
      // with the compiler itself
      throw new UndefinedBehaviourError(
          "could not create default type definitions (" + mde.getMessage()
              + ")");
    }
  }

  private Integer addTypeDefinition(String typename, TypeValue typedef)
      throws MultipleDefinitionException {
    if (typeIdentifiers.containsKey(typename)) {
      throw new MultipleDefinitionException("type-definition", typename);
    }
    Integer allocatedTypeID = this.nextTypeIdentifier;
    this.nextTypeIdentifier += 1;
    typeIdentifiers.put(typename, allocatedTypeID);
    typeDefinitions.put(allocatedTypeID, typedef);
    return allocatedTypeID;
  }

  public Integer getTypeIdentifier(String typename)
      throws UndeclaredIdentifierException {
    if (typeIdentifiers.containsKey(typename)) {
      return typeIdentifiers.get(typename);
    } else {
      throw new UndeclaredIdentifierException(typename);
    }
  }

  public String getTypename(Integer typeID) {
    if (typeIdentifiers.containsValue(typeID)) {
      return typeIdentifiers.inverse().get(typeID);
    } else {
      throw new NoSuchElementException("unknown type ID #" + typeID.toString());
    }
  }

  public TypeValue getTypeDefinition(Integer typeID) {
    if (typeDefinitions.containsKey(typeID)) {
      return typeDefinitions.get(typeID);
    } else {
      throw new NoSuchElementException("unknown type ID #" + typeID.toString());
    }
  }

  public void addUserDefinedType(String typename, TypeValue td)
      throws MultipleDefinitionException {
    Integer typeID = addTypeDefinition(typename, td);
    userDefinedTypes.put(typename, typeID);
  }

  public TypeValue getUserDefinedType(String typename)
      throws UndeclaredIdentifierException {
    if (userDefinedTypes.containsKey(typename)) {
      return typeDefinitions.get(userDefinedTypes.get(typename));
    } else {
      throw new UndeclaredIdentifierException(typename);
    }
  }

  public void addPortType(String typename, PortTypeValue portType)
      throws MultipleDefinitionException {
    Integer typeID = addTypeDefinition(typename, portType);
    portTypes.put(typename, typeID);
  }

  public PortTypeValue getPortType(String typename)
      throws UndeclaredIdentifierException {
    if (portTypes.containsKey(typename)) {
      return (PortTypeValue) typeDefinitions.get(portTypes.get(typename));
    } else {
      throw new UndeclaredIdentifierException(typename);
    }
  }

  public void addNodeType(String typename, NodeTypeValue nd)
      throws MultipleDefinitionException {
    Integer typeID = addTypeDefinition(typename, nd);
    nodeTypes.put(typename, typeID);
  }

  public NodeTypeValue getNodeType(String typename)
      throws UndeclaredIdentifierException {

    if (nodeTypes.containsKey(typename)) {
      return (NodeTypeValue) typeDefinitions.get(nodeTypes.get(typename));
    } else {
      throw new UndeclaredIdentifierException(typename);
    }
  }

  public void addConnectionType(String typename, ConnectionType cd)
      throws MultipleDefinitionException {
    Integer typeID = addTypeDefinition(typename, cd);
    connectionTypes.put(typename, typeID);
  }

  public ConnectionType getConnectionType(String typename)
      throws UndeclaredIdentifierException {
    if (connectionTypes.containsKey(typename)) {
      return (ConnectionType) typeDefinitions.get(
          connectionTypes.get(typename));
    } else {
      throw new UndeclaredIdentifierException(typename);
    }
  }

  public void addConstraintType(String typename, ConstraintType cd)
      throws MultipleDefinitionException {
    Integer typeID = addTypeDefinition(typename, cd);
    constraintTypes.put(typename, typeID);
  }

  public ConstraintType getConstraintType(String typename)
      throws UndeclaredIdentifierException {
    if (constraintTypes.containsKey(typename)) {
      return (ConstraintType) typeDefinitions.get(
          constraintTypes.get(typename));
    } else {
      throw new UndeclaredIdentifierException(typename);
    }
  }

  public void addNode(String instanceName, NodeValue node)
      throws MultipleAssignmentException {
    if (nodes.containsKey(instanceName) || reverseNodeMap.containsKey(node)) {
      throw new MultipleAssignmentException("node", instanceName);
    }
    nodes.put(instanceName, node);
    reverseNodeMap.put(node, instanceName);
  }

  public NodeValue getNode(String instanceName)
      throws UndeclaredIdentifierException {
    if (nodes.containsKey(instanceName)) {
      return nodes.get(instanceName);
    } else {
      throw new UndeclaredIdentifierException(instanceName);
    }
  }

  public String getNodeName(NodeValue instance) {
    if (reverseNodeMap.containsKey(instance)) {
      return reverseNodeMap.get(instance);
    }
    throw new NoSuchElementException();
  }

  public void addConnection(String instanceName, ConnectionValue conn)
      throws MultipleAssignmentException {
    if (connections.containsKey(instanceName)) {
      throw new MultipleAssignmentException("connection", instanceName);
    }
    connections.put(instanceName, conn);
    reverseConnectionMap.put(conn, instanceName);
  }

  public ConnectionValue getConnection(String instanceName)
      throws UndeclaredIdentifierException {
    if (connections.containsKey(instanceName)) {
      return connections.get(instanceName);
    } else {
      throw new UndeclaredIdentifierException(instanceName);
    }
  }

  public String getConnectionName(ConnectionValue instance) {
    if (reverseConnectionMap.containsKey(instance)) {
      return reverseConnectionMap.get(instance);
    }
    throw new NoSuchElementException();
  }

  public void addConstraint(String instanceName, ConstraintValue constraint)
      throws MultipleAssignmentException {
    if (constraints.containsKey(instanceName)) {
      throw new MultipleAssignmentException("constraint", instanceName);
    }
    constraints.put(instanceName, constraint);
    reverseConstraintMap.put(constraint, instanceName);
  }

  public ConstraintValue getConstraint(String instanceName)
      throws UndeclaredIdentifierException {
    if (constraints.containsKey(instanceName)) {
      return constraints.get(instanceName);
    } else {
      throw new UndeclaredIdentifierException(instanceName);
    }
  }

  public String getConstraintName(ConstraintValue instance) {
    if (reverseConstraintMap.containsKey(instance)) {
      return reverseConstraintMap.get(instance);
    }
    throw new NoSuchElementException();
  }

  public Map<String, TypeValue> getUserDefinedTypes() {
    Map<String, TypeValue> results = new HashMap<>();
    for (Map.Entry<String, Integer> typedef : userDefinedTypes.entrySet()) {
      results.put(typedef.getKey(), typeDefinitions.get(typedef.getValue()));
    }
    return ImmutableMap.copyOf(results);
  }

  public Map<String, PortTypeValue> getPortTypes() {
    Map<String, PortTypeValue> results = new HashMap<>();
    for (Map.Entry<String, Integer> typedef : portTypes.entrySet()) {
      results.put(typedef.getKey(),
          (PortTypeValue) typeDefinitions.get(typedef.getValue()));
    }
    return ImmutableMap.copyOf(results);
  }

  public Map<String, NodeTypeValue> getNodeTypes() {
    Map<String, NodeTypeValue> results = new HashMap<>();
    for (Map.Entry<String, Integer> typedef : nodeTypes.entrySet()) {
      results.put(typedef.getKey(),
          (NodeTypeValue) typeDefinitions.get(typedef.getValue()));
    }
    return ImmutableMap.copyOf(results);
  }

  public Map<String, ConnectionType> getConnectionTypes() {
    Map<String, ConnectionType> results = new HashMap<>();
    for (Map.Entry<String, Integer> typedef : connectionTypes.entrySet()) {
      results.put(typedef.getKey(),
          (ConnectionType) typeDefinitions.get(typedef.getValue()));
    }
    return ImmutableMap.copyOf(results);
  }

  public Map<String, ConstraintType> getConstraintTypes() {
    Map<String, ConstraintType> results = new HashMap<>();
    for (Map.Entry<String, Integer> typedef : constraintTypes.entrySet()) {
      results.put(typedef.getKey(),
          (ConstraintType) typeDefinitions.get(typedef.getValue()));
    }
    return ImmutableMap.copyOf(results);
  }

  public Map<String, NodeValue> getNodes() {
    return ImmutableMap.copyOf(nodes);
  }

  public Map<String, ConnectionValue> getConnections() {
    return ImmutableMap.copyOf(connections);
  }

  public Map<String, ConstraintValue> getConstraints() {
    return ImmutableMap.copyOf(constraints);
  }

}
