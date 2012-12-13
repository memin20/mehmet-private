/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.imports;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.eclipse.xtext.util.Strings.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend.core.jvmmodel.IXtendJvmAssociations;
import org.eclipse.xtend.core.scoping.XtendImportedNamespaceScopeProvider;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.formatting.IWhitespaceInformationProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.ReplaceRegion;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
public class ImportOrganizer {

	@Inject
	private IWhitespaceInformationProvider whitespaceInformationProvider;
	
	@Inject
	private IXtendJvmAssociations associations;
	
	@Inject
	private Provider<TypeUsageCollector> typeUsageCollectorProvider;
	
	@Inject 
	private ConflictResolver conflictResolver;
	
	@Inject
	private ImportSectionSerializer importSectionSerializer;
	
	@Inject
	private NonOverridableTypesProvider nonOverridableTypesProvider;
	
	@Inject(optional=true)
	private IUnresolvedTypeResolver unresolvedTypeResolver;
	
	public List<ReplaceRegion> getOrganizedImportChanges(XtextResource state) {
		XtendFile xtendFile = getXtendFile(state);
		Set<JvmDeclaredType> locallyDeclaredTypes = getDeclaredTypes(xtendFile);
		TypeUsageCollector typeUsageCollector = typeUsageCollectorProvider.get();
		TypeUsages typeUsages = typeUsageCollector.collectTypeUsages(xtendFile);
		if(unresolvedTypeResolver != null) 
			unresolvedTypeResolver.resolve(typeUsages, state);
		Map<String, JvmDeclaredType> name2type = conflictResolver.resolveConflicts(typeUsages, nonOverridableTypesProvider, xtendFile);
		Set<String> implicitlyImportedPackages = getImplicitlyImportedPackages(xtendFile);
		SortedImportSection newImportSection = new SortedImportSection();
		List<ReplaceRegion> replaceRegions = newArrayList();
		for(Map.Entry<String, JvmDeclaredType> entry: name2type.entrySet()) {
			String text = entry.getKey();
			JvmDeclaredType type = entry.getValue();
			Iterable<TypeUsage> usages = typeUsages.getUsages(type);
			if(needsImport(type, text, xtendFile, locallyDeclaredTypes, implicitlyImportedPackages, nonOverridableTypesProvider, usages)) {
				newImportSection.getImportedTypes().add(type);
			}
			for(TypeUsage usage: usages) {
				if(!equal(usage.getText(), text)) {
					replaceRegions.add(new ReplaceRegion(usage.getTextRegion(), text));
				}
			}
		}
		newImportSection.getStaticImports().addAll(typeUsages.getStaticImports());
		newImportSection.getExtensionImports().addAll(typeUsages.getExtensionImports());
		replaceRegions.add(importSectionSerializer.serialize(xtendFile, newImportSection));
		return replaceRegions; 
	}

	protected boolean needsImport(JvmDeclaredType type, String name, XtendFile xtendFile,
			Set<JvmDeclaredType> locallyDefinedTypes, Set<String> implicitlyImportedPackages, 
			NonOverridableTypesProvider nonOverridableTypesProvider, Iterable<TypeUsage> usages)  {
		return !((type.getIdentifier().equals(name))
			|| implicitlyImportedPackages.contains(type.getPackageName())
			|| locallyDefinedTypes.contains(type)
			|| isUsedInNonOverridableContextOnly(usages, nonOverridableTypesProvider, name));
	}
	
	protected boolean isUsedInNonOverridableContextOnly(Iterable<TypeUsage> usages, 
			NonOverridableTypesProvider nonOverridableTypesProvider, String name) {
		for(TypeUsage usage: usages) {
			if(nonOverridableTypesProvider.getVisibleType(usage.getContext(), name) == null) 
				return false;
		}
		return true;
	}
	
	protected Set<String> getImplicitlyImportedPackages(XtendFile xtendFile) {
		Set<String> implicitlyImportedPackages = newHashSetWithExpectedSize(3);
		if(xtendFile.getPackage() != null) 
			implicitlyImportedPackages.add(xtendFile.getPackage());
		implicitlyImportedPackages.add(XtendImportedNamespaceScopeProvider.JAVA_LANG.toString("."));
		implicitlyImportedPackages.add(XtendImportedNamespaceScopeProvider.XTEND_LIB.toString("."));
		return implicitlyImportedPackages;
	}
	
	protected XtendFile getXtendFile(XtextResource resource) {
		if (resource.getContents().isEmpty())
			return null;
		final EObject eObject = resource.getContents().get(0);
		if (!(eObject instanceof XtendFile))
			return null;
		final XtendFile xtendFile = (XtendFile) eObject;
		return xtendFile;
	}

	protected Set<JvmDeclaredType> getDeclaredTypes(XtendFile xtendFile) {
		Set<JvmDeclaredType> locallyDefinedTypes = newHashSet();
		for(XtendTypeDeclaration xtendType: xtendFile.getXtendTypes())  {
			for(EObject inferredElement: associations.getJvmElements(xtendType)) {
				if(inferredElement instanceof JvmDeclaredType)  
					locallyDefinedTypes.add((JvmDeclaredType) inferredElement);
			}
		}
		return locallyDefinedTypes;
	}
}
