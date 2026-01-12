package ast;

import types.*;
import semantic.SemanticException;

public abstract class AstStmt extends AstNode
{
	@Override
	public abstract Type semantMe() throws SemanticException;
}
