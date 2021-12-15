package io.esastack.restlight.jaxrs.resolver.param;

import esa.commons.Checks;

import java.util.function.Supplier;

public class LazyDefaultValue implements Supplier<Object> {

    private final Supplier<Object> supplier;
    //The reason why the object is not used directly is that the object
    //may be null and cannot be used to judge whether it has been loaded
    private volatile Supplier<Object> loadedSupplier;

    public LazyDefaultValue(Supplier<Object> supplier) {
        this.supplier = Checks.checkNotNull(supplier);
    }

    @Override
    public Object get() {
        if (loadedSupplier != null) {
            return loadedSupplier.get();
        }
        synchronized (this) {
            if (loadedSupplier != null) {
                return loadedSupplier.get();
            }
            Object obj = supplier.get();
            loadedSupplier = () -> obj;
            return obj;
        }
    }
}
