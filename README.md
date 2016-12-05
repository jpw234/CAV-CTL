# CAV-CTL

Format for Kripke structure definitions:

First line: integer representing # states - 1-indexed, positive  
Next lines: two integers with a space inbetween representing directed edge from first to second integer  
Next line: a lone "s" to indicate the start states  
Next line: the start states (integers), with spaces between them  
Next line: integer representing # atomic propositions - 1-indexed  
Next lines: integer representing state, then integers representing props fulfilled in that state  
EOF

An example can be found in kripke.txt  
Parser currently does not support named states or propositions
