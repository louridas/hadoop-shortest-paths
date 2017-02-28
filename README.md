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


