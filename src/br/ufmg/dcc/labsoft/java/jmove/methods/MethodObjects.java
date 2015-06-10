package br.ufmg.dcc.labsoft.java.jmove.methods;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodObjects {

	private AbstractMap<IMethod, MethodDeclaration> allDeclaration = null;
	private AbstractMap<IMethod, Set<IVariableBinding>> allLocalDeclaration = null;
	private static MethodObjects instance = null;

	private MethodObjects() {
		this.allDeclaration = new HashMap<IMethod, MethodDeclaration>();
		this.allLocalDeclaration = new HashMap<IMethod, Set<IVariableBinding>>();
	}

	public static MethodObjects getInstance() {
		if (instance == null) {
			instance = new MethodObjects();
		}
		return instance;
	}

	public void insertMapping(IMethod iMethod, MethodDeclaration mdeclaration) {
		if (!allDeclaration.containsKey(iMethod)) {
			allDeclaration.put(iMethod, mdeclaration);

		}
	}

	public MethodDeclaration getMethodDeclaration(IMethod iMethod) {
		return allDeclaration.get(iMethod);
	}

	public void insertLocalDeclaration(IMethod iMethod,
			Set<IVariableBinding> localDeclarationList) {
		allLocalDeclaration.put(iMethod, localDeclarationList);

	}

	public boolean hasTheyLocalDeclaration(IMethod iMethod) {
		return allLocalDeclaration.containsKey(iMethod);
	}

	public Set<IVariableBinding> getAllLocalDeclarationSet(IMethod iMethod) {
		return allLocalDeclaration.get(iMethod);
	}

	public AbstractMap<IMethod, MethodDeclaration> getAllMethodDeclarations() {
		return allDeclaration;
	}

}
