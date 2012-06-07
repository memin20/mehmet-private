/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.typesystem.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xtype.XComputedTypeReference;

/**
 * @author Sebastian Zarnekow - Initial contribution and API 
 * TODO JavaDoc, toString
 */
public class UnboundTypeParameterAwareTypeArgumentCollector extends ActualTypeArgumentCollector {

	public UnboundTypeParameterAwareTypeArgumentCollector(List<JvmTypeParameter> parametersToBeMapped,
			CommonTypeComputationServices services) {
		super(parametersToBeMapped, services);
	}

	@Override
	protected TypeParameterSubstitutor createTypeParameterSubstitutor(Map<JvmTypeParameter, MergedBoundTypeArgument> mapping) {
		return new UnboundTypeParameterPreservingSubstitutor(mapping, getServices());
	}

	@Override
	public Void doVisitComputedTypeReference(XComputedTypeReference reference, JvmTypeReference param) {
		if (UnboundTypeParameters.isUnboundTypeParameter(reference)) {
			UnboundTypeParameter typeParameter = (UnboundTypeParameter) reference.getTypeProvider();
			typeParameter.acceptHint(boundByInferrence(param));
			return null;
		}
		return super.doVisitComputedTypeReference(reference, param);
	}

	@Override
	protected ParameterizedTypeReferenceTraverser createParameterizedTypeReferenceTraverser() {
		return new ParameterizedTypeReferenceTraverser() {
			@Override
			public Void doVisitComputedTypeReference(XComputedTypeReference reference,
					JvmParameterizedTypeReference declaration) {
				if (UnboundTypeParameters.isUnboundTypeParameter(reference)) {
					UnboundTypeParameter typeParameter = (UnboundTypeParameter) reference.getTypeProvider();
					JvmType type = declaration.getType();
					if (type instanceof JvmTypeParameter) {
						JvmTypeParameter unboundTypeParameter = (JvmTypeParameter) type;
						if (unboundTypeParameter != typeParameter.getTypeParameter() && shouldProcess(unboundTypeParameter)) {
							processTypeParameter(unboundTypeParameter, reference);
						} else {
							// register synonym type param resolution et al for the actual type in the given UnboundTypeParameter
						}
					} else {
						typeParameter.acceptHint(boundByInferrence(declaration));
					}
					return null;
				}
				return super.doVisitComputedTypeReference(reference, declaration);
			}
		};
	}

	//	@Override
	//	protected ArrayTypeReferenceTraverser createArrayTypeReferenceTraverser() {
	//		return new ArrayTypeReferenceTraverser() {
	//			@Override
	//			public Void doVisitComputedTypeReference(XComputedTypeReference reference,
	//					JvmGenericArrayTypeReference param) {
	//				// TODO Auto-generated method stub
	//				return super.doVisitComputedTypeReference(reference, param);
	//			}
	//		};
	//	}

	@Override
	protected WildcardTypeReferenceTraverser createWildcardTypeReferenceTraverser() {
		return new UnboundTypeParameterAwareWildcardTypeReferenceTraverser(this);
	}

	@Override
	protected JvmTypeParameter findMappedParameter(JvmTypeParameter parameter,
			Map<JvmTypeParameter, MergedBoundTypeArgument> mapping, Collection<JvmTypeParameter> visited) {
		return UnboundTypeParameters.findMappedParameter(parameter, mapping, visited);
	}
}