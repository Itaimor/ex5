package ast;

import types.*;
import semantic.SemanticException;

public abstract class AstDec extends AstNode
{
    @Override
    public abstract Type semantMe() throws SemanticException;
}
