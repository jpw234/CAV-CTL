# CAV-CTL

Format for Kripke structure definitions:

First line: integer representing # states - 1-indexed, positive  
Next lines: two integers with a space inbetween representing directed edge from first to second integer  
Next line: a lone "s" to indicate the start states  
Next line: the start states (integers), with spaces between them  
Next line: integer representing # atomic propositions - 1-indexed  
Next lines: integer representing state, then integers representing props fulfilled in that state  
EOF

An example can be found in kripke.txt, which represents the structure in kripke-simple.png  
Parser currently does not support named states or propositions

Format for CTL formula files:

Each line in such a file is presumed to be a CTL formula.  
CTL formula are a series of tokens separated by whitespace ending in a newline.  
Basic propositions are represented by integers.  
The grammar for CTL formula is as follows:  
| P P (or)  
& P P (and)  
N P (not)  
G P (EG)  
X P (EX)  
U P P (EU)  
F P (EF)  
AX P  
AG P  
AF P  
AR P P  

An example list of CTL formulae is given in formulae.txt.  
Other propositions are not currently supported. All propositions are converted to EG/EX/EU under the hood.

To use this tool, compile the Java files and run CTL_Checker. Pass in a path to a legal Kripke structure definition and a legal list of CTL formulae.