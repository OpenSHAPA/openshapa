curl http://www.datavyu.org/docs/user-guide.pdf > packaged_docs/user-guide.pdf
# Add the -X flag to get debug information
# Removed jar:jar goal because package already includes that goal and it was run TWICE!
# See: https://stackoverflow.com/questions/40964500/maven-jar-plugin-3-0-2-error-you-have-to-use-a-classifier-to-attach-supplementa
mvn -Prelease,win-package -Dmaven.test.skip=true clean package launch4j:launch4j assembly:assembly