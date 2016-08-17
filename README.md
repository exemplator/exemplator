# Exemplator

This repository holds the Exemplator server.

### What is Exemplator?
Exemplator is a Java coding assistant that takes a function/code as input and searches GitHub for code snippets that use that function/code.
Thus you see how other people use the function/code you were looking for. 

The idea is to have a tool that quickly explains how APIs are used through examples and not through mediocre documentation. 


### How does it work?
1. Exemplator takes the given code and its type (e.g. the method ```"close"``` from ```"java.io.InputStream"```) and searches GitHub for Java files that include the code and the type. Then it sorts all results by popularity and passes it on to the parser.

2. The parser parses each returned file to build an AST. The AST is used to determine whether the code is actually of the type that was given or whether the code is of some other type (e.g. the method ```"close"``` could also belong to ```"java.util.Scanner"```). Only examples of the correct type are kept and returned to the user. 
