import java.io.*;
import java.util.Scanner;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import ctltree.*;

public class CTL_Checker {
	//the handler class for the CTL model checker
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		
		//get file encoding Kripke Structure, parse it, make the structure
		System.out.println("please provide a path to the .txt file that has a Kripke structure definition");
		System.out.println("example Kripke definitions are at the github for this project: https://github.com/jpw234/CAV-CTL");
		
		String path = in.nextLine();
		
		Kripke krip = null;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			
			String line = null;
			int curr_state = 0;
			
			int states = 0, props = 0;
			Map<Number, List<Number>> transitions = new HashMap<Number, List<Number>>(), labeler = new HashMap<Number, List<Number>>();
			List<Number> start_states = new ArrayList<Number>();
			
			/* Definition format:
			 * First line: integer representing # states - 1-indexed, positive
			 * Next lines: two integers with a space inbetween representing directed edge from first to second integer
			 * Next line: a lone "s" to indicate the start states
			 * Next line: the start states (integers), with spaces between them
			 * Next line: integer representing # atomic propositions - 1-indexed
			 * Next lines: integer representing state, then integers representing props fulfilled in that state
			 * EOF
			 */
			while((line = reader.readLine()) != null) {
				switch(curr_state){
				case 0: {//expect integer representing # states 
					states = Integer.parseInt(line);
					curr_state++;
					break;
				}
				case 1: {//expect either pair of ints, or "s" indicating increment state
					if(line.equals("s")) {curr_state++;}
					else {
						String[] s = line.split("\\s+");
						int source = Integer.parseInt(s[0]);
						int dest = Integer.parseInt(s[1]);
						if(transitions.get(source) == null) {
							ArrayList<Number> t = new ArrayList<Number>();
							t.add(dest);
							transitions.put(source, t);
						}
						else {
							List<Number> t = transitions.get(source);
							t.add(dest);
							transitions.put(source, t);
						}
					}
					break;
				}
				case 2: {//expect a single line, integers with spaces between, then inc state
					String[] s = line.split("\\s+");
					for(String k : s) {
						start_states.add(Integer.parseInt(k));
					}
					curr_state++;
					break;
				}
				case 3: {//expect integer representing # atomic propositions
					props = Integer.parseInt(line);
					curr_state++;
					break;
				}
				case 4: {//expect lines of form: int representing state, ints representing props, spaces between, then EOF
					String[] s = line.split("\\s+");
					int rel_state = Integer.parseInt(s[0]);
					ArrayList<Number> fulfilled = new ArrayList<Number>();
					for(int a = 1; a < s.length; a++) {
						fulfilled.add(Integer.parseInt(s[a]));
					}
					labeler.put(rel_state, fulfilled);
					break;
				}
				default: {//SHOULD NOT HAPPEN
					throw new Error("Something's fucky in the Kripke structure parser");
				}
				}
				
			}
			reader.close();
			krip = new Kripke(states, transitions, start_states, props, labeler);
			System.out.println("finished parsing K");
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found, please try again");
		}
		catch(IOException e) {
			System.out.println("IOException, uh oh");
		}
		catch(Exception e) {
			System.out.println("Unforeseen exception, maybe the Kripke structure is incorrectly defined?");
		}
		
		CTLProp c = new CTLProp(new CTLProp(2), CTLEnum.EX);
		System.out.println(krip.models(c));
	}
}
