for odir in ${2}*; do
    if [ -d $odir ]; then
        rm -rf $odir
    fi
done
/usr/local/hadoop/bin/hadoop com.sun.tools.javac.Main -Xlint:unchecked -Xdiags:verbose AdjacencyListCreator.java && \
    jar cf alc.jar AdjacencyListCreator*.class && \
    /usr/local/hadoop/bin/hadoop jar alc.jar AdjacencyListCreator $1 $2
