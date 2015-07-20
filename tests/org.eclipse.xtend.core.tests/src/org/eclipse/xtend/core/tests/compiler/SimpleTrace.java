package org.eclipse.xtend.core.tests.compiler;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.trace.AbstractTraceRegion;
import org.eclipse.xtext.generator.trace.internal.AbstractTrace;
import org.eclipse.xtext.workspace.IProjectConfig;

public class SimpleTrace extends AbstractTrace {

	private final AbstractTraceRegion root;

	public SimpleTrace(AbstractTraceRegion root) {
		this.root = root;
	}

	@Override
	protected AbstractTraceRegion doGetRootTraceRegion() {
		return root;
	}

	@Override
	/* @NonNull */
	public IProjectConfig getLocalProjectConfig() {
		throw new UnsupportedOperationException();
	}

	@Override
	public URI getLocalURI() {
		return null;
	}


	@Override
	protected InputStream getContents(URI uri, IProjectConfig project) throws IOException {
		throw new UnsupportedOperationException();
	}

}