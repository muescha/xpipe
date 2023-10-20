package io.xpipe.app.comp.store;

import io.xpipe.app.fxcomps.Comp;
import io.xpipe.app.fxcomps.util.BindingsHelper;
import io.xpipe.app.storage.DataStorage;
import io.xpipe.app.storage.DataStoreEntry;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Value;

import java.util.Comparator;
import java.util.function.Predicate;

@Value
public class StoreSection {

    public static Comp<?> customSection(StoreSection e, boolean topLevel) {
        var prov = e.getWrapper().getEntry().getProvider();
        if (prov != null) {
            return prov.customSectionComp(e, topLevel);
        } else {
            return new StoreSectionComp(e, topLevel);
        }
    }

    StoreEntryWrapper wrapper;
    ObservableList<StoreSection> allChildren;
    ObservableList<StoreSection> shownChildren;
    int depth;
    ObservableBooleanValue showDetails;

    public StoreSection(
            StoreEntryWrapper wrapper,
            ObservableList<StoreSection> allChildren,
            ObservableList<StoreSection> shownChildren,
            int depth) {
        this.wrapper = wrapper;
        this.allChildren = allChildren;
        this.shownChildren = shownChildren;
        this.depth = depth;
        if (wrapper != null) {
            this.showDetails = Bindings.createBooleanBinding(
                    () -> {
                        return wrapper.getExpanded().get() || allChildren.size() == 0;
                    },
                    wrapper.getExpanded(),
                    allChildren);
        } else {
            this.showDetails = new SimpleBooleanProperty(true);
        }
    }

    private static ObservableList<StoreSection> sorted(
            ObservableList<StoreSection> list, ObservableValue<StoreCategoryWrapper> category) {
        if (category == null) {
            return list;
        }

        var c = Comparator.<StoreSection>comparingInt(
                value -> value.getWrapper().getEntry().getValidity().isUsable() ? -1 : 1);
        var mapped = BindingsHelper.mappedBinding(category, storeCategoryWrapper -> storeCategoryWrapper.getSortMode());
        return BindingsHelper.orderedContentBinding(
                list,
                (o1, o2) -> {
                    var current = category.getValue();
                    if (current != null) {
                        return c.thenComparing(current.getSortMode().getValue().comparator())
                                .compare(o1, o2);
                    } else {
                        return c.compare(o1, o2);
                    }
                },
                category,
                mapped);
    }

    public static StoreSection createTopLevel(
            ObservableList<StoreEntryWrapper> all,
            Predicate<StoreEntryWrapper> entryFilter,
            ObservableStringValue filterString,
            ObservableValue<StoreCategoryWrapper> category) {
        var topLevel = BindingsHelper.filteredContentBinding(
                all,
                section -> {
                    return DataStorage.get().isRootEntry(section.getEntry());
                },
                category);
        var cached = BindingsHelper.cachedMappedContentBinding(
                topLevel, storeEntryWrapper -> create(storeEntryWrapper, 1, all, entryFilter, filterString, category));
        var ordered = sorted(cached, category);
        var shown = BindingsHelper.filteredContentBinding(
                ordered,
                section -> {
                    var showFilter = filterString == null || section.shouldShow(filterString.get());
                    var matchesSelector = section.anyMatches(entryFilter);
                    var sameCategory = category == null || category.getValue().contains(section.getWrapper().getEntry());
                    return showFilter && matchesSelector && sameCategory;
                },
                category,
                filterString);
        return new StoreSection(null, ordered, shown, 0);
    }

    private static StoreSection create(
            StoreEntryWrapper e,
            int depth,
            ObservableList<StoreEntryWrapper> all,
            Predicate<StoreEntryWrapper> entryFilter,
            ObservableStringValue filterString,
            ObservableValue<StoreCategoryWrapper> category) {
        if (e.getEntry().getValidity() == DataStoreEntry.Validity.LOAD_FAILED) {
            return new StoreSection(e, FXCollections.observableArrayList(), FXCollections.observableArrayList(), depth);
        }

        var allChildren = BindingsHelper.filteredContentBinding(all, other -> {
//            if (true) return DataStorage.get()
//                    .getDisplayParent(other.getEntry())
//                    .map(found -> found.equals(e.getEntry()))
//                    .orElse(false);
            // This check is fast as the children are cached in the storage
            return DataStorage.get().getStoreChildren(e.getEntry()).contains(other.getEntry());
        });
        var cached = BindingsHelper.cachedMappedContentBinding(
                allChildren, entry1 -> create(entry1, depth + 1, all, entryFilter, filterString, category));
        var ordered = sorted(cached, category);
        var filtered = BindingsHelper.filteredContentBinding(
                ordered,
                section -> {
                    return (filterString == null || section.shouldShow(filterString.get()))
                            && section.anyMatches(entryFilter);
                },
                category,
                filterString);
        return new StoreSection(e, cached, filtered, depth);
    }

    public boolean shouldShow(String filter) {
        return anyMatches(storeEntryWrapper -> storeEntryWrapper.shouldShow(filter));
    }

    public boolean anyMatches(Predicate<StoreEntryWrapper> c) {
        return c == null
                || c.test(wrapper)
                || allChildren.stream().anyMatch(storeEntrySection -> storeEntrySection.anyMatches(c));
    }
}