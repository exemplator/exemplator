# Exemplator

This repository holds the Exemplator server. The website can be found [here](http://exemplator.xyz).

### What is Exemplator?
Exemplator is a search engine for Java code snippets. It takes a function/code as input and searches GitHub for code snippets that use that function/code.
Thus you see how other people use the function/code. 

The idea is to have a tool that quickly explains how APIs are used through examples and not through mediocre documentation. 


### How does it work?
1. Exemplator takes the given code and its type and searches GitHub for Java files that include the code and type.

2. The parser builds an AST out of the file to determine if the function has the type that we want.
