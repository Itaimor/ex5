package types;

public class TypeClassVarDecList
{
    public TypeClassVarDec head;
    public TypeClassVarDecList tail;
    
    public TypeClassVarDecList(TypeClassVarDec head, TypeClassVarDecList tail)
    {
        this.head = head;
        this.tail = tail;
    }

    /** Find member by name in this list */
    public TypeClassVarDec find(String name)
    {
        for (TypeClassVarDecList it = this; it != null; it = it.tail) 
        {
            if (it.head != null && it.head.name.equals(name)) 
            {
                return it.head;
            }
        }
        return null;
    }

    /** Create inherited copy of all members */
    public TypeClassVarDecList inherit()
    {
        TypeClassVarDec newHead = null;
        if (this.head != null) 
        {
            newHead = new TypeClassVarDec(this.head.t, this.head.name, true);
        }
        TypeClassVarDecList newTail = null;
        if (this.tail != null) 
        {
            newTail = this.tail.inherit();
        }
        return new TypeClassVarDecList(newHead, newTail);
    }

    /** Insert new member at end */
    public TypeClassVarDecList insert(TypeClassVarDec d)
    {
        if (this.head == null) 
        {
            this.head = d;
            return this;
        }
        TypeClassVarDecList it = this;
        while (it.tail != null) 
        {
            it = it.tail;
        }
        it.tail = new TypeClassVarDecList(d, null);
        return this;
    }
}
