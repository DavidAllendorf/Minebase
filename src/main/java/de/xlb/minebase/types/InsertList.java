package de.xlb.minebase.types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InsertList implements Iterable<ChangeData>{
    private final List<ChangeData> data = new ArrayList<>();

    public void add(ChangeData changeData) {
        data.add(changeData);
    }

    public ChangeData get(int index) {
        return data.get(index);
    }

    public int size() {
        return data.size();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public List<ChangeData> getAll() {
        return data;
    }

    public ChangeData remove(int index) {
        return data.remove(index);
    }

    @Override
    public Iterator<ChangeData> iterator() {
        return data.iterator();
    }

    @Override
    public String toString() {
        return data.toString();
    }
}