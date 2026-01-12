package types;

public class TypeClass extends Type
{
    public TypeClass father;
    public TypeClassVarDecList dataMembers;
    
    public TypeClass(TypeClass father, String name, TypeClassVarDecList dataMembers)
    {
        super(name);
        this.father = father;
        this.dataMembers = dataMembers;
    }

    /*==================================================================*/
    /*                    TYPE CHECKS                                   */
    /*==================================================================*/
    @Override
    public boolean isClass() { return true; }



    /** Check if this class is ancestor of other */
    public boolean isAncestorOf(TypeClass other)
    {
        if (other == null) 
        {
            return false;
        }
        TypeClass current = other.father;
        while (current != null)
        {
            if (current == this)
            {
                return true;
            }
            current = current.father;
        }
        return false;
    }
    /** Check if this class is subclass of other (or same class) */
    public boolean isSubclassOf(TypeClass other)
    {
        if (other == null) return false;
        if (this == other) return true;
        return other.isAncestorOf(this);
    }
    

    /** Find member in this class only */
    public TypeClassVarDec findMember(String memberName)
    {
        for(TypeClassVarDecList it = dataMembers; it != null; it = it.tail)
        {
            TypeClassVarDec dec = it.head;
            if (dec != null && dec.name.equals(memberName))            
            {
                return dec;
            }
        }
        return null;
    }

    /** Find member in this class OR parent classes */
    public TypeClassVarDec findMemberInHierarchy(String memberName)
    {
        for (TypeClass current = this; current != null; current = current.father)
        {
            TypeClassVarDec dec = current.findMember(memberName);
            if (dec != null)
            {
                return dec;
            }
        }
        return null;
    }

    /** Find only fields (not methods) in hierarchy */
    public TypeClassVarDec findFieldInHierarchy(String fieldName)
    {
        for (TypeClass current = this; current != null; current = current.father)
        {
            for(TypeClassVarDecList it = current.dataMembers; it != null; it = it.tail)
            {
                TypeClassVarDec dec = it.head;
                if (dec == null) continue;
                boolean isMethod = (dec.t instanceof TypeFunction);
                if (!isMethod && dec.name.equals(fieldName)) 
                {
                    return dec;
                }
            }
        }
        return null;
    }
}
