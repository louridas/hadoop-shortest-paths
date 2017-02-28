# Hadoop Shortest Paths

A minimal Hadoop implementation of the single-source shortest paths
problem. The solution follows (not slavishly) the algorithm described
in Jimmy Lin and Chris Dyer, "Data-Intensive Text Processing with
MapReduce", published by Morgan and Claypool, 2010. For a detailed
explanation of how the algorithm works, check the original
publication. The following is a brief overview of the implementation
choices.

## Input Format

The shortest paths algorithm expects the graph in an adjacency list
representation. If this is not the case, the class
[`AdjacencyListCreator`](AdjacencyListCreator.java) can be used to
convert a graph from an "edge by line" format to an "adjacency list by
line" format. That is, given a graph represented in files whose lines
are:
```
FROM TO
```
`AdjacencyListCreator` converts the graph to a file with lines of the
form:
```
NODE NEIGHBOUR1,NEIGHOUR2,...
```

`AdjacencyList` creator ignores lines starting with `#`, assuming that
they are comments. As a separator for the input records it uses a
series of whitespace characters. The name of a node is a string, but
it may not contain whitespace, or commas.

## Mapper

The mapper, like the reducer, is called multiple times until the
algorithm finds the solution. The mapper outputs two different kinds
of records:
  * Records that describe the graph itself.
  * Records that describe, from each node, the path and the distance
    to its neighbours.

The very first time the mapper is called, its input will be the
adjacency list representation of the graph. The first kind of records
will then have the form:
```
NODE ADJACENCY-LIST DISTANCE FROM
```
The `DISTANCE` field will be zero if the node is equal to the source node, or
infinity for any other node. The `FROM` field is the source node if
the node is equal to the source node, or `null` for any other node.
The second kind of records will have the form:
```
NODE DISTANCE FROM
```
The `DISTANCE` field will be one if the `NODE` is a neighbour of the
source, or infinity otherwise. The `FROM` field will be the source, if
`NODE` is a neighbour of the source, or `null` otherwise.

In all subsequent times, the mapper will take as input the output of
the previous reducer, which is of the form:
```
NODE ADJACENCY-LIST DISTANCE PATH
```
The `DISTANCE` field will contain the distance to `NODE` found up to
that iteration, while `PATH` will contain the corresponding path.
The first kind of records will then be just the same as the input,
while the second kind of records will have the form:
```
NODE DISTANCE PATH
```
The `DISTANCE` field will be the updated distance to `NODE` from the
source, going through `PATH`.

