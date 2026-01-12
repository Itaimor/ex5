package ast;

import types.*;
import semantic.SemanticException;

public abstract class AstExp extends AstNode
{
	@Override
	public abstract Type semantMe() throws SemanticException;

	/**
	 * Try to evaluate this expression as a constant integer.
	 * Returns null if the expression is not a compile-time constant.
	 * Subclasses should override this method if they can be constant expressions.
	 */
	public Integer getConstantValue() {
		return null;
	}
}
