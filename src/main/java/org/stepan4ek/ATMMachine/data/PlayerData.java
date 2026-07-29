package org.stepan4ek.ATMMachine.data;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private String playerName;
    private double balance;

    public PlayerData(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.balance = 0.0;
    }

    public UUID getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public double getBalance() { return balance; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    //public void setBalance(double balance) { this.balance = balance; }
    public void addBalance(double amount) { this.balance += amount; }
    public boolean removeBalance(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }
    public boolean hasEnough(double amount) { return this.balance >= amount; }
    public void setBalance(double balance) {this.balance = balance;}
}