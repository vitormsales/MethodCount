package br.ufmg.dcc.labsoft.java.jmove.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import br.ufmg.dcc.labsoft.java.jmove.ast.DeepDependencyVisitor;
import br.ufmg.dcc.labsoft.java.jmove.basic.AllEntitiesMapping;
import br.ufmg.dcc.labsoft.java.jmove.dependencies.AccessFieldDependency;
import br.ufmg.dcc.labsoft.java.jmove.dependencies.AccessMethodDependency;
import br.ufmg.dcc.labsoft.java.jmove.dependencies.AnnotateMethodDependency;
import br.ufmg.dcc.labsoft.java.jmove.dependencies.CreateMethodDependency;
import br.ufmg.dcc.labsoft.java.jmove.dependencies.DeclareLocalVariableDependency;
import br.ufmg.dcc.labsoft.java.jmove.dependencies.DeclareParameterDependency;
import br.ufmg.dcc.labsoft.java.jmove.dependencies.DeclareReturnDependency;
import br.ufmg.dcc.labsoft.java.jmove.dependencies.Dependency;
import br.ufmg.dcc.labsoft.java.jmove.dependencies.SimpleNameDependency;
import br.ufmg.dcc.labsoft.java.jmove.dependencies.ThrowDependency;
import br.ufmg.dcc.labsoft.java.jmove.utils.JMoveSignatures;
import br.ufmg.dcc.labsoft.java.jmove.utils.PrintOutput;

public class AllMethods {

	private List<MethodJMove> allMethodsList;
	private int numberOfExcluded;
	private Map<Integer, IMethod> iMethodMapping;
	private IProgressMonitor monitor;
	private String activeProjectName;

	public AllMethods(List<DeepDependencyVisitor> allDeepDependency,
			IProgressMonitor monitor, String activeProjectName)
			throws JavaModelException {
		// TODO Auto-generated method stub

		this.numberOfExcluded = 0;
		this.allMethodsList = new ArrayList<MethodJMove>();
		this.iMethodMapping = new HashMap<Integer, IMethod>();
		this.monitor = monitor;
		this.activeProjectName = activeProjectName;

		createMethodsDependeciesList(allDeepDependency);

	}

	private void createMethodsDependeciesList(
			List<DeepDependencyVisitor> allDeepDependency)
			throws JavaModelException {
		float i = 0;

		monitor.beginTask(
				"Creating Dependencies for selected Java Project (2/4)",
				(allDeepDependency.size()));

		for (DeepDependencyVisitor deep : allDeepDependency) {
			System.out.printf("Criando metodo  %2.2" + "f %c \n",
					(100) * (++i / allDeepDependency.size()), '%');
			// for (IMethod m : deep.getUnit().findPrimaryType().getMethods()) {
			// System.out.println("element " + m.getElementName() + " "
			// + m.getDeclaringType().getFullyQualifiedName());
			// }
			// System.out.println();

			monitor.worked(1);
			for (Dependency dep : deep.getDependencies()) {

				if (dep instanceof AccessMethodDependency) {

					AccessMethodDependency acDependency = (AccessMethodDependency) dep;

					// String method = dep.getClassNameA() + "."
					// + ((AccessMethodDependency) dep).getMethodNameA()
					// + "()";
					String sourceClass = dep.getClassNameA();
					String dependecyClass = dep.getClassNameB();

					String methodA = JMoveSignatures.getMethodSignature(
							dep.getClassNameA(), acDependency.getImethodA());
					String methodB = JMoveSignatures.getMethodSignature(
							dep.getClassNameB(), acDependency.getImethodB());

					// String methodB = dep.getClassNameB() + "."
					// + ((AccessMethodDependency) dep).getMethodNameB()
					// + "()";

					List<String> dependeciesList = new ArrayList<String>();

					if (!recursive(sourceClass, methodA, dependecyClass,
							methodB)) {
						dependeciesList.add(dependecyClass);
						dependeciesList.add(methodB);
					}
					// System.out.println(sourceClass + " " + methodA
					// + " classe que depednde e metodo " + dependecyClass
					// + " " + methodB);

					putIMethodInMapping(methodA, acDependency.getImethodA());
					putIMethodInMapping(methodB, acDependency.getImethodB());

					createMethod(methodA, sourceClass, dependeciesList,
							acDependency.getImethodA());

				} else

				if (dep instanceof AccessFieldDependency) {

					AccessFieldDependency acFieldDependency = (AccessFieldDependency) dep;

					String methodA = JMoveSignatures.getMethodSignature(
							dep.getClassNameA(),
							acFieldDependency.getImethodA());

					String field = JMoveSignatures.getFieldSignature(
							dep.getClassNameB(),
							acFieldDependency.getiVariableBinding());

					// String method = dep.getClassNameA() + "."
					// + ((AccessFieldDependency) dep).getMethodName()
					// + "()";

					String sourceClass = dep.getClassNameA();
					String dependecyClass = dep.getClassNameB();
					// String field = dep.getClassNameB() + "."
					// + ((AccessFieldDependency) dep).getFieldName();

					List<String> dependeciesList = new ArrayList<String>();
					dependeciesList.add(dependecyClass);
					dependeciesList.add(field);

					putIMethodInMapping(methodA,
							acFieldDependency.getImethodA());

					createMethod(methodA, sourceClass, dependeciesList,
							acFieldDependency.getImethodA());

				} else

				if (dep instanceof SimpleNameDependency) {

					SimpleNameDependency snDependency = (SimpleNameDependency) dep;

					// String method = dep.getClassNameA() + "."
					// + ((SimpleNameDependency) dep).getMethodName()
					// + "()";

					String sourceClass = dep.getClassNameA();
					String dependecyClass = dep.getClassNameB();

					// String field = dep.getClassNameB() + "."
					// + ((SimpleNameDependency) dep).getVariableName();

					String methodA = JMoveSignatures.getMethodSignature(
							dep.getClassNameA(), snDependency.getImethodA());

					String field = JMoveSignatures.getFieldSignature(
							dep.getClassNameB(),
							snDependency.getiVariableBinding());

					List<String> dependeciesList = new ArrayList<String>();
					dependeciesList.add(dependecyClass);
					dependeciesList.add(field);

					putIMethodInMapping(methodA, snDependency.getImethodA());

					createMethod(methodA, sourceClass, dependeciesList,
							snDependency.getImethodA());

				} else

				if (dep instanceof AnnotateMethodDependency) {

					AnnotateMethodDependency anDependency = (AnnotateMethodDependency) dep;

					String sourceClass = dep.getClassNameA();
					String dependecyClass = dep.getClassNameB();
					// String method = dep.getClassNameA() + "."
					// + ((AnnotateMethodDependency) dep).getMethodName()
					// + "()";

					String methodA = JMoveSignatures.getMethodSignature(
							dep.getClassNameA(), anDependency.getImethodA());

					List<String> dependeciesList = new ArrayList<String>();
					dependeciesList.add(dependecyClass);

					putIMethodInMapping(methodA, anDependency.getImethodA());

					createMethod(methodA, sourceClass, dependeciesList,
							anDependency.getImethodA());

				} else

				if (dep instanceof CreateMethodDependency) {

					CreateMethodDependency cmDependency = (CreateMethodDependency) dep;

					String sourceClass = dep.getClassNameA();
					String dependecyClass = dep.getClassNameB();

					String methodA = JMoveSignatures.getMethodSignature(
							dep.getClassNameA(), cmDependency.getImethodA());

					// String method = dep.getClassNameA() + "."
					// + ((CreateMethodDependency) dep).getMethodNameA()
					// + "()";

					List<String> dependeciesList = new ArrayList<String>();
					dependeciesList.add(dependecyClass);

					putIMethodInMapping(methodA, cmDependency.getImethodA());

					createMethod(methodA, sourceClass, dependeciesList,
							cmDependency.getImethodA());

				} else

				if (dep instanceof DeclareParameterDependency) {

					DeclareParameterDependency dpDependency = (DeclareParameterDependency) dep;

					// String method = dep.getClassNameA()
					// + "."
					// + ((DeclareParameterDependency) dep)
					// .getMethodName() + "()";

					String methodA = JMoveSignatures.getMethodSignature(
							dep.getClassNameA(), dpDependency.getImethodA());

					String sourceClass = dep.getClassNameA();
					String dependecyClass = dep.getClassNameB();
					// String field = dep.getClassNameB() + "."
					// + ((DeclareParameterDependency) dep).getFieldName();

					List<String> dependeciesList = new ArrayList<String>();
					dependeciesList.add(dependecyClass);
					// dependeciesList.add(field);

					putIMethodInMapping(methodA, dpDependency.getImethodA());

					createMethod(methodA, sourceClass, dependeciesList,
							dpDependency.getImethodA());

				} else

				if (dep instanceof DeclareReturnDependency) {

					DeclareReturnDependency drDependency = (DeclareReturnDependency) dep;

					String methodA = JMoveSignatures.getMethodSignature(
							dep.getClassNameA(), drDependency.getImethodA());

					String sourceClass = dep.getClassNameA();
					String dependecyClass = dep.getClassNameB();
					// String method = dep.getClassNameA() + "."
					// + ((DeclareReturnDependency) dep).getMethodName()
					// + "()";

					List<String> dependeciesList = new ArrayList<String>();
					dependeciesList.add(dependecyClass);

					putIMethodInMapping(methodA, drDependency.getImethodA());

					createMethod(methodA, sourceClass, dependeciesList,
							drDependency.getImethodA());

				} else

				if (dep instanceof DeclareLocalVariableDependency) {

					DeclareLocalVariableDependency dlvDependency = (DeclareLocalVariableDependency) dep;

					String methodA = JMoveSignatures.getMethodSignature(
							dep.getClassNameA(), dlvDependency.getImethodA());

					// String method = dep.getClassNameA()
					// + "."
					// + ((DeclareLocalVariableDependency) dep)
					// .getMethodName() + "()";
					String sourceClass = dep.getClassNameA();
					String dependecyClass = dep.getClassNameB();
					// String field = dep.getClassNameB()
					// + "."
					// + ((DeclareLocalVariableDependency) dep)
					// .getFieldName();

					List<String> dependeciesList = new ArrayList<String>();
					dependeciesList.add(dependecyClass);

					// System.out.println("DeclareLocalVariableDependency");
					// System.out.println(dependecyClass);
					// System.out.println(method);
					// System.out.println(field);
					// dependeciesList.add(field);

					putIMethodInMapping(methodA, dlvDependency.getImethodA());

					createMethod(methodA, sourceClass, dependeciesList,
							dlvDependency.getImethodA());

				} else

				if (dep instanceof ThrowDependency) {

					ThrowDependency throwDependency = (ThrowDependency) dep;

					String methodA = JMoveSignatures.getMethodSignature(
							dep.getClassNameA(), throwDependency.getImethodA());

					String sourceClass = dep.getClassNameA();
					String dependecyClass = dep.getClassNameB();
					// String method = dep.getClassNameA() + "."
					// + ((ThrowDependency) dep).getMethodName() + "()";

					List<String> dependeciesList = new ArrayList<String>();
					dependeciesList.add(dependecyClass);

					putIMethodInMapping(methodA, throwDependency.getImethodA());

					createMethod(methodA, sourceClass, dependeciesList,
							throwDependency.getImethodA());

				}

			}

			if (monitor != null && monitor.isCanceled()) {
				if (monitor != null)
					monitor.done();
				throw new OperationCanceledException();
			}
		}

		// // #............ Imprime dependencias

		Iterator<MethodJMove> it = allMethodsList.iterator();
		while (it.hasNext()) {
			MethodJMove m = it.next();

			PrintOutput.write(
					AllEntitiesMapping.getInstance().getByID(m.getNameID())
							+ "\n", "conjuntos");

			for (Integer chaves : m.getMethodsDependencies()) {
				PrintOutput
						.write(AllEntitiesMapping.getInstance().getByID(chaves)
								+ "\n", "conjuntos");
			}

			PrintOutput.write("\n \n", "conjuntos");
		}
		// #............

		// Crias CSV com o nome do método, quantidade de paramentros
		it = allMethodsList.iterator();

		// cabeçalho

		String escape = "";
		String endEscape = ";";

		PrintOutput.write(escape + " QUALIFIED_NAME" + endEscape,
				activeProjectName + ".csv");

		PrintOutput.write(escape + " QTD_DEPENDECIES" + endEscape,
				activeProjectName + ".csv");

		PrintOutput.write(escape + " NANE" + endEscape, activeProjectName
				+ ".csv");

		PrintOutput.write(escape + " QTD_PARAMETERS" + endEscape,
				activeProjectName + ".csv");

		PrintOutput.write(escape + " NUMBER_OF_OCURRENCES" + endEscape,
				activeProjectName + ".csv");

		PrintOutput.write(escape + " IS_CONSTRUCTOR" + endEscape,
				activeProjectName + ".csv");

		PrintOutput.write(escape + " IS_MAIN" + endEscape, activeProjectName
				+ ".csv");

		PrintOutput.write(escape + " SYSTEM", activeProjectName + ".csv");

		PrintOutput.write("\n", activeProjectName + ".csv");

		while (it.hasNext()) {
			MethodJMove m = it.next();
			IMethod iMethod = this.getIMethod(m);
			if (iMethod != null) {
				PrintOutput.write(endEscape, activeProjectName + ".csv");

				PrintOutput.write(
						escape
								+ AllEntitiesMapping.getInstance().getByID(
										m.getNameID()) + endEscape,
						activeProjectName + ".csv");

				PrintOutput.write(escape + m.getMethodsDependencies().size()
						+ endEscape, activeProjectName + ".csv");

				PrintOutput.write(
						escape + iMethod.getElementName() + endEscape,
						activeProjectName + ".csv");

				PrintOutput.write(escape + iMethod.getNumberOfParameters()
						+ endEscape, activeProjectName + ".csv");

				PrintOutput.write(escape + iMethod.getOccurrenceCount()
						+ endEscape, activeProjectName + ".csv");

				PrintOutput.write(escape + (iMethod.isConstructor() ? 1 : 0)
						+ endEscape, activeProjectName + ".csv");

				PrintOutput.write(escape + (iMethod.isMainMethod() ? 1 : 0)
						+ endEscape, activeProjectName + ".csv");

				PrintOutput.write(escape + activeProjectName.substring(0, 5),
						activeProjectName + ".csv");

				PrintOutput.write("\n", activeProjectName + ".csv");
			}
		}

	}

	private boolean recursive(String sourceClass, String methodA,
			String dependecyClass, String methodB) {
		// TODO Auto-generated method stub

		if (sourceClass.equals(dependecyClass) && methodA.equals(methodB)) {
			return true;
		}

		return false;
	}

	private void createMethod(String methodName, String sourceClass,
			List<String> dependeciesList, IMethod iMethod) {
		// TODO Auto-generated method stub

		int methodId = AllEntitiesMapping.getInstance().createEntityID(
				methodName);

		int sourceClassID = AllEntitiesMapping.getInstance().createEntityID(
				sourceClass);

		// System.out.println("Aki primeiro "+ sourceClass);
		MethodJMove method2 = new MethodJMove(methodId, sourceClassID);

		if (allMethodsList.contains(method2)) {

			method2 = allMethodsList.get(allMethodsList.indexOf(method2));
			method2.setNewMethodsDependencies(dependeciesList);

		} else {

			// List<String> moveReachable = MoveMethod
			// .getpossibleRefactoring(getIMethod(method2));

			allMethodsList.add(method2);
			method2.setNewMethodsDependencies(dependeciesList);

		}

	}

	private void putIMethodInMapping(String methodName, IMethod iMethod) {
		// TODO Auto-generated method stub
		int methodId;
		methodId = AllEntitiesMapping.getInstance().createEntityID(methodName);

		iMethodMapping.put(methodId, iMethod);

	}

	public IMethod getIMethod(MethodJMove method) {
		// TODO Auto-generated method stub
		return iMethodMapping.get(method.getNameID());
	}

	public List<MethodJMove> getAllMethodsList() {
		return allMethodsList;
	}

	public MethodJMove getMethodByID(int methodID) {
		for (MethodJMove method : allMethodsList) {
			if (methodID == method.getNameID())
				return method;
		}
		return null;

	}

	public int getNumberOfExcluded() {
		return numberOfExcluded;
	}

	private void setNumberOfExcluded(int numberOfExcluded) {
		this.numberOfExcluded = numberOfExcluded;
	}

	public void excludeDependeciesLessThan(int Numdepedencies) {

		List<MethodJMove> smallMethod = new ArrayList<MethodJMove>();

		if (Numdepedencies > getNumberOfExcluded()) {

			setNumberOfExcluded(Numdepedencies);

			for (MethodJMove method : allMethodsList) {
				if (method.getMethodsDependencies().size() < Numdepedencies) {
					smallMethod.add(method);
				}
			}

			allMethodsList.removeAll(smallMethod);
		}
	}

	public void excludeConstructors() {

		Iterator<Entry<Integer, IMethod>> it = iMethodMapping.entrySet()
				.iterator();

		while (it.hasNext()) {
			Entry<Integer, IMethod> entry = it.next();
			IMethod method = entry.getValue();
			try {
				if (method != null && method.isConstructor()) {
					MethodJMove methodTemp = getMethodByID(entry.getKey());
					int index = allMethodsList.indexOf(methodTemp);
					allMethodsList.remove(index);
				}
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
