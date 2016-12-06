import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import ctltree.*;

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