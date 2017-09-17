package cop5556sp17;
import cop5556sp17.AST.Dec;
import java.util.*;


public class SymbolTable
{
	class interTable
	{
			int s;
			Dec d;
			interTable() {	s = 0; d = null; }
			interTable(int s1, Dec d1) { s = s1; d = d1; }
	}

	HashMap<String, ArrayList<interTable>> map;
    int current_scope, next_scope;
    ArrayList<Integer> scope_stack;

	public void enterScope()
	{
		scope_stack.add(new Integer(next_scope));
		current_scope = next_scope;
		next_scope++;
	}

	public void leaveScope()
	{
		scope_stack.remove(scope_stack.size()-1);
		current_scope = scope_stack.get(scope_stack.size()-1);
	}

	public boolean insert(String string, Dec dec)
	{
		ArrayList<interTable> list = map.get(string);
		if(list!=null)
		{
			for(int i=0; i<list.size(); i++)
			{
				if(list.get(i).s==current_scope)	return false;
			}
		}
		if (list == null)
		{
			list = new ArrayList<interTable>();
			map.put(string, list);
		}
		list.add(new interTable(current_scope, dec));
		return true;
	}

	public Dec lookup(String ident)
	{
		ArrayList<interTable> hs = map.get(ident);
		if (hs == null)
			return null;
		for(int j=scope_stack.size()-1; j>=0; j--)
		{
			for(int k=0; k<hs.size(); k++)
			{
				if(hs.get(k).s==scope_stack.get(j))	return hs.get(k).d;
			}
		}
		return null;
	}
	public SymbolTable()
	{
		map = new HashMap<>();
		scope_stack = new ArrayList<Integer>();
		scope_stack.add(0);
		current_scope = 0;
		next_scope = 1;
	}

    @Override
	public String toString()
    {
    	return null;
    }

}

