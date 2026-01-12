package ast;

import types.Type;
import semantic.SemanticException;

public abstract class AstCField extends AstNode {
	@Override
	public abstract Type semantMe() throws SemanticException;
}

