package ctltree;
import java.util.List;
import java.util.ArrayList;

public class CTLProp{
	private CTLProp left = null, right = null;
	private int terminal;
	private CTLEnum type;
	private List<Number> satisfyingStates = new ArrayList<Number>();
	
	public CTLProp(CTLProp l, CTLProp r, CTLEnum t) {
		left = l;
		right = r;
		if(type != CTLEnum.AND && type != CTLEnum.OR && type != CTLEnum.EU) 
			throw new Error("illegal type to binary CTLProp");
		type = t;
	}
	
	public CTLProp(CTLProp l, CTLEnum t) {
		left = l;
		if(type != CTLEnum.NOT && type != CTLEnum.EG && type != CTLEnum.EX)
			throw new Error("illegal type to unary CTLProp");
	}
	
	public CTLProp(int s) {
		terminal = s;
		type = CTLEnum.TERMINAL;
	}
	
	public List<CTLProp> getChildren() {
		ArrayList<CTLProp> res = new ArrayList<CTLProp>();
		if(right == null) {
			if(left == null) {
				return null;
			}
			res.add(left);
			return res;
		}
		res.add(left);
		res.add(right);
		return res;
	}
	
	public int getProp() {
		return terminal;
	}
	
	public void addSatisfyingStates(List<Number> s) {
		satisfyingStates.addAll(s);
	}
	
	public void addSatisfyingState(Number n) {
		satisfyingStates.add(n);
	}
	
	public List<Number> getSatisfyingStates() {
		return satisfyingStates;
	}
	
	public CTLEnum getType() {
		return type;
	}
}
