package utility;



public class printList {
    public static <T> void printListsWithIndex(Iterable<T> list) {
        int i = 0;
        for (T item : list) {
            System.out.printf("(%s) %s\n",i,item);
            i++;
        }
        System.out.print("\n");
    }
    public static <T> void printListsWithoutIndex(Iterable<T> list) {
        int i = 0;
        for (T item : list) {
            System.out.printf("%s\n",item);
            i++;
        }
        System.out.print("\n");
    }
}