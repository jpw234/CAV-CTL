package ctltree;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

public class CTLParser {
	
	public CTLParser() {
		
	}
	
	/* CTL statement grammar:
	 * A CTL statement is a proposition P
	 * A proposition P is:
	 * an integer (representing a base proposition)
	 * | P P (or)
	 * & P P (and)
	 * N P (not)
	 * G P (EG)
	 * X P (EX)
	 * U P P (EU)
	 * F P (EF)
	 * AX P
	 * AG P
	 * AF P
	 * AR P P
	 * 
	 * in order for the parser
	 */
	
	public List<CTLProp> parse(String filename) {
		ArrayList<CTLProp> res = new ArrayList<CTLProp>();
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			
			String line = null;
			Stack<String> stack = null;
			
			//each line is a CTL formula
			while((line = reader.readLine()) != null) {
				stack = new Stack<String>();
				
				//split on whitespace
				String[] strings = line.split("\\s+");
				
				for(int c = strings.length-1; c >= 0; c--) {
					stack.push(strings[c]);
				}
				
				//now recursive descent loop to parse the CTLProp
				res.add(parseExp(stack));
			}
			reader.close();
		}
		catch(FileNotFoundException e) {
			System.out.println("CTLParser error: file not found");
		}
		catch(IOException e) {
			System.out.println("CTLParser error: IOException");
		}
		return res;
	}
	
	private CTLProp parseExp(Stack<String> stack) {
		switch(stack.peek()) {
		case "|": {
			stack.pop(); //consume |
			return new CTLProp(parseExp(stack), parseExp(stack), CTLEnum.OR);
		}
		case "&": {
			stack.pop(); //consume &
			return new CTLProp(parseExp(stack), parseExp(stack), CTLEnum.AND);
		}
		case "N": {
			stack.pop(); //consume N
			return new CTLProp(parseExp(stack), CTLEnum.NOT);
		}
		case "G": {
			stack.pop(); //consume G
			return new CTLProp(parseExp(stack), CTLEnum.EG);
		}
		case "X": {
			stack.pop(); //consume X
			return new CTLProp(parseExp(stack), CTLEnum.EX);
		}
		case "U": {
			stack.pop(); //consume U
			return new CTLProp(parseExp(stack), parseExp(stack), CTLEnum.EU);
		}
		case "F": { //EF p = E (p | not p) U p
			stack.pop(); //consume X
			CTLProp p = parseExp(stack);
			return new CTLProp(new CTLProp(p, new CTLProp(p, CTLEnum.NOT), CTLEnum.OR), p, CTLEnum.EU);
		}
		case "AX": { //AX p = not (EX not p)
			stack.pop(); //consume AX
			CTLProp p = parseExp(stack);
			return new CTLProp(new CTLProp(new CTLProp(p, CTLEnum.NOT), CTLEnum.EX), CTLEnum.NOT);
		}
		case "AG": { //AG p = not (EF not p)
			stack.pop(); //consume AG
			CTLProp p = parseExp(stack);
			CTLProp notp = new CTLProp(p, CTLEnum.NOT);
			return new CTLProp(new CTLProp(new CTLProp(notp, new CTLProp(notp, CTLEnum.NOT), CTLEnum.OR), notp, CTLEnum.EU), CTLEnum.NOT);
		}
		case "AF": { //AF p = not (EG not p)
			stack.pop(); //consume AF
			CTLProp p = parseExp(stack);
			CTLProp notp = new CTLProp(p, CTLEnum.NOT);
			return new CTLProp(new CTLProp(notp, CTLEnum.EG), CTLEnum.NOT);
		}
		case "AR": { //AR p = not (E notp U notp)
			stack.pop(); //consume AR
			CTLProp p = parseExp(stack);
			CTLProp notp = new CTLProp(p, CTLEnum.NOT);
			return new CTLProp(new CTLProp(notp, notp, CTLEnum.EU), CTLEnum.NOT);
		}
		default: {//integer case
			return new CTLProp(Integer.parseInt(stack.pop()));
		}
		}
	}
}
