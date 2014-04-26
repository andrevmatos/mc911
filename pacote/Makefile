#
# pacote v04
# 
# Como compilar:
# $ make
#
# Como executar o compilador:
# $ make run INPUT=test/teste.java OUTPUT=teste.s
#
# Como gerar bytecode:
# $ make bytecode INPUT=teste.s OUTPUT=teste.bc
#
# Como executar o bytecode:
# $ lli teste.bc
#

SOURCES = $(wildcard src/**/*.java)
CLASSES = $(SOURCES:.java=.class)

all: $(CLASSES)

%.class: %.java
	javac -classpath src:lib/projeto2.jar $<

run:
	java -classpath src:lib/projeto2.jar main/Main $(INPUT) $(OUTPUT)

bytecode:
	llvm-as $(INPUT) -o $(OUTPUT)

clean:
	rm -f src/llvm/*.class src/llvmast/*.class


