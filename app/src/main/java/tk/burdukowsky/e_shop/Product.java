package tk.burdukowsky.e_shop;

/**
 * Created by Android Studio
 * User: STANISLAV
 * Date: 01 Март 2017 22:23
 */

class Product {

    // поля класса
    private Integer id;
    private String name;
    private Integer cost;

    // конструктор
    Product(Integer id, String name, Integer cost) {
        this.id = id;
        this.name = name;
        this.cost = cost;
    }

    // геттеры

    Integer getId() {
        return id;
    }

    String getName() {
        return name;
    }

    Integer getCost() {
        return cost;
    }
}
