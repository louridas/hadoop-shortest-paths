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

The mapper, ['ShortestPathsMapper'](ShortestPathsMapper), like the
reducer, is called multiple times until the algorithm finds the
solution. The mapper outputs two different kinds of records:
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

## Reducer

The reducer, [`ShortestPathsReducer`](ShortestPathsReducer), takes as
input the two different kinds of records output by the mapper and
finds the best path to a node from the source, if it exists. Then it
outputs the graph, in adjacency list representation, with the best
distance and path from the source to each node found to this point, as
described above.

To determine whether further iterations are needed, the Reducer uses a
counter, set by the Hadoop driver program. The driver counts the
number of updated paths. If the reducer is not able to update any
path, then there does not need to be any further iteration.

Note that this is different from the original description of the
algorithm by Lin and Dyer, where they count they number of paths that
are still at infinity. That approach works for connected graphs, but
will fail if a graph is not connected. Counting the number of updated
paths avoids that, at a cost of an extra iteration: we need an extra
MapReduce pass to notice that nothing can be updated.

## Driver program

The driver program, [`ShortestPaths`](ShortestPaths.java), just plugs in
the mapper and the reducer and sets up the counter that will be used
to count the number of updated paths in each iteration.

## Record representation

The different kinds of records are all represented as text. Therefore
the mapper and the reducer determine what kind of record they read
via string processing, that is, splitting and counting the number of
fields. This may not be the most efficient way, but it is the easiest
to express in code, without having to resort to more advanced Hadoop
programming for reading and writing custom types.

## Scripts

Two minimal scripts can be used in a Linux-like command line to
compile and run the programs:
  * [`alc.sh`](alc.sh) (adjacency list creator) to create the
    adjacency list representation.
  * [`sp.sh`](sp.sh) (shortest paths) to run the shortest paths
    program.
    
Of course, in a normal production environment one would use a proper
build system, but these two will do for this code.

## Testing

If your graph is small enough to fit in your computer's memory, you
had better use a traditional algorighm like Dijkstra's. If not, you
can use this program, for example, it can easily solve the
[Google web graph](https://snap.stanford.edu/data/web-Google.html)
that was used in a 2002 programming contest.
