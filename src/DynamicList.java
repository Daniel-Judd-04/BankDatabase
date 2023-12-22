import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DynamicList<DataType> implements Iterable<DataType>, Serializable, Cloneable {
    private String name;
    private Object[] array;
    private int size;
    private static final int DEFAULT_CAPACITY = 10;
    private final Boolean leaderboard;

    public DynamicList(String name, DataType[] array) {
        this.name = name;
        this.array = Arrays.copyOf(array, array.length);
        this.size = array.length;
        this.leaderboard = false;
    }

    public DynamicList(String name, int size) {
        this.name = name;
        this.array = new Object[size];
        this.size = size;
        this.leaderboard = false;
    }

    public DynamicList(String name) {
        this.name = name;
        this.array = new Object[DEFAULT_CAPACITY];
        this.size = 0;
        this.leaderboard = false;
    }

    public DynamicList(String name, Boolean leaderboard) {
        this.name = name;
        this.array = new Object[DEFAULT_CAPACITY];
        this.size = 0;
        this.leaderboard = leaderboard;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder(name + ": [");

        for (int i = 0; i < size; i++) {
            if (i != 0) output.append(", ");
            output.append(getComparator(array[i]));
        }
        return output + "]";
    }

    private void print(Object object) {
        System.out.println(object);
    }

    public void print(int index) {
        print(get(index));
    }

//    public void print(String comparator) {
//        print(get(comparator));
//    }

    public void display(int depth) {
        if (size > 0) {
            if (depth == 1) print("");
            print(" │ ".repeat(depth - 1) + " v " + name + " [" + size + "]:");
            for (int i = 0; i < size; i++) {
                Object element = array[i];
                if (element instanceof DynamicList) { // Recursively call function if another dynamic list
                    ((DynamicList<?>) element).display(depth + 1);
                } else {
                    print(" │ ".repeat(depth) + " > " + element);
                }
            }
            print(" │ ".repeat(depth - 1) + " ^ ");
        } else print(" > " + name + " [0]");
    }

    public void display(int depth, Boolean ranked) {
        String cursor;
        if (size > 0) {
            if (depth == 1) print("");
            print(" │ ".repeat(depth - 1) + " v " + name + " [" + size + "]:");
            for (int i = 0; i < size; i++) {
                if (!ranked) cursor = " > ";
                else {
                    if (String.valueOf(i+1).length() == 1) cursor = "  " + (i+1) + " > "; // "  1 > "
                    else if (String.valueOf(i+1).length() == 2) cursor = " " + (i+1) + " > "; // " 11 > "
                    else if (String.valueOf(i+1).length() == 3) cursor = (i+1) + " > "; // "111 > "
                    else cursor = (i+1) + "> "; // "1111> etc."
                }
                Object element = array[i];
                if (element instanceof DynamicList) { // Recursively call function if another dynamic list
                    ((DynamicList<?>) element).display(depth + 1, ranked);
                } else {
                    if (ranked && leaderboard) {
                        if (i == 0) print(" │ ".repeat(depth) + cursor + COLOUR.GOLD + element + COLOUR.RESET);
                        else if (i == 1) print(" │ ".repeat(depth) + cursor + COLOUR.SILVER + element + COLOUR.RESET);
                        else if (i == 2) print(" │ ".repeat(depth) + cursor + COLOUR.BRONZE + element + COLOUR.RESET);
                        else print(" │ ".repeat(depth) + cursor + element);
                    } else {
                        print(" │ ".repeat(depth) + cursor + element);
                    }
                }
            }
            print(" │ ".repeat(depth - 1) + " ^ ");
        } else print(" > " + name + " [0]");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void add(DataType element) {
        if (element != null) {
            if (ensureUnique(element)) {
                ensureCapacity(size + 1);
                array[size++] = element;
            } else {
                System.err.println("Info: Element '" + element + "' not added to DynamicList '" + name + "' as it already exists.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void add(DynamicList element) {
        add((DataType) element);
    }

    private Boolean ensureUnique(DataType newElement) {
        for (int i = 0; i < size; i++) {
            if (getComparator(array[i]).equals(getComparator(newElement))) {
                return false;
            }
        }
        return true;
    }

    private void ensureCapacity(int requiredSize) {
        if (requiredSize > array.length) {
            int newCapacity = Math.max(array.length * 2, requiredSize);
            array = Arrays.copyOf(array, newCapacity);
        }
    }

    public void remove(DataType element) {
        int index = indexOf(element);

        if (index != -1) {
            remove(index);
        }
        throw new NoSuchElementException("Element '" + element + "' not found in DynamicList '" + name + "'");
    }

    public void remove(int index) {
        if (index >= 0 && index < size) {
            int numToMove = size - index - 1;
            if (numToMove > 0) {
                System.arraycopy(array, index + 1, array, index, numToMove);
            }
            array[--size] = null;
        } else
            throw new IndexOutOfBoundsException("Error: Index out of bounds for DynamicList '" + name + "' while trying to remove index " + index + " for length " + size);
    }

    @SuppressWarnings("unchecked")
    public DataType get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Error: Index out of bounds for DynamicList '" + name + "' while trying to get index " + index + " for length " + size);
        }
        return (DataType) array[index];
    }

    @SuppressWarnings("unchecked")
    public DataType get(String comparator) {
        for (int i = 0; i < size; i++) {
            if (comparator.equals(getComparator(array[i]))) {
                return (DataType) array[i];
            }
        }
        System.err.println("Info: Could not get element from DynamicList '" + name + "' with string '" + comparator + "'.");
        return null;
    }

    public String getComparator(Object element) {
        // Different obj will have different comparator values
        // Return the relevant one
        if (element instanceof User) {
            return ((User) element).getId();
        } else if (element instanceof Bank) {
            return ((Bank) element).getId();
        } else if (element instanceof Account) {
            return ((Account) element).getId();
        } else if (element instanceof Transaction) {
            return ((Transaction) element).getId();
        } else if (element instanceof Connection) {
            return ((Connection) element).getId();
        } else if (element instanceof DynamicList<?>) {
            return ((DynamicList<?>) element).getName();
        } else if (element instanceof String) {
            return (String) element;
        } else if (element instanceof String[]) {
            return Arrays.toString((String[]) element);
        } else if (element instanceof SecurityQuestion) {
            return ((SecurityQuestion) element).getId();
        } else if (element instanceof Integer) {
            return String.valueOf(element);
        }
        System.err.println("Info: Could not find comparator of object '" + element + "' in DynamicList '" + name + "'");
        return null;
    }

    public int size() {
        return size;
    }

    public void clear() {
        array = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    public Boolean isEmpty() {
        return size == 0;
    }

    public Boolean contains(DataType element) {
        for (int i = 0; i < size; i++) {
            if (element.equals(array[i])) return true;
        }
        return false;
    }

//    public Boolean contains(String propertyValue) {
//        return get(propertyValue) != null;
//    }

    @SuppressWarnings("unchecked")
    public DataType[] toArray() {
        return Arrays.copyOf(array, size, (Class<? extends DataType[]>) array.getClass());
    }

    public void addAll(Collection<? extends DataType> collection) {
        ensureCapacity(size + collection.size());
        for (DataType element : collection) {
            array[size++] = element;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(array);
    }

    public int indexOf(DataType element) {
        for (int i = 0; i < size; i++) {
            if (element == array[i]) return i;
        }
        throw new NoSuchElementException("Element '" + element + "' not found in DynamicList '" + name + "'");
    }

    @SuppressWarnings("unchecked")
    public void forEach(Consumer<? super DataType> action) {
        for (int i = 0; i < size; i++) {
            action.accept((DataType) array[i]);
        }
    }

    @SuppressWarnings("unchecked")
    public DynamicList<DataType> filter(String propertyName, String propertyValue) {
        DynamicList<DataType> result = new DynamicList<>(name + " (Filtered by " + formatMethodName(propertyName) + ": '" + propertyValue + "')");

        for (int i = 0; i < size; i++) {
            DataType element = (DataType) array[i];
            try {
                // Use reflection to get the property value dynamically
                Method method = element.getClass().getMethod("get" + capitalize(propertyName));
                Object actualValue = method.invoke(element);
                if (actualValue != null && actualValue.toString().equals(propertyValue)) {
                    result.add(element);
                }
            } catch (Exception e) {
                // Handle reflection exceptions
                e.printStackTrace();
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public DynamicList<DataType> filter(String propertyName, Boolean propertyValue) {
        DynamicList<DataType> result = new DynamicList<>(name + " (Filtered by " + formatMethodName(propertyName) + ": '" + propertyValue + "')");

        for (int i = 0; i < size; i++) {
            DataType element = (DataType) array[i];
            try {
                // Use reflection to get the property value dynamically
                Method method = element.getClass().getMethod("get" + capitalize(propertyName));
                Object actualValue = method.invoke(element);
                if (actualValue != null && actualValue.equals(propertyValue)) {
                    result.add(element);
                }
            } catch (Exception e) {
                // Handle reflection exceptions
                e.printStackTrace();
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public DynamicList<DataType> filter(String propertyName, int propertyValue, String operator) {
        DynamicList<DataType> result = new DynamicList<>(name + " (Filtered by " + formatMethodName(propertyName) + " " + operator + " " + propertyValue + ")");

        for (int i = 0; i < size; i++) {
            DataType element = (DataType) array[i];
            try {
                // Use reflection to get the property value dynamically
                Method method = element.getClass().getMethod("get" + capitalize(propertyName));
                Object actualValue = method.invoke(element);
                if (actualValue != null) {
                    if (operator.equals("==") && (int) actualValue == propertyValue) {
                        result.add(element);
                    } else if (operator.equals(">") && (int) actualValue > propertyValue) {
                        result.add(element);
                    } else if (operator.equals("<") && (int) actualValue < propertyValue) {
                        result.add(element);
                    }
                }
            } catch (Exception e) {
                // Handle reflection exceptions
                e.printStackTrace();
            }
        }
        return result;
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String formatMethodName(String methodName) {
        if (methodName.startsWith("get")) methodName = methodName.substring(3);

        StringBuilder output = new StringBuilder();
        String[] parts = methodName.split("(?=[A-Z])");
        for (int i = 0; i < parts.length; i++) {
            if (i != 0) output.append(" ");
            output.append(capitalize(parts[i]));
        }
        return output.toString();
    }

    @SuppressWarnings("unchecked")
    public DynamicList<DataType> reversed() {
        DynamicList<DataType> reversedList = new DynamicList<>(name + " (Reversed)");
        for (int i = size - 1; i >= 0; i--) {
            reversedList.add((DataType) array[i]);
        }
        return reversedList;
    }

    @SuppressWarnings("unchecked")
    public DynamicList<DataType> random() {
        Random random = new Random();

        Object[] newArray = Arrays.copyOf(array, size);

        for (int i = newArray.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);

            // Swap elements at i and index
            Object temp = newArray[i];
            newArray[i] = newArray[index];
            newArray[index] = temp;
        }

        return (DynamicList<DataType>) new DynamicList<>(name + " (Random)", newArray);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DynamicList<DataType> clone() {
        try {
            DynamicList<DataType> clone = (DynamicList<DataType>) super.clone();
            clone.name = name + " (Clone)";
            clone.array = Arrays.copyOf(array, size);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @SuppressWarnings("unchecked")
    public DynamicList<DataType> sort(Comparator<? super DataType> comparator, String sortMethod) {
        DataType[] tempArray = (DataType[]) Arrays.copyOf(array, size);
        Arrays.sort(tempArray, comparator);
        DynamicList<DataType> newList = new DynamicList<>(name + " (Sorted by '" + formatMethodName(sortMethod) + "')");
        newList.setFromArray(tempArray);
        return newList;
    }

    public void setFromArray(Object[] newArray) {
        setFromArray(newArray, name);
    }

    public void setFromArray(Object[] newArray, String newName) {
        this.name = newName;
        this.array = Arrays.copyOf(newArray, newArray.length);
        this.size = newArray.length;
    }

    @SuppressWarnings("unchecked")
    public DynamicList<DataType> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        DynamicList<DataType> sublist = new DynamicList<>(name + " (Sublist)");
        sublist.addAll((Collection<? extends DataType>) Arrays.asList(Arrays.copyOfRange(array, fromIndex, toIndex)));
        return sublist;
    }

    @SuppressWarnings("unchecked")
    public Stream<DataType> stream() {
        return Arrays.stream(array, 0, size).map(obj -> (DataType) obj);
    }

    @Override
    public Iterator<DataType> iterator() {
        return new Iterator<>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public DataType next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return get(currentIndex++);
            }
        };
    }

    public enum COLOUR {
        GOLD("\u001B[38;2;204;173;0m"),
        SILVER("\u001B[38;2;202;202;202m"),
        BRONZE("\u001B[38;2;205;127;50m"),
        RESET("\u001B[0m");

        public final String ANSIValue;

        COLOUR(String ANSIValue) {
            this.ANSIValue =  ANSIValue;
        }

        public String toString() {
            return ANSIValue;
        }
    }

    // ADD combination to DynamicList
}
