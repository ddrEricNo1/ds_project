package com.ds.pojo;

/*
this is the pojo class for item
 */


import java.io.Serializable;
import java.util.concurrent.Semaphore;

public class Item implements Serializable {
    // name of the item
    private String name;

    // starting price
    private float starting_price;

    // current bidding price
    private float bidding_price;

    // name of the brand
    private String brand;

    // highest bidder
    private String highest_bidder;

    public Item(String name, float starting_price, String brand) {
        this.name = name;
        this.starting_price = starting_price;
        this.brand = brand;
        this.bidding_price = starting_price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getStarting_price() {
        return starting_price;
    }

    public void setStarting_price(float starting_price) {
        this.starting_price = starting_price;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getHighest_bidder() {
        return highest_bidder;
    }

    public void setHighest_bidder(String highest_bidder) {
        this.highest_bidder = highest_bidder;
    }

    public String getName() {
        return this.name;
    }

    public float getBidding_price() {
        return bidding_price;
    }

    public void setBidding_price(float bidding_price) {
        this.bidding_price = bidding_price;
    }

    @Override
    public String toString() {
        String itemInfo = "Item name: " + this.getName() + " " +
                "starting price: " + this.getStarting_price() + " " +
                "brand: " + this.getBrand() + " " +
                "name of the highest bidder" + this.getHighest_bidder();
        return itemInfo;
    }
}

