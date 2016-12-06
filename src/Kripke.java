import java.util.Map;

import javax.print.attribute.standard.Finishings;

import java.util.List;
import java.util.ArrayList;
import ctltree.*;
import java.util.Stack;

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
	
	/* Functions to write:
	 * Given state find all states that can reach it
	 * find Strongly Connected Components of the graph
	 */
	
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
			res = labeler.get(s).contains(p.getProp());
			if(res) p.addSatisfyingState(s);
			return res;
		}
		case OR: {
			res = (models(p.getChildren().get(0), s) || models(p.getChildren().get(1), s));
			if(res) p.addSatisfyingState(s);
			return res;
		}
		case AND: {
			res = (models(p.getChildren().get(0), s) && models(p.getChildren().get(1), s));
			if(res) p.addSatisfyingState(s);
			return res;
		}
		case NOT: {
			res = !(models(p.getChildren().get(0), s));
			if(res) p.addSatisfyingState(s);
			return res;
		}
		case EX: { //K, s0 models EX p iff there exists an s1 such that s0 -> s1 and K, s1 models p
			List<Number> nexts = transitions.get(s);
			for(Number n : nexts) {
				res = res || (models(p.getChildren().get(0), n));
			}
			if(res) p.addSatisfyingState(s);
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
						for(Number a : nexts) {
							if(fin.contains(a)) {
								res = true;
								break;
							}
							else if(legal.contains(a)) {
								stack.push(a);
							}
						}
					}
				}
				res = false;
			}
			if(res) p.addSatisfyingState(s);
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
}