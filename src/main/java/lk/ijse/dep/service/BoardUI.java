package lk.ijse.dep.service;

public interface BoardUI {
    void update(int col, boolean isHuman);
    void notifyWinner(Winner winner);
}
