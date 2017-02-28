for odir in ${2}*; do
    if [ -d $odir ]; then
        rm -rf $odir
    fi
done
/usr/local/hadoop/bin/hadoop com.sun.tools.javac.Main -Xlint:unchecked -Xdiags:verbose ShortestPaths*.java && \
    jar cf sp.jar ShortestPaths*.class && \
    /usr/local/hadoop/bin/hadoop jar sp.jar ShortestPaths $1 $2 $3
