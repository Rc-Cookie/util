package de.rccookie.util;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public interface ImmutableTable<L,V> extends Table<L,V> {

    @Override
    default V setValue(@Range(from = 0) int row, @Range(from = 0) int column, @Nullable V value) {
        throw new ImmutabilityException();
    }

    @Override
    default void addRow(@Range(from = 0) int index, Collection<? extends V> values) {
        throw new ImmutabilityException();
    }

    @Override
    default void addColumn(@Range(from = 0) int index, Collection<? extends V> values) {
        throw new ImmutabilityException();
    }

    @Override
    default void addRow(@Range(from = 0) int index, Map<?, ? extends V> labeledValues) {
        throw new ImmutabilityException();
    }

    @Override
    default void addColumn(@Range(from = 0) int index, Map<?, ? extends V> labeledValues) {
        throw new ImmutabilityException();
    }

    @Override
    default void addRow(@Range(from = 0) int index, L label, Collection<? extends V> values) {
        throw new ImmutabilityException();
    }

    @Override
    default void addColumn(@Range(from = 0) int index, L label, Collection<? extends V> values) {
        throw new ImmutabilityException();
    }

    @Override
    default void addRow(@Range(from = 0) int index, L label, Map<?, ? extends V> labeledValues) {
        throw new ImmutabilityException();
    }

    @Override
    default void addColumn(@Range(from = 0) int index, L label, Map<?, ? extends V> labeledValues) {
        throw new ImmutabilityException();
    }

    @Override
    default void addRowsOrdered(@Range(from = 0) int index, Collection<? extends Collection<? extends V>> rows) {
        throw new ImmutabilityException();
    }

    @Override
    default void addColumnsOrdered(@Range(from = 0) int index, Collection<? extends Collection<? extends V>> columns) {
        throw new ImmutabilityException();
    }

    @Override
    default void addRowsOrdered(@Range(from = 0) int index, Table<?, ? extends V> table) {
        throw new ImmutabilityException();
    }

    @Override
    default void addColumnsOrdered(@Range(from = 0) int index, Table<?, ? extends V> table) {
        throw new ImmutabilityException();
    }

    @Override
    default void addRowsLabeled(@Range(from = 0) int index, Collection<? extends Map<?, ? extends V>> rows) {
        throw new ImmutabilityException();
    }

    @Override
    default void addColumnsLabeled(@Range(from = 0) int index, Collection<? extends Map<?, ? extends V>> columns) {
        throw new ImmutabilityException();
    }

    @Override
    default void addRowsLabeled(@Range(from = 0) int index, Table<?, ? extends V> table) {
        throw new ImmutabilityException();
    }

    @Override
    default void addColumnsLabeled(@Range(from = 0) int index, Table<?, ? extends V> table) {
        throw new ImmutabilityException();
    }

    @Override
    default void setRow(@Range(from = 0) int index, Collection<? extends V> values) {
        throw new ImmutabilityException();
    }

    @Override
    default void setColumn(@Range(from = 0) int index, Collection<? extends V> values) {
        throw new ImmutabilityException();
    }

    @Override
    default void setRow(@Range(from = 0) int index, Map<?, ? extends V> labeledValues) {
        throw new ImmutabilityException();
    }

    @Override
    default void setColumn(@Range(from = 0) int index, Map<?, ? extends V> labeledValues) {
        throw new ImmutabilityException();
    }

    @Override
    default void removeRow(@Range(from = 0) int index) {
        throw new ImmutabilityException();
    }

    @Override
    default void removeColumn(@Range(from = 0) int index) {
        throw new ImmutabilityException();
    }

    @Override
    default void setColumnLabels(Collection<? extends L> labels) {
        throw new ImmutabilityException();
    }

    @Override
    default void setRowLabels(Collection<? extends L> labels) {
        throw new ImmutabilityException();
    }

    @Override
    default void clear() {
        throw new ImmutabilityException();
    }


    interface ImmutableVector<L,V> extends Vector<L,V> {

        @Override
        default L setLabel(L label) {
            throw new ImmutabilityException();
        }

        @Override
        default V set(int index, @Nullable V value) {
            throw new ImmutabilityException();
        }

        @Override
        default V set(Object label, @Nullable V value) {
            throw new ImmutabilityException();
        }

        @Override
        default void clear() {
            throw new ImmutabilityException();
        }
    }
}
