package varlang;
import static varlang.AST.*;
import static varlang.Value.*;

import java.util.List;
import java.util.ArrayList;

import varlang.AST.AddExp;
import varlang.AST.NumExp;
import varlang.AST.DivExp;
import varlang.AST.MultExp;
import varlang.AST.Program;
import varlang.AST.SubExp;
import varlang.AST.VarExp;
import varlang.AST.Visitor;
import varlang.Env.EmptyEnv;
import varlang.Env.ExtendEnv;
import varlang.Env.GlobalEnv;

public class Evaluator implements Visitor<Value> {
	Env initEnv = initialEnv(); //New for definelang

	Value valueOf(Program p) {
			return (Value) p.accept(this, initEnv);
	}

	@Override
	public Value visit(AddExp e, Env env) {
		List<Exp> operands = e.all();
		double result = 0;
		for(Exp exp: operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic type-checking
			result += intermediate.v(); //Semantics of AddExp in terms of the target language.
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(UnitExp e, Env env) {
		return new UnitVal();
	}

	@Override
	public Value visit(NumExp e, Env env) {
		return new NumVal(e.v());
	}

	@Override
	public Value visit(DivExp e, Env env) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env);
		double result = lVal.v(); 
		for(int i=1; i<operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			result = result / rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(MultExp e, Env env) {
		List<Exp> operands = e.all();
		double result = 1;
		for(Exp exp: operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic type-checking
			result *= intermediate.v(); //Semantics of MultExp.
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(Program p, Env env) {
		try {
			for(DefineDecl d: p.decls())
				d.accept(this, initEnv);
			return (Value) p.e().accept(this, initEnv);
		} catch (ClassCastException e) {
			return new DynamicError(e.getMessage());
		}
	}

	@Override
	public Value visit(SubExp e, Env env) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env);
		double result = lVal.v();
		for(int i=1; i<operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			result = result - rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(VarExp e, Env env) {
		// Previously, all variables had value 42. New semantics.
		return env.get(e.name());
	}

	@Override
	public Value visit(LetExp e, Env env) { // New for varlang.
		List<String> names = e.names();
		List<Exp> value_exps = e.value_exps();
		List<Value> values = new ArrayList<Value>(value_exps.size());

		for(Exp exp : value_exps)
			values.add((Value)exp.accept(this, env));

		Env new_env = env;
		for (int i = 0; i < names.size(); i++)
			new_env = new ExtendEnv(new_env, names.get(i), values.get(i));

		return (Value) e.body().accept(this, new_env);
	}
	@Override
	public Value visit(DefineDecl e, Env env) { // New for definelang.
		String name = e.name();
		Exp value_exp = e.value_exp();
		Value value = (Value) value_exp.accept(this, env);
		((GlobalEnv) initEnv).extend(name, value);
		return new Value.UnitVal();
	}
	private Env initialEnv() {
		GlobalEnv initEnv = new GlobalEnv();
		return initEnv;
	}

	@Override
	public Value visit(ModExp e, Env env) {
		List<Exp> operands = e.all();

		List<Integer> values = new ArrayList<>();
		for (int i = 0; i < operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			if (i > 0) {
				if (rVal.v() == 0 || rVal.v() % 1 > 0) {
					return new DynamicError("Zero or float operator for mod is not permitted on RHS.");
				}
			} else {
				if (rVal.v() % 1 > 0) {
					return new DynamicError("Float operator for mod is not permitted. ");
				}
			}
			values.add((int) rVal.v());
		}

		int result = values.get(0);
		for (int i = 1; i < values.size(); i++) {
			result = mod(result, values.get(i));
		}

		return new NumVal(result);
	}

	private int mod(int a, int b) {

		return a % b;

	}
}
