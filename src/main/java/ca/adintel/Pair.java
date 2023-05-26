package ca.adintel;

public class Pair<T extends Comparable> implements Comparable<Pair<T>>{
    public T first = null;
    public T second = null;

    public Pair(T t1,T t2){
        first = t1;
        second = t2;
    }

    public Pair(){
        first = null;
        second = null;
    }

    public void add(T t){
        if (first==null) first = t;
        else if (first==t) return;

        second = t;
    }

    public String toString(){
        return first+":"+second;
    }

    @Override
    public int compareTo(final Pair o) {
        if (o==null) return -1;
        if (first==o.first&&second==o.second) return 0;
        int compare1 = first.compareTo(o.first);
        if (compare1!=0) return compare1;
        return second.compareTo(o.second);
    }

    @Override
    public boolean equals(final Object obj) {
        try {
            Pair<T> test = (Pair<T>) obj;
            return (first.equals(test.first)&&second.equals(test.second));
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return first.hashCode()^second.hashCode();
    }
}
