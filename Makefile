ANTLR_JAR = /usr/local/lib/antlr-4.5-complete.jar

%: all

all : $(ANTLR_JAR)
	./testscript Pool program $(INPUT_FILE)

$(ANTLR_JAR) :
	cd /usr/local/lib
	sudo curl -O https://www.antlr.org/download/antlr-4.5-complete.jar
	cd -;

clean :
	rm Pool*.class Pool*.java Pool*.tokens