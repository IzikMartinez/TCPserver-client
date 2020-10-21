JFLAGS=-g
JC=javac
DIR=$(PWD)
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
CLASSES = \
	RBACMonitor.java \
	PasswordTable.java \
	ProtocolMessage.java \
    SecurityServer.java \
    SecurityClient.java \
    AbstractProtocolHandler.java \
    ProtocolMessageWindow.java \
    ServerProtocolHandler.java \
    ClientProtocolHandler.java
all: classes; 
classes: $(CLASSES:.java=.class)
clean:
	$(RM) *.class 