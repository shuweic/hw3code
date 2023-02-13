package varlang;

public interface Value {
	public String toString();
	static class NumVal implements Value {
		private double _val;
	    public NumVal(double v) { _val = v; } 
	    public double v() { return _val; }
	    public String toString() { 
	    	int tmp = (int) _val;
	    	if(tmp == _val) return "" + tmp;
	    	return "" + _val; 
	    }
	}
	static class UnitVal implements Value {
		public static final UnitVal v = new UnitVal();
		public String toString() { return ""; }
	}
	static class DynamicError implements Value {
		private String message = "Unknown dynamic error.";
		public DynamicError(String message) { this.message = message; }
		public String toString() { return "" + message; }
	}
}
