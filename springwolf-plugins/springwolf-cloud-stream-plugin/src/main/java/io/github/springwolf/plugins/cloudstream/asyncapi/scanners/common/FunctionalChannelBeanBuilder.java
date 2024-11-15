// SPDX-License-Identifier: Apache-2.0
package io.github.springwolf.plugins.cloudstream.asyncapi.scanners.common;

import io.github.springwolf.core.asyncapi.scanners.common.payload.internal.TypeExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class FunctionalChannelBeanBuilder {
    private final TypeExtractor typeExtractor;

    public Set<FunctionalChannelBeanData> build(SimpleFunctionRegistry.FunctionInvocationWrapper wrapper) {
        if (wrapper.isConsumer() || wrapper.isWrappedBiConsumer()) {
            Type payloadType = getTypeGenerics(wrapper.getInputType()).get(0);
            return Set.of(ofConsumer(wrapper, payloadType));
        } else if (wrapper.isSupplier()) {
            Type payloadType = getTypeGenerics(wrapper.getOutputType()).get(0);
            return Set.of(ofSupplier(wrapper, payloadType));
        } else if (wrapper.isFunction()) {
            Type inputType = getTypeGenerics(wrapper.getInputType()).get(0);
            Type outputType = getTypeGenerics(wrapper.getOutputType()).get(0);
            return Set.of(ofConsumer(wrapper, inputType), ofSupplier(wrapper, outputType));
        }

        return Collections.emptySet();
    }

    private static FunctionalChannelBeanData ofConsumer(
            SimpleFunctionRegistry.FunctionInvocationWrapper wrapper, Type payloadType) {
        String name = wrapper.getFunctionDefinition();
        String cloudStreamBinding = firstCharToLowerCase(name) + "-in-0";
        return new FunctionalChannelBeanData(
                name,
                wrapper.getTarget().getClass(),
                payloadType,
                FunctionalChannelBeanData.BeanType.CONSUMER,
                cloudStreamBinding);
    }

    private static FunctionalChannelBeanData ofSupplier(
            SimpleFunctionRegistry.FunctionInvocationWrapper wrapper, Type payloadType) {
        String name = wrapper.getFunctionDefinition();
        String cloudStreamBinding = firstCharToLowerCase(name) + "-out-0";
        return new FunctionalChannelBeanData(
                name,
                wrapper.getTarget().getClass(),
                payloadType,
                FunctionalChannelBeanData.BeanType.SUPPLIER,
                cloudStreamBinding);
    }

    private static String firstCharToLowerCase(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    private List<Type> getTypeGenerics(Type type) {
        if (type instanceof ParameterizedType) {
            return List.of(typeExtractor.extractActualType(type));
        }
        return List.of(type);
    }
}
