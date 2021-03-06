import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import ctltree.*;
import java.util.Stack;
import java.lang.Math;

public class Kripke {
	
	/* A Kripke structure is effectively a finite state machine augmented with labels.
	 * The formal definition of a Kripke structure <S, T, S0, A, L> is as follows:
	 * 
	 * S: the (finite, named) set of states of the structure
	 * T: the transition relation of type S x S, describing edges between states (directed)
	 * S0: the set of start states
	 * A: a set of atomic propositions
	 * L: S -> 2^A labels which propositions hold at each state
	 */
	
	private int states;
	private Map<Number, List<Number>> transitions;
	private List<Number> start_states;
	private int props;
	private Map<Number, List<Number>> labeler;
	
	public Kripke(int s, Map<Number, List<Number>> t, List<Number> ss, 
				  int p, Map<Number, List<Number>> l) {
		states = s;
		transitions = t;
		start_states = ss;
		props = p;
		labeler = l;
	}
	
	//does the Kripke structure model the given proposition
	//this holds if the proposition is satisfied by all initial states of K
	public boolean models(CTLProp p) {
		boolean res = true;
		//true iff all start states model p
		for(Number k : start_states) {
			res = res & models(p, k);
		}
		return res;
	}
	
	//does the Kripke structure model the given proposition at the given state
	public boolean models(CTLProp p, Number s) {
		boolean res = false;
		
		switch(p.getType()) {
		case TERMINAL: {
			if(labeler.get(s) == null) return false;
			res = labeler.get(s).contains(p.getProp());
			return res;
		}
		case OR: {
			res = (models(p.getChildren().get(0), s) || models(p.getChildren().get(1), s));
			return res;
		}
		case AND: {
			res = (models(p.getChildren().get(0), s) && models(p.getChildren().get(1), s));
			return res;
		}
		case NOT: {
			res = !(models(p.getChildren().get(0), s));
			return res;
		}
		case EX: { //K, s0 models EX p iff there exists an s1 such that s0 -> s1 and K, s1 models p
			List<Number> nexts = transitions.get(s);
			if(nexts == null) return false;
			for(Number n : nexts) {
				res = res || (models(p.getChildren().get(0), n));
			}
			return res;
		}
		case EU: {//K, s0 models EpUv iff there exists an s' such that s0 -> ... -> s', K, s' models v, and s0 -> ... all model p
			//if s models v EpUv is trivially true
			if(models(p.getChildren().get(1), s)) res = true;
			//but if s doesn't model v or p EpUv is trivially false
			else if(!models(p.getChildren().get(0), s)) res = false;
			else {
				//approach: restrict graph to states where p OR v are true
				//see if there is a path on this graph from state s to a state modeling v
				ArrayList<Number> legal = new ArrayList<Number>();
				ArrayList<Number> fin = new ArrayList<Number>();
				ArrayList<Number> discovered = new ArrayList<Number>();
				for(int a = 1; a <= states; a++) {
					if(models(p.getChildren().get(1), a)) {
						legal.add(a);
						fin.add(a);
					}
					else if(models(p.getChildren().get(0), a)) {
						legal.add(a);
					}
				}
				
				//simple DFS using only nodes in legal
				Stack<Number> stack = new Stack<Number>();
				stack.push(s);
				while(!stack.isEmpty()) {
					Number curr = stack.pop();
					if(!discovered.contains(curr)) {
						discovered.add(curr);
						List<Number> nexts = transitions.get(curr);
						
						if(nexts == null) break;
						
						for(Number a : nexts) {
							if(fin.contains(a)) {
								res = true;
								break;
							}
							else if(legal.contains(a)) {
								stack.push(a);
							}
						}
						if(res) break;
					}
				}
			}
			return res;
		}
		case EG: {//K, s0 models EG p iff there exists an si, sj, ... such that s0 -> ... -> si -> sj -> ... and si, sj, ... model p
			//using scc(p) as "fin" as above, use DFS to find if there's a path from s to a state in fin
			List<Number> fin = sccProp(p.getChildren().get(0));
			ArrayList<Number> discovered = new ArrayList<Number>();
			
			//simple DFS
			Stack<Number> stack = new Stack<Number>();
			stack.push(s);
			while(!stack.isEmpty()) {
				Number curr = stack.pop();
				if(!discovered.contains(curr)) {
					discovered.add(curr);
					List<Number> nexts = transitions.get(curr);
					
					if(nexts == null) break;
					
					for(Number a : nexts) {
						if(fin.contains(a)) {
							res = true;
							break;
						}
						else {
							stack.push(a);
						}
					}
					if(res) break;
				}
			}
			return res;
		}
		default: return false;
		}
	}
	
	public List<Number> modelling_states(CTLProp p) {
		ArrayList<Number> res = new ArrayList<Number>();
		for(int a = 1; a <= states; a++) {
			if(models(p, a)) res.add(a);
		}
		return res;
	}
	
	//returns a list containing every state that is in a SCC, such that all states in that SCC satisfy p
	private List<Number> sccProp(CTLProp p) {
		List<Number> acceptable = modelling_states(p);
		ArrayList<Number> res = new ArrayList<Number>();
		//find all SCCs with Tarjan's algorithm
		ArrayList<ArrayList<Number>> allSCCs = scc(acceptable);
		//for each SCC check if acceptable entirely contains it
		for(List<Number> scc : allSCCs) {
			for(Number n : scc) {
				if(!res.contains(n)) res.add(n);
			}
		}
		//return res
		return res;
	}

	private HashMap<Number, Number> indexes = null;
	private int index = 0;
	private HashMap<Number, Number> lowlinks = null;
	private Stack<Number> stack = null;
	private ArrayList<ArrayList<Number>> sccs = null;
	
	//returns a list of SCCs in the Kripke structure (SCCs are lists of states)
	//Only the SCCs that are composed of entirely states in the "acceptable" parameter
	//based on https://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm
	private ArrayList<ArrayList<Number>> scc(List<Number> acceptable) {
		indexes = new HashMap<Number, Number>();
		index = 0;
		lowlinks = new HashMap<Number, Number>();
		stack = new Stack<Number>();
		sccs = new ArrayList<ArrayList<Number>>();
		
		for(int a = 1; a <= states; a++) {
			if(!acceptable.contains(a)) continue;
			if(indexes.get(a) == null) {
				sccinternal(a, acceptable);
			}
		}
		
		return sccs;
	}
	
	private void sccinternal(int v, List<Number> acceptable) {
		indexes.put(v, index);
		lowlinks.put(v, index);
		index++;
		stack.push(v);
		
		if(transitions.get(v) == null) return;
		
		for(Number w : transitions.get(v)) {
			if(!acceptable.contains(w)) continue;
			if(indexes.get(w) == null) {
				sccinternal((int)w, acceptable);
				lowlinks.put(v, Math.min((int)lowlinks.get(v), (int)lowlinks.get(w)));
			}
			else if(stack.contains(w)) {
				lowlinks.put(v, Math.min((int)lowlinks.get(v), (int)indexes.get(w)));
			}
		}
		
		if(lowlinks.get(v) == indexes.get(v)) {
			ArrayList<Number> newSCC = new ArrayList<Number>();
			int w;
			do {
				w = (int)stack.pop();
				newSCC.add(w);
			} while(w != v);
			sccs.add(newSCC);
		}
	}
}