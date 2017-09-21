curl http://www.datavyu.org/docs/user-guide.pdf > packaged_docs/user-guide.pdf
# The arguments 'package' and 'appbundle:bundle' are there to create an application bundle
mvn clean package appbundle:bundle -U -Dmaven.test.skip=true jfx:native
