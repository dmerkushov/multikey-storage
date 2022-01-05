package ru.dmerkushov.mkcache.loadtest;

import ru.dmerkushov.loadtest.LoadTesting;

public class MKCLoadTest {

    public static void main(String[] args) throws Exception {
        LoadTesting loadTesting = new LoadTesting(100, 10000, 600000);
        loadTesting.runLoadTest(MKCSimulation.class);
    }
}
