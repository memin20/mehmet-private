package org.eclipse.xtext.ui.generator;

import java.util.List;
import java.util.Set;

import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.generator.AbstractGeneratorFragment;
import org.eclipse.xtext.generator.BindFactory;
import org.eclipse.xtext.generator.Binding;
import org.eclipse.xtext.generator.xbase.XbaseGeneratorFragment;
import org.eclipse.xtext.resource.containers.IAllContainersState;

import com.google.common.collect.Lists;

public class ImplicitUiFragment extends AbstractGeneratorFragment {

	private final List<String> fileExtensions;

	public ImplicitUiFragment(List<String> fileExtensions2) {
		this.fileExtensions = fileExtensions2;
	}

	@Override
	protected List<Object> getParameters(Grammar g) {
		return Lists.newArrayList(fileExtensions, XbaseGeneratorFragment.doesUseXbase(g));
	}

	@Override
	public Set<Binding> getGuiceBindingsUi(Grammar grammar) {
		BindFactory bindFactory = new BindFactory()
			.addTypeToProviderInstance(
					IAllContainersState.class.getName(),
					"org.eclipse.xtext.ui.shared.Access.getJavaProjectsState()");
		if (XbaseGeneratorFragment.doesUseXbase(grammar)) {
			bindFactory = bindFactory.addTypeToType("org.eclipse.xtext.ui.editor.XtextEditor", "org.eclipse.xtext.xbase.ui.editor.XbaseEditor");
			bindFactory = bindFactory.addTypeToType("org.eclipse.xtext.ui.editor.model.XtextDocumentProvider", "org.eclipse.xtext.xbase.ui.editor.XbaseDocumentProvider");
		}
		return bindFactory.getBindings();
	}

	@Override
	public String[] getRequiredBundlesUi(Grammar grammar) {
		if (XbaseGeneratorFragment.doesUseXbase(grammar)) {
			return new String[] {
					"org.eclipse.xtext.ui",
					"org.eclipse.xtext.ui.shared",
					"org.eclipse.xtext.xbase.ui",
					"org.eclipse.ui.editors",
					"org.eclipse.ui" };
		}
		return new String[] {
				"org.eclipse.xtext.ui",
				"org.eclipse.xtext.ui.shared",
				"org.eclipse.ui.editors",
				"org.eclipse.ui" };
	}

}
