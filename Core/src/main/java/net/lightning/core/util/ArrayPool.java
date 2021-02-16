package net.lightning.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class ArrayPool<T> extends ArrayList<T> {

    public ArrayPool(int i) {
        super(i);
    }

    public ArrayPool() {
    }

    public ArrayPool(@NotNull Collection<? extends T> collection) {
        super(collection);
    }

    public ArrayPool<T> append(T element) {
        add(element);
        return this;
    }

    public static class PredicatePool<T> extends ArrayPool<Predicate<T>> {

        public PredicatePool() {
            super();
        }

        public PredicatePool(List<Predicate<T>> fromList) {
            super(fromList);
        }

        public boolean testAll(T val) {
            for (Predicate<T> predicate : this) {
                if (!predicate.test(val)) {
                    return false;
                }
            }
            return true;
        }

        public boolean testAll(T val, boolean whenEmpty) {
            if (isEmpty()) {
                return whenEmpty;
            }

            for (Predicate<T> predicate : this) {
                if (!predicate.test(val)) {
                    return false;
                }
            }
            return true;
        }

        public boolean testOr(T val) {
            if (isEmpty()) {
                return true;
            }

            for (Predicate<T> predicate : this) {
                if (predicate.test(val)) {
                    return true;
                }
            }
            return false;
        }

    }

    public static class BiPredicatePool<T, U> extends ArrayPool<BiPredicate<T, U>> {

        public BiPredicatePool() {
            super();
        }

        public BiPredicatePool(List<BiPredicate<T, U>> fromList) {
            super(fromList);
        }

        public boolean testAll(T val1, U val2) {
            for (BiPredicate<T, U> biPredicate : this) {
                if (!biPredicate.test(val1, val2)) {
                    return false;
                }
            }
            return true;
        }

        public boolean testOr(T val1, U val2) {
            if (isEmpty()) {
                return true;
            }

            for (BiPredicate<T, U> biPredicate : this) {
                if (biPredicate.test(val1, val2)) {
                    return true;
                }
            }
            return false;
        }

    }

    public static class ConsumerPool<T> extends ArrayPool<Consumer<T>> {

        public ConsumerPool() {
            super();
        }

        public ConsumerPool(List<Consumer<T>> fromList) {
            super(fromList);
        }

        public void accept(T val) {
            for (Consumer<T> consumer : this) {
                consumer.accept(val);
            }
        }

    }

}
